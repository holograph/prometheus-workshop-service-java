#!/bin/bash
set -e

export DEBIAN_FRONTEND=noninteractive
OPENTELEMETRY_JAVA_AGENT_VERSION=2.15.0

echo '- Installing JDK'
sudo apt-get install -y openjdk-17-jdk

echo '- Installing Maven'
sudo apt-get install -y maven

echo '- Setting up workshop service'
git clone https://github.com/holograph/prometheus-workshop-service-java.git
cd ~/prometheus-workshop-service-java
mvn install

echo '- Installing OpenTelemetry Java agent'
cd
wget "https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v${OPENTELEMETRY_JAVA_AGENT_VERSION}/opentelemetry-javaagent.jar"

echo '- Installing systemd service for workshop service'
cat <<EOF | sudo tee /etc/systemd/system/workshop-service.service
[Unit]
Description=Prometheus workshop service
Wants=network-online.target
After=network-online.target

[Service]
User=ubuntu
Group=ubuntu
Type=simple
WorkingDirectory=/home/ubuntu/prometheus-workshop-service-java
ExecStart=/usr/bin/java \
  -javaagent:/home/ubuntu/opentelemetry-javaagent.jar \
  -Dotel.service.name=prometheus-workshop-service-java \
  -Dotel.logs.exporter=none \
  -Dotel.traces.exporter=none \
  -Dotel.metrics.exporter=otlp \
  -Dotel.metric.export.interval=10000 \
  -jar /home/ubuntu/prometheus-workshop-service-java/target/prometheus-workshop-service-java-0.0.1-SNAPSHOT.jar
ExecStop=/bin/kill -15 $MAINPID

SuccessExitStatus=143

[Install]
WantedBy=multi-user.target
EOF

echo '- Enabling workshop-service'
sudo systemctl daemon-reload
sudo systemctl enable workshop-service
sudo systemctl start workshop-service
