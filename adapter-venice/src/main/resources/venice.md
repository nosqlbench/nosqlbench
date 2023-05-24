---
weight: 0
title: VeniceDB
---
# 1. Overview

Configuration options:
- router_url: the address of the Venice Router service (default: http://localhost:7777)
- store_name: the name of the store to use (default: store1)
- token: the token to use for authentication (default: none)

# 2. Sample commands

You can run Venice using the Venice Standalone package:

```bash
git clone https://github.com/datastax/venice
cd venice
./gradlew :services:venice-standalone:run
```

Then you create a "store" using the scripts in the `adapter-venice/src/main/resources` directory:

```bash
cd adapter-venice/src/main/resources/scripts
./dowload.sh
./create-store.sh store1
```

The script creates a Venice store with the given Key and Value schemas defined in the key.avsc and value.avsc files.
Please ensure that you set the same schemas on your workload configuration files (keySchema and valueSchema).

Then you can populate the store with some data using NB.

Open a new terminal and run:

```bash
java -jar nb5/target/nb5.jar run driver=venice  workload=adapter-venice/src/main/resources/venice_writer.yaml store_name=store1 router_url=http://localhost:7777 cycles=100000 -v --report-summary-to stdout:60  --report-csv-to reports
```

And you can read the data back using NB as well:

```bash
java -jar nb5/target/nb5.jar run driver=venice  workload=adapter-venice/src/main/resources/venice_reader.yaml store_name=store1 router_url=http://localhost:7777 cycles=100000 -v --report-summary-to stdout:60  --report-csv-to reports
```
