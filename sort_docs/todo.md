- convert core input to be equivalent
  of `input=type:interval,cycles:N[..M]`
- Add doc support for input, input filters, outputs, output filters
- Build metadata scaffolding for parameters, so unused parameters may be
  warned about.
    - parameters should be part of the activity API
    - parameters should not re-trigger def observers for non-change
      evhandler
    - parameters across all subsystems should be discoverable or
      enumerable
- make stride auto-sizing uniformly apply after sequencing
- reimplement core activity and scenario logic as async/reactor with
  monitors
- convert to Java 9
- add activity library commands
- add --list-input-filters and --list-output-filters 