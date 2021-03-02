#!/bin/bash

set -e

$hadoop_path fs -fs hdfs://$hdfs_server -mkdir -p /tw/rawData/stationInformation/checkpoints \
&& $hadoop_path fs -fs hdfs://$hdfs_server -mkdir -p /tw/rawData/stationInformation/data \
&& $hadoop_path fs -fs hdfs://$hdfs_server -mkdir -p /tw/rawData/stationStatus/checkpoints \
&& $hadoop_path fs -fs hdfs://$hdfs_server -mkdir -p /tw/rawData/stationStatus/data \
&& $hadoop_path fs -fs hdfs://$hdfs_server -mkdir -p /tw/rawData/stationDataSF/checkpoints \
&& $hadoop_path fs -fs hdfs://$hdfs_server -mkdir -p /tw/rawData/stationDataSF/data \
#TODO : following one directory is never written to
&& $hadoop_path fs -fs hdfs://$hdfs_server -mkdir -p /tw/rawData/stationDataNYC/checkpoints \
&& $hadoop_path fs -fs hdfs://$hdfs_server -mkdir -p /tw/stationMart/checkpoints \
&& $hadoop_path fs -fs hdfs://$hdfs_server -mkdir -p /tw/stationMart/data
