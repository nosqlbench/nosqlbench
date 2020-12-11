FROM openjdk:15-alpine
RUN apk --no-cache add curl

COPY nb/target/nb.jar nb.jar
ENTRYPOINT ["java","-jar", "nb.jar"]
RUN mkdir -p /nosqlbench
