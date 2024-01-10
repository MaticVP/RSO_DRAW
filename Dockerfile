FROM maven:3.8.3-openjdk-17 AS build

COPY ./ /app

WORKDIR /app

RUN mvn -DskipTests=true test --show-version --update-snapshots --batch-mode clean package

FROM eclipse-temurin:17-jre-ubi9-minimal

RUN mkdir /app

WORKDIR /app

ARG JAR_FILE

ENV LINUX_JAR_FILE=${JAR_FILE}

ENV DB_URL=${SPRING_CONFIG_URI}

COPY --from=build ./app/target/demo-0.0.1-SNAPSHOT.jar /app

EXPOSE 8082

ENTRYPOINT ["java", "-Dspring.datasource.url=${DBURI}","-Dspring.datasource.username=${DBUSERNAME}", "-Dspring.datasource.password=${PASSWORD}","-Dspring.config.import=${CONFIGIP}","-jar", "demo-0.0.1-SNAPSHOT.jar"]

