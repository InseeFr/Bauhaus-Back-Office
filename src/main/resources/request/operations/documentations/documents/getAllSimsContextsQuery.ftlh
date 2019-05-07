
SELECT DISTINCT ?sims  
  WHERE
{   GRAPH ?src
    	{ ?a ?b ?c} 
	FILTER(REGEX(STR(?src), '/qualite/rapport/')) 
	BIND(REPLACE( STR(?src) , '(.*/)(\\w.+$)', '$2' ) AS ?sims) . 
}