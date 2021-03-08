#!/bin/sh

set -e
SLEEP_DURATION=180
echo "====Sleeping for ${SLEEP_DURATION} seconds===="
sleep ${SLEEP_DURATION}
echo "====Running sbt test====="
sbt test