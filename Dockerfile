FROM eclipse-temurin:17-jdk-alpine AS base
WORKDIR /app

FROM base as build
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:resolve

COPY ./src ./src
RUN ./mvnw package

FROM base as final
COPY --from=build app/target/vider-quantum-engine-1.0-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]
