FROM openjdk:11
RUN apt-get update && apt-get install -y curl ash

COPY nb/target/nb.jar nb.jar
ENTRYPOINT ["java","-jar", "nb.jar"]
RUN mkdir -p /nosqlbench
