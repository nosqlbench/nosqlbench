// With the API support for Polyglot, we lost the ability to expose
// map operations (underlying Java Map API) as well as proxy methods
// on ECMA objects. What works now is object semantics + non-map methods.
// More testing and refinement may be needed to clarify the rules here.

// previously:
// print('params.get("one")=\'' + params.get("one") + "'"); // worked with nashorn
// print('params.get("three")=\'' + params.get("three") + "'");
// print('params.size()=' + params.size());

// Called with one=two three=four

print('params["one"]=\'' + params["one"] + "'");
print('params["three"]=\'' + params["three"] + "'");

var overrides = {
  'three': "five"
};

var overridden = params.withOverrides(overrides);

print('overridden["three"] [overridden-three-five]=\'' + overridden["three"] + "'");

var defaults = {
    'four': "niner"
};

var defaulted = params.withDefaults(defaults);

print('defaulted.get["four"] [defaulted-four-niner]=\'' + defaulted["four"] + "'");

