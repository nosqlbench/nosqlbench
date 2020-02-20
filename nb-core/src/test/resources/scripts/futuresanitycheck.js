print('waiting 500 ms');
scenario.waitMillis(500);
print('waited');
scenario.start('type=diag;alias=test;cycles=0..1000000000;threads=10;interval=2000;');
print('waiting again');
scenario.modify('test','threads',"1");
print('waiting 5000 ms');
scenario.waitMillis(5000);
scenario.modify('test','threads',"20");
print('modified threads to 20');



