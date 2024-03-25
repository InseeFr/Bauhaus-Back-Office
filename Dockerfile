FROM eclipse-temurin:21-jre-alpine
WORKDIR application
COPY target/*.jar bauhaus.jar
ENTRYPOINT ["java", "-jar",  "/application/bauhaus.jar"]