FROM gradle:9.4-jdk21 AS build

WORKDIR /app

COPY . .

RUN gradle bootJar --no-daemon

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/build/libs/post-service.jar post-service.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "post-service.jar"]