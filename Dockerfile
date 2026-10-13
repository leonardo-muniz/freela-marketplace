FROM eclipse-temurin:27-jre

WORKDIR /app

ARG SERVICE

COPY ${SERVICE}/target/*.jar /app/app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
