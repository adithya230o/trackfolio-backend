FROM eclipse-temurin:21-jdk

LABEL authors="adi"

WORKDIR /app

# Copy packaged JAR
COPY target/trackfolio-0.0.1-SNAPSHOT.jar trackfolio.jar

# Expose port
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/trackfolio.jar"]