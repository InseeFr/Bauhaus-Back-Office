FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /opt/app
<<<<<<<< HEAD:Dockerfile.bauhaus

COPY ./.mvn .mvn
COPY ./mvnw mvnw


COPY ./pom.xml ./
COPY ./module-bauhaus-bo/ ./module-bauhaus-bo
COPY ./module-domain/ ./module-domain
COPY ./module-infrastructure/ ./module-infrastructure

========
COPY ../.mvn .mvn
COPY ../mvnw pom.xml ./
COPY src ./src
>>>>>>>> 6f7e20ca (feat: init mono repo (#991)):module-bauhaus-bo/Dockerfile
RUN chmod +x mvnw
RUN ./mvnw clean install -DskipTests=true

FROM eclipse-temurin:21-jre-jammy

RUN groupadd -g 10000 javagroup
RUN useradd -u 10000 -g javagroup -s /usr/sbin/nologin javauser
RUN mkdir /opt/app/
RUN chown -R 10000:10000 /opt/app/

USER 10000
COPY --from=builder /opt/app/module-bauhaus-bo/target/*.jar /opt/app/bauhaus.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar",  "/opt/app/bauhaus.jar"]
