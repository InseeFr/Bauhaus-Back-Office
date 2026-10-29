#!/usr/bin/env bash
# Rejoue en curl les appels Colectica que fait le back-office pour servir
#   GET /api/ddi/mutualized-codes-list/{agency}/{id}/codes
# Chaque réponse brute est gardée dans $OUT_DIR. Le résultat final (même JSON que l'endpoint) est
# reconstruit dans $OUT_DIR/codes.json : cette étape est faite par le back, pas par Colectica.
#
# Usage :
#   eval "$(mise env -s bash -C Bauhaus-Back-Office)"
#   scripts/mutualized-codes.sh [agency] [id]
#
# Variables (défauts = colectica.yml) :
#   COLECTICA_BASE_URL   https://metadonnees-operations-demo.insee.fr
#   COLECTICA_API_PATH   /api/v1/
#   COLECTICA_LANG       fr-FR
#   MUTUALIZED_GROUP     fr.insee/6755ebd7-82a4-4e8d-9b49-ba39a9ab3281
#   COLECTICA_TOKEN      token déjà obtenu (sinon COLECTICA_USERNAME / COLECTICA_PASSWORD)
#   OUT_DIR              /tmp/mutualized-codes
set -euo pipefail

AGENCY=${1:-fr.insee}
ID=${2:-dec898d6-da86-3f54-9c4f-8465216f2ca8}
BASE_URL=${COLECTICA_BASE_URL:-https://metadonnees-operations-demo.insee.fr}
BASE_URL=${BASE_URL%/}
API_PATH=${COLECTICA_API_PATH:-/api/v1/}
API="$BASE_URL/${API_PATH#/}"
LANG_TAG=${COLECTICA_LANG:-fr-FR}
GROUP=${MUTUALIZED_GROUP:-fr.insee/6755ebd7-82a4-4e8d-9b49-ba39a9ab3281}
GROUP_AGENCY=${GROUP%%/*}
GROUP_ID=${GROUP#*/}
OUT_DIR=${OUT_DIR:-/tmp/mutualized-codes}

# colectica.yml › itemTypes
CODE_LIST_TYPE=8b108ef8-b642-4484-9c49-f88e4bf7cf1d
CATEGORY_TYPE=7e47c269-bcab-40f7-a778-af7bbc4e3d00

mkdir -p "$OUT_DIR"

step() { printf '\n== %s\n' "$*" >&2; }

# call <fichier de sortie> <méthode> <url> [corps JSON]
call() {
  local out=$1 method=$2 url=$3 body=${4:-}
  local args=(-sS --fail-with-body -o "$out" -w '   → HTTP %{http_code} en %{time_total}s, %{size_download} octets\n'
    -X "$method" "$url" -H "Authorization: Bearer $TOKEN")
  if [[ -n $body ]]; then
    args+=(-H 'Content-Type: application/json' -d "$body")
    echo "curl -X $method '$url' -H 'Authorization: Bearer \$TOKEN' -H 'Content-Type: application/json' -d '$body'" >&2
  else
    echo "curl -X $method '$url' -H 'Authorization: Bearer \$TOKEN'" >&2
  fi
  curl "${args[@]}" >&2
}

# 1. Authentification : ColecticaClient#authenticate (sautée si COLECTICA_TOKEN est fourni)
if [[ -n ${COLECTICA_TOKEN:-} ]]; then
  TOKEN=$COLECTICA_TOKEN
else
  step "1. Authentification"
  : "${COLECTICA_USERNAME:?absent : eval \"\$(mise env -s bash -C Bauhaus-Back-Office)\"}"
  : "${COLECTICA_PASSWORD:?absent}"
  echo "curl -X POST '$BASE_URL/token/createtoken' -H 'Content-Type: application/json' -d '{\"username\":\"$COLECTICA_USERNAME\",\"password\":\"***\"}'" >&2
  TOKEN=$(curl -sS --fail-with-body -X POST "$BASE_URL/token/createtoken" -H 'Content-Type: application/json' \
    -d "$(jq -n --arg u "$COLECTICA_USERNAME" --arg p "$COLECTICA_PASSWORD" '{username: $u, password: $p}')" \
    | jq -r .access_token)
  echo "   → token obtenu (${#TOKEN} caractères)" >&2
fi

# 2. Version du groupe : ConfiguredGroupsCodeListRefsProvider#latestGroupVersions
step "2. Dernière version du groupe mutualisé $GROUP"
call "$OUT_DIR/2-group-version.json" POST "${API}item/_getLatestVersionNumbers" \
  "{\"Identifiers\":[{\"AgencyId\":\"$GROUP_AGENCY\",\"Identifier\":\"$GROUP_ID\"}]}"
GROUP_VERSION=$(jq -r '.[0].Item2' "$OUT_DIR/2-group-version.json")
echo "   → version $GROUP_VERSION" >&2

# 3. CodeLists du groupe : ConfiguredGroupsCodeListRefsProvider#childrenOfType
step "3. CodeLists mutualisées du groupe"
call "$OUT_DIR/3-group-codelists.json" POST "${API}_query/relationship/bysubject/descriptions" \
  "{\"itemTypes\":[\"$CODE_LIST_TYPE\"],\"targetItem\":{\"agencyId\":\"$GROUP_AGENCY\",\"identifier\":\"$GROUP_ID\",\"version\":$GROUP_VERSION},\"useDistinctTargetItem\":true}"
jq -r '.[] | select(. != null) | "   → \(.AgencyId)/\(.Identifier)"' "$OUT_DIR/3-group-codelists.json" >&2
if ! jq -e --arg a "$AGENCY" --arg i "$ID" 'any(.[]; . != null and .AgencyId == $a and .Identifier == $i)' \
  "$OUT_DIR/3-group-codelists.json" >/dev/null; then
  echo "!! $AGENCY/$ID n'est pas mutualisée : le back répond 200 avec un corps vide." >&2
  exit 1
fi

# 4. Version de la liste : ColecticaCodeListRepository#latestVersion
step "4. Dernière version de la liste $AGENCY/$ID"
call "$OUT_DIR/4-codelist-version.json" POST "${API}item/_getLatestVersionNumbers" \
  "{\"Identifiers\":[{\"AgencyId\":\"$AGENCY\",\"Identifier\":\"$ID\"}]}"
VERSION=$(jq -r '.[0].Item2' "$OUT_DIR/4-codelist-version.json")
echo "   → version $VERSION" >&2

# 5 et 6 : ColecticaCodeListRepository#getMutualizedCodeList (en parallèle côté back)
step "5. CodeList : XML DDI 3 dans le champ Item (Value + CategoryReference de chaque code)"
call "$OUT_DIR/5-codelist.json" GET "${API}item/$AGENCY/$ID/$VERSION"

step "6. Catégories du set : enveloppes _query, le libellé vient du champ Label"
call "$OUT_DIR/6-categories.json" POST "${API}_query" \
  "{\"itemTypes\":[\"$CATEGORY_TYPE\"],\"searchLatestVersion\":true,\"searchSets\":[{\"agencyId\":\"$AGENCY\",\"identifier\":\"$ID\",\"version\":$VERSION}],\"maxResults\":1000000}"

# Projection faite par le back : MutualizedCodeListCodes#from
step "Reconstruction de la réponse /codes (côté back, aucun appel Colectica)"
python3 -I - "$OUT_DIR" "$LANG_TAG" <<'PY'
import json, sys, xml.etree.ElementTree as ET

out_dir, lang = sys.argv[1], sys.argv[2]
local = lambda e: e.tag.rsplit("}", 1)[-1]
child = lambda e, name: next((c for c in e if local(c) == name), None)
text = lambda e: (e.text or "").strip() if e is not None else None
XML_LANG = "{http://www.w3.org/XML/1998/namespace}lang"

def label_in(labels):
    # la langue demandée si la clé existe (même vide), sinon la première ; "" sans libellé
    if not labels:
        return ""
    return (labels[lang] if lang in labels else next(iter(labels.values()))) or ""

with open(f"{out_dir}/6-categories.json", encoding="utf-8") as f:
    results = json.load(f).get("Results") or []
latest = {}
for cat in results:  # dernière version par agence/identifiant
    key = (cat["AgencyId"], cat["Identifier"])
    if key not in latest or latest[key]["Version"] < cat["Version"]:
        latest[key] = cat
labels = {cat["Identifier"]: label_in(cat.get("Label")) for cat in latest.values()}

with open(f"{out_dir}/5-codelist.json", encoding="utf-8") as f:
    root = ET.fromstring(json.load(f)["Item"])
code_list = root if local(root) == "CodeList" else next(e for e in root.iter() if local(e) == "CodeList")

codes = []
for code in (c for c in code_list if local(c) == "Code"):
    ref = child(code, "CategoryReference")
    label = labels.get(text(child(ref, "ID")), "") if ref is not None else ""
    codes.append({"id": text(child(code, "ID")), "value": text(child(code, "Value")) or "", "label": label})

list_label = child(code_list, "Label")
list_labels = {}
if list_label is not None:
    for content in (c for c in list_label if local(c) == "Content"):
        list_labels.setdefault(content.get(XML_LANG, ""), content.text or "")

result = {
    "agencyId": text(child(code_list, "Agency")),
    "id": text(child(code_list, "ID")),
    "version": text(child(code_list, "Version")),
    "label": label_in(list_labels),
    "codes": codes,
}
with open(f"{out_dir}/codes.json", "w", encoding="utf-8") as f:
    json.dump(result, f, ensure_ascii=False)
empty = sum(1 for c in codes if c["label"] == "")
print(f"   → {len(latest)} catégorie(s), {len(codes)} code(s), dont {empty} libellé(s) vide(s)", file=sys.stderr)
PY
echo "   → réponses brutes et codes.json dans $OUT_DIR" >&2
