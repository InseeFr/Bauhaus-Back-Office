FROM gitlab-registry.insee.fr:443/kubernetes/images/run/jre:21

COPY ./target/*.jar /usr/local/app.jar

EXPOSE 8080

RUN adduser starter
USER starter

CMD ["java", "-jar","/usr/local/app.jar"]
