print("params from command line:");
print(params);
print('before: params.get("three"):' + params.get("three"));
print('before: params.three:' + params.get("three"));

var overrides = {
  'three': "undef",
};

print("params.three after overriding with three:UNDEF");
params = params.withOverrides({'three':'UNDEF'});
print(params);
print('after: params.get("three"):' + params.get("three"));
print('after: params.three:' + params.get("three"));


