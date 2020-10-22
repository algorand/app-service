FROM openjdk:11

ADD target/app-service-0.0.3-SNAPSHOT.jar app-service-0.0.3-SNAPSHOT.jar

ADD app-service.yml app-service.yml

CMD java -jar app-service-0.0.3-SNAPSHOT.jar server app-service.yml

EXPOSE 8080 8081
