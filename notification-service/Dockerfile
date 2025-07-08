FROM openjdk:17-jdk-slim AS build
LABEL authors="l4yoos"
WORKDIR /app
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

ARG RESEND_API_KEY_ARG
ENV RESEND_API_KEY=${RESEND_API_KEY_ARG}

RUN ./mvnw clean package -DskipTests

FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8881
ENTRYPOINT ["java","-jar","app.jar"]