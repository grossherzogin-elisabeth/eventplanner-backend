FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY . .
RUN ./gradlew build

FROM eclipse-temurin:21-jdk-alpine
COPY --from=builder /app/build/libs/*.jar app.jar
#COPY --from=builder /app/static/ static/
ENTRYPOINT ["java","-XX:+UseContainerSupport","-jar","/app.jar"]
