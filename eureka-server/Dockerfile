FROM eclipse-temurin:17-jdk-jammy AS eureka-server-builder
LABEL authors="l4yoos"
WORKDIR /app
COPY . /app/
RUN cd eureka-server && ../mvnw clean package -B -Dmaven.test.skip=true

FROM eclipse-temurin:17-jre-jammy
LABEL authors="l4yoos"
WORKDIR /app
COPY --from=eureka-server-builder /app/eureka-server/target/*.jar app.jar
EXPOSE 8761
ENTRYPOINT ["java","-jar","app.jar"]