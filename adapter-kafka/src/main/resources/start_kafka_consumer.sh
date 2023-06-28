#!/usr/local/bin/bash
#
# Copyright (c) 2023 nosqlbench
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

: "${REBUILD:=1}"
: "${CYCLES:=1000000000}"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" &>/dev/null && pwd)"
if [[ ${REBUILD} -eq 1 ]]; then
    "${SCRIPT_DIR}/build-nb-kafka-driver.sh"
fi
java -jar nb5/target/nb5.jar \
     run \
     driver=kafka \
     -vv \
     --report-interval 5 \
     --docker-metrics \
     cycles=${CYCLES} \
     threads=1 \
     num_clnt=1 \
     num_cons_grp=1 \
     yaml="${SCRIPT_DIR}/scenarios/kafka_consumer.yaml" \
     config="${SCRIPT_DIR}/conf/kafka_config.properties" \
     bootstrap_server=PLAINTEXT://localhost:9092
