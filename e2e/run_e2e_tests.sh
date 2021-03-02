#!/usr/bin/env bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

echo "====Building Producer JARs===="
$DIR/../CitibikeApiProducer/gradlew -p $DIR/../CitibikeApiProducer clean bootJar
echo "====Building Consumer JARs===="
cd $DIR/../RawDataSaver && sbt package
cd $DIR/../StationConsumer && sbt package
cd $DIR/../StationTransformerNYC && sbt package
echo "====Running docker-compose===="
docker-compose --project-name=streamingdatapipelinee2e \
 --project-directory $DIR -f $DIR/docker-compose.yml up --build -d
#sbt test
#clean task
#docker rm -f $(docker ps -a | grep streamingdatapipelinee2e | awk '{print $1}')