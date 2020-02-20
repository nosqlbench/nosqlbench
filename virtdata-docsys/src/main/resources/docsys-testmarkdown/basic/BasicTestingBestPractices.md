# Testing Best Practices

It's no secret that distributed systems are difficult. This is a primary reason why sound testing and simulation of expected load
on the system is so important to do before running an application in production. Here we discuss a few of the best practices to follow
when using DSBench but also for testing a distributed database such as DataStax Distribution of Apache Cassandra and DataStax Enterprise.

### Topology

See existing doc for inspiration
https://powertools.datastax.com/ebdse/advanced-topics/topology/

- Run clients on separate machines from database
- Run performance monitoring on separate machines from client and database
- etc.

### Client Saturation

Make sure client is not bottle neck

### Testing Phases

Base data set first, read / write workload

### Database saturation

Don't crush cluster, how to find saturation point and then scale back

...