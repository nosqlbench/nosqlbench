#bin/bash
# This is an utility script to create the store in Venice.
# Use ./download.sh in order to download the binaries needed to run this script.

set -x -e
HERE=$(realpath $0)
cd $HERE

jar=binaries/*admin-tool-all*.jar
storeName=$1
url=http://localhost:5555
clusterName=venice-cluster0
keySchema=key.avsc
valueSchema=value.avsc

# create the store
java -jar $jar --new-store --url $url --cluster $clusterName  --store $storeName --key-schema-file $keySchema --value-schema-file $valueSchema --hybrid-data-replication-policy NON_AGGREGATE


# enable incremental push and disable read quota
java -jar $jar --update-store --url $url --cluster $clusterName  --store $storeName --storage-quota -1 --incremental-push-enabled true --hybrid-data-replication-policy NON_AGGREGATE --read-quota 1000000

# create the first version of the store
java -jar $jar --empty-push --url $url --cluster $clusterName --store $storeName --push-id init --store-size 1000
