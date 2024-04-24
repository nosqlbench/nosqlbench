# diag

This is a diagnostic activity type. Its action simply reports the cycle number and the reporting delay between it's schedule reporting time and the current time. The reporting is interleaved between the threads, with the logical number of reports remaining constant regardless of the thread count.

## example activitydef

alias=testdiag;driver=diag;threads=50;interval=2000;

## controls

When the interval parameter is changed, all motor slots are notified and the reporting times are updated for each
active motor.

## parameters

- interval - The number of milliseconds to delay between each report.
  ( default: 1000ms )
- modulo - The cycle rate at which to generate a single output status line.
  ( default: modulo=10000000 )
- errormodulo - The cycle rate at which to generate a non-zero status code
  (1=every cycle, 10=every 10th cycle)
  ( default: errormodule=1000 )
- phases - The number of phases to run.
  ( default: phases=1 )
