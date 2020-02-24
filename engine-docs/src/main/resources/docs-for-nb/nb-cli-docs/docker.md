## docker metrics

### summary

Enlist nosqlbench to stand up your metrics infrastructure using a local docker runtime:

    --docker-metrics

When this option is set, nosqlbench will start graphite, prometheus, and grafana automatically
on your local docker, configure them to work together, and point nosqlbench to send metrics
the system automatically. It also imports a base dashboard for nosqlbench and configures grafana
snapshot export to share with a central DataStax grafana instance (grafana can be found on localhost:3000
with the default credentials admin/admin).

### details

If you want to know exactly what nosqlbench is doing, it's the equivalent of running the following by hand:

#### pull and run the graphite-exporter container

    docker run -d -p 9108:9108 -p 9109:9109 -p 9109:9109/udp prom/graphite-exporter

####  prometheus config

place prometheus config in .prometheus:

prometheus.yml (found in resources/docker/prometheus/prometheus.yml)


#### pull and run the prometheus container

    docker run -d -p 9090:9090 -v '<USER HOME>/.prometheus:/etc/prometheus' prom/prometheus --config.file=/etc/prometheus/prometheus.yml" --storage.tsdb.path=/prometheus" --storage.tsdb.retention=183d --web.enable-lifecycle

#### pull and run the grafana container

    docker run -d -p 3000:3000 -v grafana/grafana

with the following environment variables:

    GF_SECURITY_ADMIN_PASSWORD=admin
    GF_AUTH_ANONYMOUS_ENABLED="true"
    GF_SNAPSHOTS_EXTERNAL_SNAPSHOT_URL=http://54.165.144.56:3001
    GF_SNAPSHOTS_EXTERNAL_SNAPSHOT_NAME="Send to Wei"

#### configure grafana

use the grafana api to set up the datasource and dashboard

POST
http://localhost:3000/api/dashboards/db

Payload:
analysis.json (found in resources/docker/dashboards/analysis.json)

POST
http://localhost:3000/api/datasources

Payload:
prometheus-datasource.yaml (found in resources/docker/datasources/prometheus-datasource.yaml)

