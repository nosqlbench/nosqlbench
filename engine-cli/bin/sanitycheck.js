scenario.start('type=diag;alias=test;min=1;max=10000000;threads=10;interval=2000;');

print('waiting 2000 ms');
scenario.waitMillis(2000);
print('waited');

activities.test.interval=1000;
activities.test.threads=2;

print('waiting 2000 ms');
scenario.waitMillis(2000);

activities.test.threads=20;
activities.test.interval=500;




