FROM eclipse-temurin:21-jdk-jammy as builder
WORKDIR /opt/app
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
COPY ./src ./src
RUN ./mvnw clean install -DskipTests=true

FROM eclipse-temurin:21-jre-alpine
WORKDIR /opt/app

RUN addgroup -g 10000 javagroup
RUN adduser -D -s / -u 10000 javauser -G javagroup
RUN chown -R 10000:10000 /opt/app/

USER 10000
COPY --from=builder /opt/app/target/*.jar /opt/app/bauhaus.jar
ENTRYPOINT ["java", "-jar",  "/opt/app/bauhaus.jar"]