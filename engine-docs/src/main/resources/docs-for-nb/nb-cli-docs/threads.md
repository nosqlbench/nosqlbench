# Setting threads

Threads may be set in a few different ways depending on the type of
testing you are doing.

Sometimes, you need the client runtime to emulate a threading model of
an application. Other times you may want the client to go as fast as it
can regardless of the threading model. The difference between these
varies significantly depending on whether you are using asynchronous
messaging or not.

Some valid forms for setting threads include:

- threads=auto
  - Sets the thread count to 10x the number of CPUs
  - This does not consider hyper-threading
- threads=2x
  - Sets the thread count to 2x the number of CPUs
  - This does not consider hyper-threading
- threads=10
  - Simply sets the thread count to 10
