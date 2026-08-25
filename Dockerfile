FROM gradle:8.14.3-jdk21-alpine AS build
WORKDIR /workspace

COPY settings.gradle.kts build.gradle.kts gradle.properties ./
COPY src ./src

RUN gradle --no-daemon clean bootJar

FROM eclipse-temurin:25-jre-alpine

RUN addgroup -S app && adduser -S -G app -u 10001 app
WORKDIR /app

COPY --from=build /workspace/build/libs/*.jar /app/app.jar

USER 10001:10001
EXPOSE 8080

ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
