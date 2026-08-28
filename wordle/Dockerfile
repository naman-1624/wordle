# -------- Build Stage --------
FROM maven:3.9.12-eclipse-temurin-25 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# -------- Run Stage --------
FROM eclipse-temurin:25-jdk
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]