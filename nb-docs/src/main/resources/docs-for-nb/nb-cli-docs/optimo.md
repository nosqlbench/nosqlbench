# Optimo Extension (BOBYQA)

Optimo is the name of the scripting extension that allows nosqlbench
scenarios to take advantage of the BOBYQA optimization algorithm.

## Usage

To instantiate a new instance of optimo, call the extension object
`optimos` in this way:

```
var optimo1 = optimos.init();
```

You work with the instance directly. The extension object `optimos` is
there only as a dispenser of new optimo instances, nothing more.

To add a parameter to the optimo instance, use the `param` method like
this:

```
optimo1.param('pressure', 1, 500);
optimo1.param('temperature', 275, 307);
```

This adds a `pressures` parameter to the algorithm with a range between
1 and 500, inclusive. This means that optimo will provide a parameter of
this name with a value in that range for each evaluation.

You should also set some initial parameters, which are key settings for
the BOBYQA optimizer. Read more about BOBYQA to understand what these
settings mean.

```
optimo1
 .setInitialRadius(10000.0) // The initial trust radius
 .setStoppingRadius(0.001)  // The stopping condition (trust radius)
 .setMaxEval(100);         // Maximum number of iterations
```

Finally, you need to give optimo an objective function. The construction
of the objective function is the most important detail for using optimo
effectively. The function signature is simply `function(params)`, where
params is a map containing the named parameters provided by BOBYQA. This
makes it easy to integrate and extend the algorithm with varying
parameters as you like.

For example, you can add a function with this syntax:

```
optimo1.setObjectiveFunction(function(params) {
  return params.temperature * params.pressure
});
```

In practice, this function will look much more like this:

```
optimo1.setObjectiveFunctikon(function(params) {
  provide_user_feedback_about_params(params);
  var result=run_test_with_params(params);
  value = calculate_objective_value_of_result(result);
  provide_user_feedback_about_result_and_value(result,value);
  return value;
});
```

This schematic shows the two-phase aspect of combining the parameters
for testing with an actual test, and then taking the results of that
test and scoring it so that the algorithm knows which way to steer its
search pattern.

Once you have configured an optimo instance, you can put it in control
of the scenario with a call like this:

```
var result = optimo.optimize();
```

The result that it provides is an instance of
io.nosqlbench.extensions.optimizers.MVResult, which provides
`.getVarArray()`, `.getVarMap()`, and a useful `.toString()` method.

