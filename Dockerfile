FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B -Pdocker
COPY src ./src
COPY funds.db.zst ./src/main/resources/
RUN mvn package -DskipTests -Pdocker -B

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S spring && adduser -S spring -G spring
COPY --chown=spring:spring --from=build /app/target/historical-mf-nav-data-*.jar app.jar

ENV DAILY_NAV_DATABASE_TYPE=postgres
ENV DAILY_NAV_URL=jdbc:postgresql://postgres:5432/dailynav
ENV DAILY_NAV_USERNAME=postgres
ENV DAILY_NAV_PASSWORD=postgres
ENV SPRING_PROFILES_ACTIVE=docker

EXPOSE 8080

RUN apk add --no-cache curl
HEALTHCHECK --interval=30s --timeout=3s \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
