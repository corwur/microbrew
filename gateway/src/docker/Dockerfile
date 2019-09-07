FROM openjdk:11.0-jdk-slim
ARG jar
COPY ${jar} /app.jar
EXPOSE 8090
ENTRYPOINT ["java", "-jar", "app.jar"]