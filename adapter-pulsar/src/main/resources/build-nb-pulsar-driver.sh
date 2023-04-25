#!/usr/local/bin/bash
: "${SKIP_TESTS:=1}"
(
  cd "$(git rev-parse --show-toplevel)" && \
    mvn clean install "-DskipTests" -pl adapters-api,adapter-pulsar,nb5 && \
    [[ ${SKIP_TESTS} -ne 1 ]] && \
    mvn test -pl adapters-api,adapter-pulsar
)
