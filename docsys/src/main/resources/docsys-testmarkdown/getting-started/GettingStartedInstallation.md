# Installation

DSBench is a command line utility that is packaged as a standalone executable.

#### 1. Prerequisites

- Java 8 or 11 installed on the machine that will run DSBench

#### 2. Install DSBench

DSBench is hosted on DataStax servers at the following location.

```
<url>
```

Install from the command line

```
curl <url>
```

It is a best practice to run DSBench on a separate set of machines
rather than directly on the DSE nodes to avoid resource contention.


#### 3. Confirm Installation

```
<path-to-dsbench>/dsbench --help
```