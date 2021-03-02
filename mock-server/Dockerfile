FROM openjdk:8-jdk-alpine
ADD ./wiremock-standalone-2.27.2.jar /app.jar
COPY ./__files /__files
COPY ./mappings /mappings
ENTRYPOINT java -jar ./app.jar --port 9991 --root-dir /