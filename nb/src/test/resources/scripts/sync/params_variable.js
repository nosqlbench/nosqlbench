print('params.get("one")=\'' + params.get("one") + "'");
print('params.get("three")=\'' + params.get("three") + "'");
print('params.size()=' + params.size());

var overrides = {
  'three': "five"
};
print('params.get("three") [overridden-three-five]=\'' + params.withOverrides(overrides).get("three") + "'");
var defaults = {
    'four': "niner"
};
print('params.get("four") [defaulted-four-niner]=\'' + params.withDefaults(defaults).get("four") + "'");

