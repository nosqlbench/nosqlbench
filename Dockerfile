FROM eclipse-temurin:17-alpine
RUN apk --no-cache add curl

COPY nb/target/nb.jar nb.jar
ENTRYPOINT ["java","-jar", "nb.jar"]
RUN apk add --update udev
RUN mkdir -p /nosqlbench
