FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM gocv/opencv:4.9.0-ubuntu-22.04

RUN apt-get update && apt-get install -y --no-install-recommends \
    openjdk-21-jre-headless \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

ENV LD_LIBRARY_PATH=/usr/local/lib:$LD_LIBRARY_PATH
ENV JAVA_OPTS="-Djava.library.path=/usr/local/share/java/opencv4"

COPY --from=build /app/target/*.jar /app/facedec.jar

ENV PORT=8080
EXPOSE 8080
ENTRYPOINT ["java","--enable-preview","-jar","facedec.jar"]