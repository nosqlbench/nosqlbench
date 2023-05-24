#bin/bash
# This is an utility script to create the store in Venice.
# Use ./download.sh in order to download the binaries needed to run this script.

set -x -e
HERE=$(dirname $0)
# move to the directory with the Schema files
cd $HERE
jar=../../../../target/venice-admin-tool-all.jar 
storeName=$1
url=http://localhost:5555
clusterName=venice-cluster0
keySchema=key.avsc
valueSchema=value.avsc

# create the store
java -jar $jar --new-store --url $url --cluster $clusterName  --store $storeName --key-schema-file $keySchema --value-schema-file $valueSchema --hybrid-data-replication-policy NON_AGGREGATE

# enable incremental push, disable read quota and set NON_AGGREGATE hybrid-data-replication-policy
java -jar $jar --update-store --url $url --cluster $clusterName  --store $storeName --storage-quota -1 --incremental-push-enabled true --hybrid-data-replication-policy NON_AGGREGATE --read-quota 1000000 --hybrid-rewind-seconds 86400 --hybrid-offset-lag 1000

# create the first version of the store
java -jar $jar --empty-push --url $url --cluster $clusterName --store $storeName --push-id init --store-size 1000
