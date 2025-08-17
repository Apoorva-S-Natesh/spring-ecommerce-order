## Stage 1: Build
#FROM gradle:8-jdk21 AS builder
#WORKDIR /app
#COPY . .
#RUN ./gradlew clean build -x test
#
## Stage 2: Run (use JRE since Kotlin compiles to JVM bytecode)
#FROM eclipse-temurin:21-jre
#WORKDIR /app
#COPY --from=builder /app/build/libs/*SNAPSHOT.jar app.jar
#EXPOSE 8080
#ENTRYPOINT ["java", "-jar", "app.jar"]

FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy the locally built JAR into the container
COPY build/libs/spring-ecommerce-0.0.1-SNAPSHOT.jar app.jar

# Expose the app port
EXPOSE 8080

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]
