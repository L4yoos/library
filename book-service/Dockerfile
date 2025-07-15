FROM openjdk:17-jdk-slim AS common-builder
LABEL authors="l4yoos"
WORKDIR /app
COPY . /app/
RUN cd common && ./mvnw clean install -B -Dmaven.test.skip=true -X

FROM openjdk:17-jdk-slim AS book-service-builder
LABEL authors="l4yoos"
WORKDIR /app
COPY --from=common-builder /root/.m2 /root/.m2
COPY --from=common-builder /app /app
RUN cd book-service && ./mvnw clean package -B -Dmaven.test.skip=true

FROM openjdk:17-jdk-slim AS book-service-run
LABEL authors="l4yoos"
WORKDIR /app
COPY --from=book-service-builder /app/book-service/target/*.jar app.jar
EXPOSE 8880
ENTRYPOINT ["java","-jar","app.jar"]