ASK {
    ?collection ?b ?c .
    FILTER(STRENDS(STR(?collection),'/concepts/definitions/${ID}')) .
}
