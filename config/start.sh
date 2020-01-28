#!/bin/sh

set -e

echo fr.insee.rmes.bauhaus.sesame.gestion.sesameServer=http://graphdb:7200 >> /usr/local/tomcat/webapps/bauhaus-dev.properties

exec "$@"
