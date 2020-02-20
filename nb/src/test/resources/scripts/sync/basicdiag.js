basic_diag = params.withOverrides({
    "alias" : "basic_diag",
    "type" : "diag"
});


print('starting activity basic_diag');
scenario.start(basic_diag);
