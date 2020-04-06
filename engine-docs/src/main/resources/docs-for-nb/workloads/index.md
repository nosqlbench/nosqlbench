---
title: Built-In Workloads
weight: 40
---

# Built-In Workloads

There are a few built-in workloads which you may want to run. These can be run from a command without having to
configure anything, or they can be tailored with their built-in parameters.

## Finding Workloads

To find the build-in scenarios, ask NoSQLBench like this:

    nb --list-workloads

This specifically lists the workloads which provide named scenarios. Only named scenarios are included. Workloads are
contained in yaml files. If a yaml file is in the standard path and contains a root `scenarios` element, then it is
included in the listing above.

Each of these scenarios has a set of parameters which can be changed on the command line.

## Running Workloads

You can run them directly, by name with `nb <workload> [<scenario>] [<params>...]`. If not provided, scenario is assumed
to be `default`.

For example, the `cql-iot` workload is listed with the above command, and can be executed like this:

    # put your normal extra params in ... below, like hosts, for example
    nb cql-iot default ...

    # OR, with scenario name default
    nb cql-iot ...

You can add any parameters to the end, and these parameters will be passed automatically to each stage of the scenario
as needed. Within the scenario, designers have the ability to lock parameters so that overrides are used appropriately.

## Conventions

The built-in workloads follow a set of conventions so that they can be used interchangeably. This is more for users who
are using the stages of these workloads directly, or for users who are designing new scenarios to be included in the
built-ins.

### Phases

Each built-in contains the following tags that can be used to break the workload up into uniform phases:

- schema - selected with `tags=phase:schema`
- rampup - selected with `tags=phase:rampup`
- main - selected with `tags=phase:main`

### Parameters

Each built-in has a set of adjustable parameters which is documented below per workload. For example, the cql-iot
workload has a `sources` parameter which determines the number of unique devices in the dataset.

## Adding Workloads

If you want to add your own workload to NoSQLBench, or request a specific type of workload, please
[Request a workload](https://github.com/nosqlbench/nosqlbench/issues) or
[Submit a pull request](https://github.com/nosqlbench/nosqlbench/pulls).

