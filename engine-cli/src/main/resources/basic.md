# for specific help topics run
    ./nb help <topic>

# for a list of available topics run
    ./nb help topics

# to see the web based docs run
    ./nb docserver

# To run a simple built-in workload run:
    ./nb  cql-iot

# To get a list of available workloads run:
    ./nb --list-workloads

# Note: this will include built-in workloads, shipped with nb and workloads in your local directory.

# To provide your own contact points (comma separated), add the hosts= parameter
    ./nb  cql-iot hosts=host1,host2

# Additionally, if you have docker installed on your local system, and your user has permissions to use it, you can use --docker-metrics to stand up a live metrics dashboard at port 3000.
    ./nb  cql-iot --docker-metrics
