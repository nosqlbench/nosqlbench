# Modular Scenarios

Sketch of named scenario:

```yaml
scenarios:
 scenario:
  runit: start driver=... alias=runit
  modifyit: optimo activity=runit

```

In this case, a named scenario `scenario1` is used as the _scenario context_ in which two
different scenario phases are run. This could happen before but with some restrictions which
prevented a java-native "scenario" from being run in the same context. A couple changes are
needed to support the pattern above:

- The execution boundary of what was previously self-contained within a scenario as such needs
  to be modular. Different kinds of scenario logic embodiments need to be able to work on the
  same scenario state in turn, native or scripted. Thus the previous "scenario" model is no
  longer sufficient.
- A scenario needs to be defined as the context of a scenario which can be shared across
  separate phases, and the previous scenario logic needs to be made more granular so that it can
  be composed within the scenario context.
- Java and non-java scenario phases need to be treated uniformly, as well as any other
  implementations.

This requires runtime engine changes to align to the above, but will also involve a couple other
changes:

- Since scenario phases can operate on an established scenario context
