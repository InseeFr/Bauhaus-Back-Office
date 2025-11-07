
// GET concepts /members
[
{
"prefLabelLg1": "Accueils collectifs de mineurs à caractère éducatif",
"prefLabelLg2": "Collective education centres for minors",
"id": "c1201"
}
]
// GET /collection/:id
{
"creator": "HIE2004879",
"isValidated": "false",
"contributor": "DG75-L201",
"prefLabelLg1": "dfg",
"prefLabelLg2": "dfg",
"created": "2025-11-07T09:36:29.712314",
"id": "c1d368b6-9313-4203-8889-5a433bff46c4",
"concepts": [
    {
    "prefLabelLg1": "Accueils collectifs de mineurs à caractère éducatif",
    "prefLabelLg2": "Collective education centres for minors",
    "id": "c1201"
    }
]
}

// BODY du PUT
{
    "id":"c1d368b6-9313-4203-8889-5a433bff46c4", //NotNull
    "prefLabelLg1":"dfg", //NotNull
    "prefLabelLg2":"dfg", //Nullable
    "descriptionLg1":"dfg",//Nullable
    "descriptionLg2":"dfg",//Nullable
    "creator":"HIE2004879",//NotNull
    "contributor":"DG75-L201",//Nullable
    "concepts":["c12345"]// [] ou string[]
}

// BODY du POST
{
    "prefLabelLg1":"dfg", 
    "prefLabelLg2":"dfg",
    "descriptionLg1":"dfg",
    "descriptionLg2":"dfg",
    "creator":"HIE2004879",
    "contributor":"DG75-L201",
    "concepts":["c12345"]
}
