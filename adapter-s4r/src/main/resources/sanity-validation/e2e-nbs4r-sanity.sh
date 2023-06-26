#! /usr/local/bin/bash

##
#  Copyright (c) 2022-2023 nosqlbench
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
##

CUR_SCRIPT_FOLDER=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
source "${CUR_SCRIPT_FOLDER}/utilities.sh"

echo

##
# Show usage info
#
usage() {
   echo
   echo "Usage: e2e-nbs4r-sanity.sh [-h]"
   echo "                           -j </full/path/to/jwt/token/file>]"
   echo "       -h : Show usage info"
   echo "       -j : JWT token file full path. Default to 'jwt.token' in the same directory!"
   echo
}

if [[ $# -gt 3 ]]; then
   usage
   errExit 10 "Incorrect input parameter count!"
fi

jwtTokenFile="./jwt.token"
while [[ "$#" -gt 0 ]]; do
    case $1 in
      -h) usage; exit 0 ;;
      -j) jwtTokenFile=$2; shift ;;
      *) errExit 20 "Unknown input parameter passed: $1"; ;;
    esac
    shift
done

mkdir -p "./logs/nb5-exec"
mainLogDir="${CUR_SCRIPT_FOLDER}/logs"
nbExecLogDir="${mainLogDir}/nb5-exec"

# 2022-08-19 11:40:23
startTime=$(date +'%Y-%m-%d %T')
# 20220819114023
startTime2=${startTime//[: -]/}
sanityTestMainLogFile="${mainLogDir}/e2e-nbs4r-sanity-${startTime2}.log"
echo > "${sanityTestMainLogFile}"

debugMsg "jwtTokenFile=${jwtTokenFile}" "${sanityTestMainLogFile}"
if ! [[ -f "${jwtTokenFile}" ]]; then
    errExit 30 \
      "Can't find the required JWT token file (for Pulsar cluster connection) at the specified location: \"" +
      jwtTokenFile + "\"!" \
      "${sanityTestMainLogFile}"
fi

sanityS4rCfgPropFileName="e2e-sanity-config.properties"
sanityS4rCfgPropFile="${CUR_SCRIPT_FOLDER}/${sanityS4rCfgPropFileName}"
if [[ ! -f "${CUR_SCRIPT_FOLDER}/${sanityS4rCfgPropFileName}.tmpl" ]]; then
    errExit 30 \
      "Can't find the required sanity test config file template at the specified location: \"" +
      "${CUR_SCRIPT_FOLDER}/${sanityS4rCfgPropFileName}.tmpl" + "\"!" \
      "${sanityTestMainLogFile}"
fi

cp -rf "${CUR_SCRIPT_FOLDER}/${sanityS4rCfgPropFileName}.tmpl" "${sanityS4rCfgPropFile}"
if [[ -n "${jwtTokenFile// }" ]]; then
    replaceStringInFile "<jwt_token_file_tmpl>" "file://${jwtTokenFile}" "${sanityS4rCfgPropFile}"
else
    replaceStringInFile "<jwt_token_file_tmpl>" "" "${sanityS4rCfgPropFile}"
fi

NB5JAR="${CUR_SCRIPT_FOLDER}/../../../../../nb5/target/nb5.jar"
sanityS4rSenderYamlFile="${CUR_SCRIPT_FOLDER}/e2e-sanity-sender.yaml"
sanityS4rReceiverYamlFile="${CUR_SCRIPT_FOLDER}/e2e-sanity-receiver.yaml"

{
  echo;
  echo "======================================================================================================";
  echo "Starting the sanity test for the NoSQLBench S4J adapter at ${startTime} ...";
  echo;
  echo "  >>> Kick off an S4R message sending workload ..."
  echo;
} >> "${sanityTestMainLogFile}"

read -r -d '' nbs4rMsgSendCmd << EOM
java -jar ${NB5JAR} run driver=s4r -vv --logs-dir=${nbExecLogDir} strict_msg_error_handling=\"false\" \
    cycles=1000 threads=8 num_conn=1 num_channel=2 num_exchange=2 num_msg_clnt=2 \
    config=${sanityS4rCfgPropFile} \
    workload=${sanityS4rSenderYamlFile}
EOM
debugMsg "nbs4rMsgSendCmd=${nbs4rMsgSendCmd}" "${sanityTestMainLogFile}"

eval '${nbs4rMsgSendCmd}'
if [[ $? -ne 0 ]]; then
    errExit 40 "Failed to kick off the S4R message sending workload!" "${sanityTestMainLogFile}"
fi

# pause 5 seconds before kicking off the message sending workload
sleep 5

{
  echo;
  echo "  >>> Kick off an S4J message receiving workload after 30 seconds..."
  echo;
} >> "${sanityTestMainLogFile}"

read -r -d '' nbs4rMsgRecvCmd << EOM
java -jar ${NB5JAR} run driver=s4r -vv --logs-dir=${nbExecLogDir} \
  cycles=1000 threads=16 num_conn=1 num_channel=2 num_exchange=2 num_queue=2 num_msg_clnt=2\
  config=${sanityS4rCfgPropFile} \
  workload=${sanityS4rReceiverYamlFile}
EOM
debugMsg "nbs4rMsgRecvCmd=${nbs4rMsgRecvCmd}"  "${sanityTestMainLogFile}"

eval '${nbs4rMsgRecvCmd}'
if [[ $? -ne 0 ]]; then
    errExit 40 "Failed to kick off the S4R message receiving workload!" "${sanityTestMainLogFile}"
fi

echo "NB S4J workload sanity check passed!" >> "${sanityTestMainLogFile}"


echo
