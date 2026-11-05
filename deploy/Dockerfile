FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /src
COPY pom.xml .
COPY services ./services
ARG MODULE
RUN mvn -q -DskipTests package -pl services/$${MODULE} -am

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
ARG MODULE
COPY --from=build /src/services/${MODULE}/target/app.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
