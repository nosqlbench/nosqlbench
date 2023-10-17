#!/bin/bash

DATASETS="glove-25-angular glove-50-angular glove-100-angular glove-200-angular deep-image-96-angular lastfm-64-dot"

mkdir -p testdata
pushd .
cd testdata

if [ -f _env.sh ]
then . _env.sh
fi

DATASET=${DATASETS?is required}

for dataset in ${DATASETS}
do
 URL="http://ann-benchmarks.com/${dataset}.hdf5"
 curl -OL "${URL}"
done

