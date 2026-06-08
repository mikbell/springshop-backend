FROM eclipse-temurin:26-jdk AS build
WORKDIR /app

ENV MAVEN_OPTS="-Xmx512m -XX:MaxRAMPercentage=75.0"

RUN apt-get update && apt-get install -y tar curl && rm -rf /var/lib/apt/lists/*

COPY .mvn/ .mvn
COPY mvnw pom.xml ./

RUN sed -i 's/\r$//' mvnw && chmod +x mvnw

RUN ./mvnw dependency:go-offline -B

COPY src ./src
RUN ./mvnw clean package -DskipTests -Dmaven.test.skip=true

FROM eclipse-temurin:26-jre-alpine
WORKDIR /app

RUN apk add --no-cache curl \
    && addgroup -S spring && adduser -S spring -G spring \
    && mkdir -p /app/uploads/products \
    && chown -R spring:spring /app/uploads

USER spring:spring

COPY --from=build /app/target/*.jar app.jar

EXPOSE 3000

HEALTHCHECK --interval=30s --timeout=5s --start-period=45s --retries=3 \
    CMD curl -fsS "http://localhost:${SERVER_PORT:-3000}/actuator/health" || exit 1

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS:-} -jar app.jar"]
