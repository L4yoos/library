FROM openjdk:17-jdk-slim AS common-builder
LABEL authors="l4yoos"
WORKDIR /app
COPY . /app/
RUN cd common && ./mvnw clean install -B -Dmaven.test.skip=true -X

FROM openjdk:17-jdk-slim AS loan-service-builder
LABEL authors="l4yoos"
WORKDIR /app
COPY --from=common-builder /root/.m2 /root/.m2

COPY . /app/

RUN cd loan-service && ./mvnw clean package -B -Dmaven.test.skip=true

FROM openjdk:17-jdk-slim AS loan-service-run
LABEL authors="l4yoos"
WORKDIR /app
COPY --from=loan-service-builder /app/loan-service/target/*.jar app.jar
EXPOSE 8882
ENTRYPOINT ["java","-jar","app.jar"]