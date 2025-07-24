FROM openjdk:17-jdk-slim
LABEL authors="l4yoos"
VOLUME /tmp
ARG JAR_FILE=api-gateway/target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","app.jar"]
EXPOSE 8081