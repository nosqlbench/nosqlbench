#! /usr/local/bin/bash

DEBUG=false

##
# Show debug message
# - $1 : the message to show
# - $2 : (Optional) the file to log the message to
debugMsg() {
    if [[ "${DEBUG}" == "true" ]]; then
        local msg=${1}
        local file=${2}

        if [[ -z "${file}" ]]; then
            echo "[Debug] ${msg}"
        else
            echo "[Debug] ${msg}" >> "${file}"
        fi

    fi
}

##
# - $1 : the exit code
# - $2 : the message to show
# - $3 : (Optional) the file to log the message to
errExit() {
    local exitCode=${1}
    local msg=${2}
    local file=${3}

    if [[ -z "${file}" ]]; then
        echo "[Error] ${msg}"
    else
        echo "[Error] ${msg}" >> "${file}"
    fi

    exit ${exitCode}
}

##
# Read the properties file and returns the value based on the key
# 2 input parameters:
# - 1st parameter: the property file to scan
# - 2nd parameter: the key to search for
getPropVal() {
    local propFile=$1
    local searchKey=$2
    local value=$(grep "${searchKey}" ${propFile} | grep -Ev "^#|^$" | cut -d'=' -f2)
    echo $value
}

##
# Check if the sed being used is GNU sed
isGnuSed() {
    local gnu_sed=$(sed --version 2>&1 | grep -v 'illegal\|usage\|^\s' | grep "GNU sed" | wc -l)
    echo ${gnu_sed}
}


##
# Replace the occurrence of a string place holder with a specific value in a file
# Four input parameters:
# - 1st parameter: the place holder string to be replaced
# - 2nd parameter: the value string to replace the place holder
# - 3rd parameter: the file
# - 4th parameter: (Optional) a particular line identifier to replace.
#                  if specified, only replace the place holder in the matching line
#                  otherwise, replace all occurrence in the file
#
# TBD: use this function to hide GNU difference (Mac vs Linux, GNU or not)
#
replaceStringInFile() {
    local placeHolderStr=${1}
    local valueStr=${2}
    local fileToScan=${3}
    local lineIdentifier=${4}

    # in case '/' is part of the string
    placeHolderStr=$(echo ${placeHolderStr} | sed 's/\//\\\//g')
    valueStr=$(echo ${valueStr} | sed 's/\//\\\//g')

    gnuSed=$(isGnuSed)
    if [[ "$OSTYPE" == "darwin"* && ${gnuSed} -eq 0 ]]; then
        if ! [[ -z "${lineIdentifier// }" ]]; then
            sed -i '' "${lineIdentifier}s/${placeHolderStr}/${valueStr}/g" ${fileToScan}
        else
            sed -i '' "s/${placeHolderStr}/${valueStr}/g" ${fileToScan}
        fi
    else
        if ! [[ -z "${lineIdentifier// }" ]]; then
            sed -i "${lineIdentifier}s/${placeHolderStr}/${valueStr}/g" ${funcCfgJsonFileTgt}
        else
            sed -i "s/${placeHolderStr}/${valueStr}/g" ${fileToScan}
        fi
    fi
}
