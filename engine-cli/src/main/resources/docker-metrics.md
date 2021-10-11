# docker-metrics

Enlist nosqlbench to stand up your metrics infrastructure using a local
docker runtime:

    --docker-metrics

When this option is set, nosqlbench will start graphite, prometheus,
and grafana dockers (if-needed) automatically on your local system
, configure them to work together, and point nosqlbench to send metrics
and annotations to the system automatically.

The included NoSQLBench dashboard uses the default grafana credentials of
 admin:admin. You can find this dashboard by browsing to the "manage
  dashboards" section of grafana.

# remote docker-metrics

It is possible to use `--docker-metrics` to set up a metrics collector
stack on one system and use it from multiple other systems. In order
to point client system at the collector, use an option like this:

    --docker-metrics-at 192.168.192.168

This will configure graphite and grafana annotations to point to
the docker stack at the configured address.

Further, if you want to do one-time configuration on the collector node
and other nodes, you can use this pattern:

    # on the collector node
    ... --pin --docker-metrics

    # on other nodes
    ... --pin --docker-metrics-at <collector node ip>

This causes these options to be configured by default in an argsfile
at `$HOME/.nosqlbench/argsfile`. The options above are pinned to
be included by default in every command run from that point forward.

## Docker Details

If you want to know exactly what nosqlbench is doing, it's the equivalent
of running the following by hand:

    # pull and run the graphite-exporter container
    docker run -d -p 9108:9108 -p 9109:9109 -p 9109:9109/udp prom/graphite-exporter

Configuration files which are used by the docker containers are stored in:

    $HOME/.nosqlbench

## Resetting docker state

If you need to clear the state for a local docker metrics stack, you
 can remove these directories.

    # DASHBOARDS AND METRICS WILL BE LOST IF YOU DO THIS
    rm ~/.nosqlbench/{grafana,prometheus,prometheus-conf,graphite-exporter}

## Manually installing dockers

    # pull and run the prometheus container
    docker run -d -p 9090:9090 -v '<USER HOME>/.prometheus:/etc/prometheus' prom/prometheus --config.file=/etc/prometheus/prometheus.yml" --storage.tsdb.path=/prometheus" --storage.tsdb.retention=183d --web.enable-lifecycle

    # pull and run the grafana container
    docker run -d -p 3000:3000 -v grafana/grafana

## Experimental environment variables

These may allow you to send snapshot data to a specially configured
remote grafana instance.

    GF_SECURITY_ADMIN_PASSWORD=admin
    GF_AUTH_ANONYMOUS_ENABLED="true"
    GF_SNAPSHOTS_EXTERNAL_SNAPSHOT_URL=http://54.165.144.56:3001
    GF_SNAPSHOTS_EXTERNAL_SNAPSHOT_NAME="Send to Wei"

## Configuration Endpoints (Experimental)

You can use the grafana api to set up the datasource and dashboard
if you have other tools which integrate with grafana:

    # These are not commands, they are only provides API parameters

    POST http://localhost:3000/api/dashboards/db
    analysis.json
    # (found in resources/docker/dashboards/analysis.json)

    POST http://localhost:3000/api/datasources
    prometheus-datasource.yaml
    # (found in resources/docker/datasources/prometheus-datasource.yaml)

