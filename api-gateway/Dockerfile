FROM eclipse-temurin:17-jdk-jammy AS api-gateway-builder
LABEL authors="l4yoos"
WORKDIR /app
COPY mvnw ./
COPY .mvn/ .mvn/
COPY api-gateway/pom.xml api-gateway/
COPY api-gateway/src/ api-gateway/src/
RUN ./mvnw dependency:go-offline -f api-gateway/pom.xml
RUN cd api-gateway && ../mvnw clean package -B -Dmaven.test.skip=true

FROM eclipse-temurin:17-jre-jammy
LABEL authors="l4yoos"
WORKDIR /app
COPY --from=api-gateway-builder /app/api-gateway/target/*.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]
EXPOSE 8081