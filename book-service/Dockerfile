FROM openjdk:17-jdk-slim AS common-builder
LABEL authors="l4yoos"
WORKDIR /app

COPY common/ /app/common/

RUN chmod +x /app/common/mvnw

RUN --mount=type=cache,target=/root/.m2 \
    cd common && ./mvnw clean install -B -Dmaven.test.skip=true -X && \
    cp -R /root/.m2 /app/.m2_cache_export

FROM openjdk:17-jdk-slim AS book-service-builder
LABEL authors="l4yoos"
WORKDIR /app

COPY --from=common-builder /app/.m2_cache_export /root/.m2

COPY . /app/

RUN cd book-service && ./mvnw clean package -B -Dmaven.test.skip=true

FROM openjdk:17-jdk-slim AS book-service-run
LABEL authors="l4yoos"
WORKDIR /app
COPY --from=book-service-builder /app/book-service/target/*.jar app.jar
EXPOSE 8880
ENTRYPOINT ["java","-jar","app.jar"]