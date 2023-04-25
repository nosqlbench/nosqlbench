#!/usr/local/bin/bash
: "${REBUILD:=1}"
: "${CYCLES:=1000000000}"
: "${CYCLERATE:=100}"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" &>/dev/null && pwd)"
if [[ ${REBUILD} -eq 1 ]]; then
    "${SCRIPT_DIR}/build-nb-pulsar-driver.sh"
fi
while [[ 1 -eq 1 ]]; do
  java -jar nb5/target/nb5.jar \
       run \
       driver=pulsar \
       -vv \
       --report-interval 5 \
       --docker-metrics \
       cycles="${CYCLES}" \
       cyclerate="${CYCLERATE}" \
       threads=1 \
       yaml="${SCRIPT_DIR}/yaml_examples/producer_4KB_workload.yaml" \
       config="${SCRIPT_DIR}/config.properties"
  sleep 10
done
