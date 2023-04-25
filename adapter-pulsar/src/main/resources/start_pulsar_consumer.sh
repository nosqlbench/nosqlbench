#!/usr/local/bin/bash
: "${REBUILD:=1}"
: "${CYCLES:=1000000000}"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" &>/dev/null && pwd)"
if [[ ${REBUILD} -eq 1 ]]; then
    "${SCRIPT_DIR}/build-nb-pulsar-driver.sh"
fi
java -jar nb5/target/nb5.jar \
     run \
     driver=pulsar \
     -vv \
     --report-interval 5 \
     --docker-metrics \
     cycles=${CYCLES} \
     yaml="${SCRIPT_DIR}/yaml_examples/consumer_4KB_workload.yaml" \
     config="${SCRIPT_DIR}/config.properties"
