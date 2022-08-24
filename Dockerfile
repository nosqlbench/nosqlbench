FROM eclipse-temurin:17-jdk

COPY nb5/target/nb5.jar nb5.jar
ENTRYPOINT ["java","-jar", "nb5.jar"]

RUN ln -s /bin/bash /bin/ash
RUN ln -s nb5.jar nb.jar
#RUN apk add --update udev
RUN mkdir -p /nosqlbench
