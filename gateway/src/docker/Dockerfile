FROM openjdk:11.0-jdk-slim
ARG jar
COPY ${jar} /app.jar
COPY application.yml /application.yml
EXPOSE 8090
ENTRYPOINT ["java", "-jar", "app.jar"]