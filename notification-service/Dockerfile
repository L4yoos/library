FROM openjdk:17-jdk-slim AS common-builder
LABEL authors="l4yoos"
WORKDIR /app
COPY . /app/
RUN cd common && ./mvnw clean install -B -Dmaven.test.skip=true -X

FROM openjdk:17-jdk-slim AS notification-service-builder
LABEL authors="l4yoos"
WORKDIR /app
COPY --from=common-builder /root/.m2 /root/.m2
COPY --from=common-builder /app /app
RUN cd notification-service && ./mvnw clean package -B -Dmaven.test.skip=true

FROM openjdk:17-jdk-slim AS notification-service-run
LABEL authors="l4yoos"
WORKDIR /app
COPY --from=notification-service-builder /app/notification-service/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]