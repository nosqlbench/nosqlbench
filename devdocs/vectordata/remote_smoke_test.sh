#!/bin/bash
# Smoke-test remote vectordata access for one dataset profile.
#
# Required:
#   VECTORDATA_CATALOG   hosted catalog URL
#   DATASET              '<dataset>' or '<dataset>:<profile>' (profile
#                        defaults to 'default')
# Optional:
#   NB                   how to invoke nosqlbench (default: nb5), e.g.
#                        NB='java -jar nb5.jar'
#
# Usage:
#   VECTORDATA_CATALOG='https://your.host/path/catalog.yaml' \
#     DATASET=mydataset:myprofile bash remote_smoke_test.sh
#
# Checks base, query, and neighbor-index facets by eagerly warming
# exactly the records each check reads — the same chunks demand paging
# would fetch, cheap even against a billion-record facet, but with the
# plan announced and download progress emitted on stderr while the
# bytes move.

set -e    # abort on the first failed check

: "${VECTORDATA_CATALOG:?set VECTORDATA_CATALOG to your hosted catalog URL}"
: "${DATASET:?set DATASET to '<dataset>' or '<dataset>:<profile>'}"
case "$DATASET" in
  *:*) PROFILE="${DATASET#*:}"; DATASET="${DATASET%%:*}" ;;
  *)   PROFILE=default ;;
esac
NB="${NB:-nb5}"                       # or e.g. NB='java -jar nb5.jar'
export VECTORDATA_HOME="${VECTORDATA_HOME:-/tmp/vdtest}"   # isolated config+cache boundary

check() { ${NB} run driver=stdout cycles=3 threads=1 format=readout \
  "op={{$1('${DATASET}:${PROFILE}','[0..3)','eager');Stringify()}}"; }

check BaseVectors        # base_vectors, clipped to the profile window
check QueryVectors       # query_vectors, inherited from default in sized profiles
check NeighborIndices    # neighbor_indices for this profile

echo "ok: ${DATASET}:${PROFILE} base, query, and neighbor indices read via ${VECTORDATA_CATALOG}"
