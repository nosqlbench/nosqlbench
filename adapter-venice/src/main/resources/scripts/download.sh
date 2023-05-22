##########
# This script downloads the binaries needed to run the create-store.sh script.
##########
set -x -e
HERE=$(realpath $(dirname $0))
VENICETOOLSURL=https://github.com/datastax/venice/releases/download/ds-0.4.17-alpha-12/venice-admin-tool-all.jar
BINDIR=$HERE/binaries
rm -Rf $BINDIR
mkdir $BINDIR
pushd $BINDIR
cd $BINDIR
curl -L -O $VENICETOOLSURL
popd





