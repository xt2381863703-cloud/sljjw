FROM maven:3.9.8-eclipse-temurin-17 AS build
WORKDIR /workspace

RUN mkdir -p /root/.m2
COPY maven-settings.xml /root/.m2/settings.xml

COPY . .
RUN mvn -s /root/.m2/settings.xml -q -DskipTests -pl reservation-service -am package

FROM eclipse-temurin:17-jre
WORKDIR /app
RUN mkdir -p /data

ARG JAR_PATH
COPY --from=build /workspace/${JAR_PATH} app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
