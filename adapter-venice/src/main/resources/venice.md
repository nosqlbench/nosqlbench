---
weight: 0
title: VeniceDB
---
# 1. Overview

Configuration options:
- router_url: the address of the Venice Router service (default: http://localhost:7777)
- store_name: the name of the store to use (default: store1)
- token: the token to use for authentication (default: none)

# 2. Sample command

java -jar nb5/target/nb5.jar run driver=venice  workload=adapter-venice/src/main/resources/venice_reader.yaml store_name=store1 router_url=http://localhost:7777 cycles=100 -v
