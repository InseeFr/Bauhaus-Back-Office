FROM eclipse-temurin:21-jdk-jammy as builder
WORKDIR /opt/app
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
COPY ./src ./src
RUN ./mvnw clean install -DskipTests=true

FROM eclipse-temurin:21-jdk-alpine
WORKDIR /opt/app

COPY --from=builder /opt/app/target/*.jar /opt/app/bauhaus.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar",  "/opt/app/bauhaus.jar"]
