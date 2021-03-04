#!/usr/bin/env bash

set -xe

usage() {
    echo "Deployment script "
    echo "./deply.sh BASTION_PUBLIC_IP TRAINING_COHORT"
}

if [ $# -eq 0 ]; then
    usage
    exit 1
fi

BASTION_PUBLIC_IP=$1
TRAINING_COHORT=$2

echo "====Updating SSH Config===="

echo "
	User ec2-user
	IdentitiesOnly yes
	ForwardAgent yes
	DynamicForward 6789
    StrictHostKeyChecking no
    UserKnownHostsFile /dev/null

Host emr-master.${TRAINING_COHORT}.training
    User hadoop

Host *.${TRAINING_COHORT}.training !bastion.${TRAINING_COHORT}.training
	ForwardAgent yes
	ProxyCommand ssh -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null ec2-user@${BASTION_PUBLIC_IP} -W %h:%p 2>/dev/null
	User ec2-user
    StrictHostKeyChecking no
    UserKnownHostsFile /dev/null

Host bastion.${TRAINING_COHORT}.training
    User ec2-user
    HostName ${BASTION_PUBLIC_IP}
    DynamicForward 6789
" >> ~/.ssh/config

echo "====SSH Config Updated===="

echo "====Insert app config in zookeeper===="
scp ./zookeeper/seed.sh kafka.${TRAINING_COHORT}.training:/tmp/zookeeper-seed.sh
ssh kafka.${TRAINING_COHORT}.training <<EOF
set -e
export hdfs_server="emr-master.${TRAINING_COHORT}.training:8020"
export kafka_server="kafka.${TRAINING_COHORT}.training:9092"
export zk_command="zookeeper-shell localhost:2181"
sh /tmp/zookeeper-seed.sh
EOF

echo "====Inserted app config in zookeeper===="

echo "====Copy jar to ingester server===="
scp CitibikeApiProducer/build/libs/tw-citibike-apis-producer0.1.0.jar ingester.${TRAINING_COHORT}.training:/tmp/
echo "====Jar copied to ingester server===="

ssh ingester.${TRAINING_COHORT}.training <<EOF
set -e

 function kill_process {
    pid=\$(pgrep -f "java .*tw-citibike-apis-producer0.1.0.jar")
    for i in "\${pid[@]}"
    do
       echo \$1
       kill -9 \$i
    done

}

station_information="station-information"
station_status="station-status"
station_san_francisco="station-san-francisco"
station_marseille="station-marseille"


echo "====Kill running producers===="

kill_process

echo "====Runing Producers Killed===="

echo "====Deploy Producers===="
nohup java -jar /tmp/tw-citibike-apis-producer0.1.0.jar --spring.profiles.active=\${station_information} --kafka.brokers=kafka.${TRAINING_COHORT}.training:9092 1>/tmp/\${station_information}.log 2>/tmp/\${station_information}.error.log &
nohup java -jar /tmp/tw-citibike-apis-producer0.1.0.jar --spring.profiles.active=\${station_san_francisco} --producer.topic=station_data_sf --kafka.brokers=kafka.${TRAINING_COHORT}.training:9092 1>/tmp/\${station_san_francisco}.log 2>/tmp/\${station_san_francisco}.error.log &
nohup java -jar /tmp/tw-citibike-apis-producer0.1.0.jar --spring.profiles.active=\${station_status} --kafka.brokers=kafka.${TRAINING_COHORT}.training:9092 1>/tmp/\${station_status}.log 2>/tmp/\${station_status}.error.log &
nohup java -jar /tmp/tw-citibike-apis-producer0.1.0.jar --spring.profiles.active=\${station_marseille} --kafka.brokers=kafka.${TRAINING_COHORT}.training:9092 1>/tmp/\${station_marseille}.log 2>/tmp/\${station_marseille}.error.log &

echo "====Producers Deployed===="
EOF


echo "====Configure HDFS paths===="
scp ./hdfs/seed.sh emr-master.${TRAINING_COHORT}.training:/tmp/hdfs-seed.sh

ssh emr-master.${TRAINING_COHORT}.training <<EOF
set -e
export hdfs_server="emr-master.${TRAINING_COHORT}.training:8020"
export hadoop_path="hadoop"
sh /tmp/hdfs-seed.sh
EOF

echo "====HDFS paths configured==="

echo "====Config Custom metric publisher ===="
scp ./sbin/publishCustomMetricToCloudWatch.sh emr-master.${TRAINING_COHORT}.training:/tmp/publishCustomMetricToCloudWatch.sh

ssh emr-master.${TRAINING_COHORT}.training <<EOF
chmod +x /tmp/publishCustomMetricToCloudWatch.sh
echo "*/5 * * * * /tmp/publishCustomMetricToCloudWatch.sh  --from-cron" | crontab
EOF

echo "==== Custom metric publisher configured ==="


echo "====Copy Raw Data Saver Jar to EMR===="
scp RawDataSaver/target/scala-2.11/tw-raw-data-saver_2.11-0.0.1.jar emr-master.${TRAINING_COHORT}.training:/tmp/
echo "====Raw Data Saver Jar Copied to EMR===="

scp sbin/go.sh emr-master.${TRAINING_COHORT}.training:/tmp/go.sh

ssh emr-master.${TRAINING_COHORT}.training <<EOF
set -e

source /tmp/go.sh

echo "====Kill Old Raw Data Saver===="

kill_application "StationStatusSaverApp"
kill_application "StationInformationSaverApp"
kill_application "StationDataSFSaverApp"
kill_application "StationDataMarseilleSaverApp"

echo "====Old Raw Data Saver Killed===="

echo "====Deploy Raw Data Saver===="

nohup spark-submit --master yarn --deploy-mode cluster --class com.tw.apps.StationLocationApp --name StationDataMarseilleSaverApp --packages org.apache.spark:spark-sql-kafka-0-10_2.11:2.3.0 --driver-memory 500M --conf spark.sql.streaming.minBatchesToRetain=2 --conf spark.sql.shuffle.partitions=10 --conf spark.executor.memory=1g  --conf spark.cores.max=1 /tmp/tw-raw-data-saver_2.11-0.0.1.jar kafka.${TRAINING_COHORT}.training:2181 "/tw/stationDataMarseille" 1>/tmp/raw-station-marseille-data-saver.log 2>/tmp/raw-station-marseille-data-saver.error.log &

nohup spark-submit --master yarn --deploy-mode cluster --class com.tw.apps.StationLocationApp --name StationStatusSaverApp --packages org.apache.spark:spark-sql-kafka-0-10_2.11:2.3.0 --driver-memory 500M --conf spark.sql.streaming.minBatchesToRetain=2 --conf spark.sql.shuffle.partitions=10 --conf spark.executor.memory=1g  --conf spark.cores.max=1 /tmp/tw-raw-data-saver_2.11-0.0.1.jar kafka.${TRAINING_COHORT}.training:2181 "/tw/stationStatus" 1>/tmp/raw-station-status-data-saver.log 2>/tmp/raw-station-status-data-saver.error.log &

nohup spark-submit --master yarn --deploy-mode cluster --class com.tw.apps.StationLocationApp --name StationInformationSaverApp --packages org.apache.spark:spark-sql-kafka-0-10_2.11:2.3.0 --driver-memory 500M --conf spark.sql.streaming.minBatchesToRetain=2 --conf spark.sql.shuffle.partitions=10 --conf spark.executor.memory=1g --conf spark.cores.max=1 /tmp/tw-raw-data-saver_2.11-0.0.1.jar kafka.${TRAINING_COHORT}.training:2181 "/tw/stationInformation" 1>/tmp/raw-station-information-data-saver.log 2>/tmp/raw-station-information-data-saver.error.log &

nohup spark-submit --master yarn --deploy-mode cluster --class com.tw.apps.StationLocationApp --name StationDataSFSaverApp --packages org.apache.spark:spark-sql-kafka-0-10_2.11:2.3.0 --driver-memory 500M --conf spark.sql.streaming.minBatchesToRetain=2 --conf spark.sql.shuffle.partitions=10 --conf spark.executor.memory=1g --conf spark.cores.max=1 /tmp/tw-raw-data-saver_2.11-0.0.1.jar kafka.${TRAINING_COHORT}.training:2181 "/tw/stationDataSF" 1>/tmp/raw-station-data-sf-saver.log 2>/tmp/raw-station-data-sf-saver.error.log &

echo "====Raw Data Saver Deployed===="
EOF


echo "====Copy Station Consumers Jar to EMR===="
scp StationConsumer/target/scala-2.11/tw-station-consumer_2.11-0.0.1.jar emr-master.${TRAINING_COHORT}.training:/tmp/

scp StationTransformerNYC/target/scala-2.11/tw-station-transformer-nyc_2.11-0.0.1.jar emr-master.${TRAINING_COHORT}.training:/tmp/
echo "====Station Consumers Jar Copied to EMR===="

scp sbin/go.sh emr-master.${TRAINING_COHORT}.training:/tmp/go.sh

ssh emr-master.${TRAINING_COHORT}.training <<EOF
set -e

source /tmp/go.sh


echo "====Kill Old Station Consumers===="

kill_application "StationApp"
kill_application "StationTransformerNYC"

echo "====Old Station Consumers Killed===="

echo "====Deploy Station Consumers===="

nohup spark-submit --master yarn --deploy-mode cluster --class com.tw.apps.StationApp --name StationApp --packages org.apache.spark:spark-sql-kafka-0-10_2.11:2.3.0 --driver-memory 500M --conf spark.executor.memory=1g  --conf spark.sql.streaming.minBatchesToRetain=2 --conf spark.sql.shuffle.partitions=10 --conf spark.cores.max=1 /tmp/tw-station-consumer_2.11-0.0.1.jar kafka.${TRAINING_COHORT}.training:2181 1>/tmp/station-consumer.log 2>/tmp/station-consumer.error.log &

nohup spark-submit --master yarn --deploy-mode cluster --class com.tw.apps.StationApp --name StationTransformerNYC --packages org.apache.spark:spark-sql-kafka-0-10_2.11:2.3.0 --driver-memory 500M --conf spark.executor.memory=1g  --conf spark.sql.streaming.minBatchesToRetain=2 --conf spark.sql.shuffle.partitions=10  --conf spark.cores.max=1 /tmp/tw-station-transformer-nyc_2.11-0.0.1.jar kafka.${TRAINING_COHORT}.training:2181 1>/tmp/station-transformer-nyc.log 2>/tmp/station-transformer-nyc.error.log &

echo "====Station Consumers Deployed===="
EOF
