FROM eclipse-temurin:17-jre-alpine
ADD target/spring-batch5-mongodb*.jar spring-batch5-mongodb.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","spring-batch5-mongodb.jar"]