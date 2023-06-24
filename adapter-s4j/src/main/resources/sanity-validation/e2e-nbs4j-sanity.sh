#! /usr/local/bin/bash

CUR_SCRIPT_FOLDER=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
source "${CUR_SCRIPT_FOLDER}/utilities.sh"

echo

##
# Show usage info
#
usage() {
   echo
   echo "Usage: e2e-nbs4j-sanity.sh [-h]"
   echo "                           [-c </path/to/client.conf>]"
   echo "       -h : Show usage info"
   echo "       -c : (Optional) Pulsar cluster connection file. Default to 'client.conf' in the same directory!"
   echo
}

if [[ $# -gt 3 ]]; then
   usage
   errExit 10 "Incorrect input parameter count!"
fi

pulsarClientConf="./client.conf"
while [[ "$#" -gt 0 ]]; do
    case $1 in
      -h) usage; exit 0 ;;
      -c) pulsarClientConf=$2; shift ;;
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
sanityTestMainLogFile="${mainLogDir}/e2e-nbs4j-sanity-${startTime2}.log"
echo > "${sanityTestMainLogFile}"

debugMsg "pulsarClientConf=${pulsarClientConf}" "${sanityTestMainLogFile}"
if ! [[ -f "${pulsarClientConf}" ]]; then
    errExit 30 \
      "Can't find the Pulsar cluster client.conf file at the specified location: \"" + pulsarClientConf + "\"!" \
      "${sanityTestMainLogFile}"
fi

brokerSvcUrl=$(getPropVal ${pulsarClientConf} "brokerServiceUrl")
webSvcUrl=$(getPropVal ${pulsarClientConf} "webServiceUrl")
authPlugin=$(getPropVal ${pulsarClientConf} "authPlugin")
authParams=$(getPropVal ${pulsarClientConf} "authParams")
debugMsg "brokerSvcUrl=${brokerSvcUrl}" "${sanityTestMainLogFile}"
debugMsg "webSvcUrl=${webSvcUrl}" "${sanityTestMainLogFile}"
debugMsg "authPlugin=${authPlugin}" "${sanityTestMainLogFile}"
debugMsg "authParams=xxxxxx" "${sanityTestMainLogFile}"

sanityS4jCfgPropFileName="e2e-sanity-config.properties"
sanityS4jCfgPropFile="${CUR_SCRIPT_FOLDER}/${sanityS4jCfgPropFileName}"
cp -rf "${CUR_SCRIPT_FOLDER}/${sanityS4jCfgPropFileName}.tmpl" "${sanityS4jCfgPropFile}"
if [[ -n "${authPlugin// }" || -n "${authParams// }" ]]; then
    replaceStringInFile "<authPlugin_tmpl>" "${authPlugin}" "${sanityS4jCfgPropFileName}"
    replaceStringInFile "<authParams_tmpl>" "${authParams}" "${sanityS4jCfgPropFileName}"
else
    replaceStringInFile "<authPlugin_tmpl>" "" "${sanityS4jCfgPropFileName}"
    replaceStringInFile "<authParams_tmpl>" "" "${sanityS4jCfgPropFileName}"
fi

NB5JAR="${CUR_SCRIPT_FOLDER}/../../../../../nb5/target/nb5.jar"
sanityS4jMsgSenderYamlFile="${CUR_SCRIPT_FOLDER}/sanity-msg-sender-queue.yaml"
sanityS4jMsgReceiverYamlFile="${CUR_SCRIPT_FOLDER}/sanity-msg-receiver-queue.yaml"


{
  echo;
  echo "======================================================================================================";
  echo "Starting the sanity test for the NoSQLBench S4J adapter at ${startTime} ...";
  echo;
  echo "  >>> Kick off an S4J message sending workload ..."
  echo;
} >> "${sanityTestMainLogFile}"

read -r -d '' nbs4jMsgSendCmd << EOM
java -jar ${NB5JAR} run driver=s4j -vv --logs-dir=${nbExecLogDir} \
    cycles=1000 threads=4 num_conn=2 num_session=2 \
    session_mode=\"client_ack\" strict_msg_error_handling=\"false\" \
    service_url=${brokerSvcUrl} \
    web_url=${webSvcUrl} \
    config=${sanityS4jCfgPropFile} \
    yaml=${sanityS4jMsgSenderYamlFile}
EOM
debugMsg "nbs4jMsgSendCmd=${nbs4jMsgSendCmd}" "${sanityTestMainLogFile}"

eval '${nbs4jMsgSendCmd}'
if [[ $? -ne 0 ]]; then
    errExit 40 "Failed to kick off the S4J message sending workload!" "${sanityTestMainLogFile}"
fi

# pause 5 seconds before kicking off the message sending workload
sleep 5

{
  echo;
  echo "  >>> Kick off an S4J message receiving workload after 30 seconds..."
  echo;
} >> "${sanityTestMainLogFile}"

read -r -d '' nbs4jMsgRecvCmd << EOM
java -jar ${NB5JAR} run driver=s4j -vv --logs-dir=${nbExecLogDir} \
  cycles=1000 threads=4 num_conn=2 num_session=2 \
  session_mode=\"client_ack\" strict_msg_error_handling=\"false\" \
  service_url=${brokerSvcUrl} \
  web_url=${webSvcUrl} \
  config=${sanityS4jCfgPropFile} \
  yaml=${sanityS4jMsgReceiverYamlFile}
EOM
debugMsg "nbs4jMsgRecvCmd=${nbs4jMsgRecvCmd}"  "${sanityTestMainLogFile}"

eval '${nbs4jMsgRecvCmd}'
if [[ $? -ne 0 ]]; then
    errExit 40 "Failed to kick off the S4J message receiving workload!" "${sanityTestMainLogFile}"
fi

echo "NB S4J workload sanity check passed!" >> "${sanityTestMainLogFile}"


echo
