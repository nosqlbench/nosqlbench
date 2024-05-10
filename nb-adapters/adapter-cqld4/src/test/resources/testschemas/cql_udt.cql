CREATE KEYSPACE baselines
            WITH REPLICATION = {
            'class' : 'SimpleStrategy',
            'replication_factor' : 1
            };

CREATE TYPE baselines.phone (
    country_code int,
    number text,
);

CREATE TYPE baselines.address (
    street text,
    city text,
    zip text,
    phones map<text, phone>
);

CREATE TABLE baselines.user (
    name text PRIMARY KEY,
    addresses map<text, frozen<address>>
);

ALTER TYPE baselines.address RENAME zip TO zipcode AND city to city_code;
ALTER TYPE baselines.address ADD country text;

DROP TYPE IF EXISTS baselines.address;
