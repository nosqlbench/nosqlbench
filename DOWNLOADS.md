# Getting NoSQLBench

## Latest Downloads

The latest release of NoSQLBench is always available from github releases.

- download [the latest release of nb5](https://github.com/nosqlbench/nosqlbench/releases/latest/download/nb5), a linux binary
  - To download it with curl,
    use `curl -L -O https://github.com/nosqlbench/nosqlbench/releases/latest/download/nb5`
  - (be sure to `chmod +x nb` once you download it)
- download [the latest release of nb5.jar](https://github.com/nosqlbench/nosqlbench/releases/latest/download/nb5.jar), a single-jar application.
  - This requires java 15 or later, make sure your `java -version`
    command says that you are on Java 15 or later.
  - To download it with curl, use `curl -L -O https://github.com/nosqlbench/nosqlbench/releases/latest/download/nb5.jar`.

## Docker

You can use a live docker image for the latest nosqlbench.

1. run `docker pull nosqlbench/nosqlbench`
2. You can run `docker run --rm nosqlbench/nosqlbench ...` just as you would with `nb5 ...` or `java -jar nb5.jar` ...

Links to docker images:

- [nosqlbench/nosqlbench:latest](https://hub.docker.com/r/nosqlbench/nosqlbench/tags?page=1&name=latest)
- [All tagged docker images](https://hub.docker.com/r/nosqlbench/nosqlbench/tags)

