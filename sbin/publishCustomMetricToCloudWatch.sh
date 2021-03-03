#!/usr/bin/env bash
set -ex
export AWS_DEFAULT_REGION=ap-southeast-1
SECONDS_PER_MINUTE=60
LAST_MODIFIED_DATE=$(date +%s -d $echo "$(hadoop fs -stat /tw/stationMart/data)")
CURRENT=$(date +%s)
DIFF=$(($CURRENT- $LAST_MODIFIED_DATE))
MINUTES=$((DIFF / SECONDS_PER_MINUTE ))
echo $MINUTES
aws cloudwatch put-metric-data --metric-name MonitorHDFSLocation --namespace "Custom" --value $MINUTES