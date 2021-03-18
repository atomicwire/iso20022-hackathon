Real-Time Synchronised Settlement Demo
======================================

This repository contains a demo implementation of Real-Time Synchronised Settlement, as proposed in our submission to
the [ISO 20022 Hackathon](https://iso20022hackathon.hackerearth.com/) hosted by the BIS Innovation Hub and SWIFT.


Running the demo
----------------

> Note: Java 8 or newer is required to run the demo.

There are two built-in options for running the demo locally.

For a simple demonstration, simulating at a rate of 1 settlement request per second and with message tracing enabled,
run:

```bash
$ ./gradlew :run
```

And to run the simulation at the maximum rate with message tracing disabled, run:

```bash
$ ./gradlew :runUnlimited
```

On Windows, substitute `./gradlew.bat` as the command.


Running in an IDE
-----------------

To run in an IDE of your choice, you must install the Lombok plugin and enable annotation processing.


License
-------

Copyright © 2021, [Atomic Wire Technology Ltd](https://www.atomicwire.io/)
