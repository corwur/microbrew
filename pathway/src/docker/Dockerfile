FROM openjdk:11.0-jdk-slim
ARG jar
COPY ${jar} /app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]