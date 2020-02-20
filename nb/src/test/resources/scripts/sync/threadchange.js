scenario.start('type=diag;alias=threadchange;cycles=0..100000000;threads=1;interval=2000;modulo=1000000');
activities.threadchange.threads=1;
print("threads now " + activities.threadchange.threads);
print('waiting 500 ms');
activities.threadchange.threads=5;
print("threads now " + activities.threadchange.threads);
scenario.stop('threadchange');
