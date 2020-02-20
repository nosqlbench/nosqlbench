scriptingmetrics extension
==========================

This extensions allows you to create and update metrics
that are visible to nosqlbench from your scripting environment.

### Example
~~~
var mygauge= scriptingmetrics.newGauge("mygaugename", 1.0);
...
mygauge.update(5.3);
...
~~~

Any such metric will be available in the runtime as a standard metric. For now, only gauges are supported, but others may be added as needed.