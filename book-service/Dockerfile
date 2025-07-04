FROM openjdk:17-jdk-slim AS build
LABEL authors="l4yoos"
WORKDIR /app
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

RUN ./mvnw clean package -DskipTests

FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8880
ENTRYPOINT ["java","-jar","app.jar"]