// With the API support for Polyglot, we lost the ability to expose
// map operations (underlying Java Map API) as well as proxy methods
// on ECMA objects. What works now is object semantics + non-map methods.
// More testing and refinement may be needed to clarify the rules here.

print("params from command line:");
print(params);
print('before: params["three"]:' + params["three"]);
print('before: params.three:' + params.three);

var overrides = {
  'three': "undef",
};

print("params.three after overriding with three:UNDEF");
params = params.withOverrides({'three':'UNDEF'});
print(params);
print('after: params["three"]:' + params["three"]);
print('after: params.three:' + params.three);


