FROM openjdk:14-alpine
COPY nb/target/nb.jar nb.jar
ENTRYPOINT ["java","-jar", "nb.jar"]
