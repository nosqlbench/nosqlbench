# Script Examples

The scripts in this directory are meant to be used for two things:

1. Integrated testing of real scenarios as part of the NoSQLBench build.
2. Living documentation of NoSQLBench engine and extensions which is proven to work and always
   up to date.

## Running

All of these scripts can be invoked directly by NoSQLBench with one of these patterns:

* `nb script <basename> <arg>...`
  * example: `nb script params_variable one=two three=four`
* `java -jar nb.jar script <filename>.js <arg>...` (ensure you have Java 17 for this)
  * example: `java -jar nb.jar params_variable five=six`
* `nb <basename> <arg>...` (if and only if `<script>.js` is located in scripts/auto)
  * example: `nb `

## Diag Adapter

Some of these scripts use a diag driver which acts as a configurable
op template for the purposes of demonstration and integrated testing.
For more details on how to configure and use this for more advanced
scenarios, see the help for the adapter `diag`. NOTE: the diag driver
adapter has been updated and modernized for nb5, so if you are used
to the old one, be sure to brush up on the improvements.

## Status

It is a goal to have all the scripts located within this directory
be executed automatically on every full build with their own documented
success criteria which is also visible. As of 2022-06-09, all of the
scripts besides the ones in the doconly folder are executed by
an integrated testing harness, although this is not enforced yet as a rule.

Further, these scripts should be externalized into docs in a way that is
kept up to date with each release.


