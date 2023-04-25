#!/usr/local/bin/bash
: "${REBUILD:=1}"
: "${CYCLES:=1000000000}"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" &>/dev/null && pwd)"
if [[ ${REBUILD} -eq 1 ]]; then
    "${SCRIPT_DIR}/build-nb-kafka-driver.sh"
fi
while [[ 1 -eq 1 ]]; do
  java -jar nb5/target/nb5.jar \
       run \
       driver=kafka \
       -vv \
       --report-interval 5 \
       --docker-metrics \
       cycles="${CYCLES}" \
       threads=1 \
       num_clnt=1 \
       yaml="${SCRIPT_DIR}/kafka_producer.yaml" \
       config="${SCRIPT_DIR}/kafka_config.properties" \
       bootstrap_server=PLAINTEXT://localhost:9092
  sleep 10
done
