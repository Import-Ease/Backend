FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace/app

COPY pom.xml ./
COPY mvnw ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw -q -DskipTests dependency:go-offline

COPY src ./src
RUN ./mvnw -q -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /workspace/app/target/*.jar app.jar

ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java -jar /app/app.jar"]
