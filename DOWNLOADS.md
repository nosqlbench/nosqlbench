# Getting NoSQLBench

## Latest Downloads

The latest release of NoSQLBench is always available from github releases.

- download [the latest release of nb](https://github.com/nosqlbench/nosqlbench/releases/latest/download/nb), a linux
  binary
  - (be sure to `chmod +x nb` once you download it)
- download [the latest release of nb.jar](https://github.com/nosqlbench/nosqlbench/releases/latest/download/nb.jar), a
  single-jar application.
  - This requires java 12 or later, make sure your `java -version` command says that you are on Java 12 or later.

## Docker

You can use a live docker image for the latest nosqlbench.

1. run `docker pull nosqlbench/nosqlbench`
2. docserver `docker run -p 12345:12345 --rm --name nb-docs nosqlbench/nosqlbench docserver http://0.0.0.0:12345
`
3. Any other command can be run against your nosqlbench docker images using this form.

Links to docker images:

- [nosqlbench/nosqlbench:latest](https://hub.docker.com/r/nosqlbench/nosqlbench/tags?page=1&name=latest)
- [All tagged docker images](https://hub.docker.com/r/nosqlbench/nosqlbench/tags)

## Maven

The latest release of NoSQLBench is always available fro Maven Central.
You can find the [latest version with Maven central search](https://search.maven.org/search?q=g:io.nosqlbench).

## Additional Plans

- Docker -We have a docker build in the works. Stay tuned for the details.
- Github artifacts - We are planning to add this as another channel. Stay tuned.
