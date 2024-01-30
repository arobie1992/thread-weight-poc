Very simple example of overloading Java with threads. Platform threads use OS threads and will hit issues spawning new 
threads at a certain amount of load. Virtual threads are part of Project Loom and are Java's take on 
[green threads](https://en.wikipedia.org/wiki/Green_thread). They avoid the issue, but require significant extra 
complexity in Java.

See `Server.java` for config options. All should be self-explanatory. Two methods are provided. One, `runServer` is a
realistic scenario where this might occur, but can be difficult to trigger on your own. The other, `loadTest` is
provided for easily reproducing the error.

To run: `java Server.java`. Requires Java 21.