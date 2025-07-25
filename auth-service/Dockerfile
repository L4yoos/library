FROM eclipse-temurin:17-jdk-jammy AS common-builder
LABEL authors="l4yoos"
WORKDIR /app
COPY . /app/
RUN cd common && ./mvnw clean install -B -Dmaven.test.skip=true -X

FROM eclipse-temurin:17-jdk-jammy AS auth-service-builder
LABEL authors="l4yoos"
WORKDIR /app
COPY --from=common-builder /root/.m2 /root/.m2
COPY . /app/
RUN cd auth-service && ./mvnw clean package -B -Dmaven.test.skip=true

FROM eclipse-temurin:17-jre-jammy AS auth-service-run
LABEL authors="l4yoos"
WORKDIR /app
COPY --from=auth-service-builder /app/auth-service/target/*.jar app.jar
EXPOSE 8884
ENTRYPOINT ["java","-jar","app.jar"]