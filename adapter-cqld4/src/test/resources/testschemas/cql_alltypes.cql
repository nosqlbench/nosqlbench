/*
 * Copyright (c) 2022 nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

-- CREATE KEYSPACE baselines
--  WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '1'}  AND durable_writes = true;

CREATE KEYSPACE baselines
            WITH REPLICATION = {
            'class' : 'SimpleStrategy',
            'replication_factor' : 1
            };

CREATE TABLE baselines.alltypes
(
    id          text PRIMARY KEY,
    a_bigint    bigint,
    a_blob      blob,
    a_boolean   boolean,
    a_date      date,
    a_decimal   decimal,
    a_double    double,
    a_duration  duration,
    a_float     float,
    a_frozen    frozen<list< int >>,
    a_list      list<text>,
    a_map       map<text,text>,
    a_set       set<text>,
    a_smallint  smallint,
    a_text      text,
    a_time      time,
    a_timestamp timestamp,
    a_timeuuid  timeuuid,
    a_tinyint   tinyint,
    a_uuid      uuid,
    a_varchar   text,
    a_varint    varint,
    an_ascii    ascii,
    an_inet     inet,
    an_int      int
) WITH additional_write_policy = '99PERCENTILE'
      AND bloom_filter_fp_chance = 0.01
      AND caching = {'keys': 'ALL', 'rows_per_partition': 'NONE'}
      AND comment = ''
      AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy', 'max_threshold': '32', 'min_threshold': '4'}
      AND compression = {'chunk_length_in_kb': '64', 'class': 'org.apache.cassandra.io.compress.LZ4Compressor'}
      AND crc_check_chance = 1.0
      AND default_time_to_live = 0
      AND gc_grace_seconds = 864000
      AND max_index_interval = 2048
      AND memtable_flush_period_in_ms = 0
      AND min_index_interval = 128
      AND nodesync = {'enabled': 'true', 'incremental': 'true'}
      AND read_repair = 'BLOCKING'
      AND speculative_retry = '99PERCENTILE';

