var leader = {
    type: 'diag',
    alias: 'leader',
    targetrate: '10000'
};

var follower = {
    type: 'diag',
    alias: 'follower',
    linkinput: 'leader'
};

scenario.start(leader);
print("started leader");
scenario.start(follower);
print("started follower");

scenario.waitMillis(500);

scenario.stop(leader);
print("stopped leader");
scenario.stop(follower);
print("stopped follower");

