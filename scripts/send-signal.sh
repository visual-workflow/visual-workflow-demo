#!/usr/bin/env bash

SIGNAL_NAME=$1
SIGNAL_PARAMS=$2

set -x

if [ "dataReceived" == "$SIGNAL_NAME" ]; then
  temporal workflow signal --name $SIGNAL_NAME --workflow-id $WFID  --input "\"$SIGNAL_PARAMS\""
else
  temporal workflow signal --name $SIGNAL_NAME --workflow-id $WFID
fi

