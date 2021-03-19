Real-Time Synchronised Settlement Demo
======================================

This repository contains a demo implementation of Real-Time Synchronised Settlement, as proposed in our submission to
the [ISO 20022 Hackathon](https://iso20022hackathon.hackerearth.com/) hosted by the BIS Innovation Hub and SWIFT.

The demo simulates an incoming stream of PvP settlement requests in the form of ISO 20022 [Foreign Exchange Trade
Instruction (fxtr.014)](https://www.iso20022.org/iso-20022-message-definitions?search=fxtr.014) messages and atomically
settles them via mock ISO 20022 communication with the relevant liquidity pool (e.g. a central bank) to reserve the
necessary liquidity from the delivery agents before instructing the actual payments.


### Contents

* [Running the demo](#running-the-demo)
* [How it works](#how-it-works)
* [Running in an IDE](#running-in-an-ide)
* [License](#license)


Running the demo
----------------

> Note: Java 8 or Java 11 is required to run the demo.

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


How it Works
------------

This demo is implemented as a stream processing application using [Apache Flink](https://flink.apache.org/), a high-
performance and high-scale framework for developing streaming applications with consistency guarantees on the JVM.
Please see the [Apache Flink documentation](https://flink.apache.org/flink-architecture.html) for an introduction to
its concepts.

Our demo application is implemented five stages, which are wired together in the application entrypoint,
[AtomicSettlement](src/main/java/io/atomicwire/iso20022hackathon/AtomicSettlement.java).

* Map the incoming logical ISO 20022 messages to their conceptual representation, then split out both Payment
  Obligations that constitute the transaction
  * See [MapToAtomicSettlementContext](src/main/java/io/atomicwire/iso20022hackathon/MapToAtomicSettlementContext.java)
  * See [SplitPaymentObligations](src/main/java/io/atomicwire/iso20022hackathon/SplitPaymentObligations.java)
* Collect the Payment Obligations by currency and delivery agent, send a liquidity reservation to the relevant liquidity pool, and await the response
  * See [ReserveLiquidity](src/main/java/io/atomicwire/iso20022hackathon/ReserveLiquidity.java)
* Join the liquidity reservations back to the original transaction, then split out the Payment Obligations again
  * See [JoinLiquidityReservations](src/main/java/io/atomicwire/iso20022hackathon/JoinLiquidityReservations.java)
  * See [SplitPaymentObligations](src/main/java/io/atomicwire/iso20022hackathon/SplitPaymentObligations.java)
* Instruct the payment for each leg independently and await the response
  * See [SettlePaymentObligation](src/main/java/io/atomicwire/iso20022hackathon/SettlePaymentObligation.java)
* Join the payment confirmations and emit the logical message indicating its success
  * See [JoinPaymentConfirmations](src/main/java/io/atomicwire/iso20022hackathon/JoinPaymentConfirmations.java)

Additionally, there are a handful of other packages containing supporting code:

* [`context`](src/main/java/io/atomicwire/iso20022hackathon/context) contains the data classes used to move data
  throughout the application
* [`generator`](src/main/java/io/atomicwire/iso20022hackathon/generator) contains code to simulate the incoming
  settlement requests
* [`iso20022.logical`](src/main/java/io/atomicwire/iso20022hackathon/iso20022/logical) contains mock representations of
  the mock ISO 20022 logical messages used by the application
* [`iso20022.conceptual`](src/main/java/io/atomicwire/iso20022hackathon/iso20022/conceptual) contains mock
  representations of the mock ISO 20022 conceptual layer entities used by the application
* [`iso20022.bridge`](src/main/java/io/atomicwire/iso20022hackathon/iso20022/bridge) contains code for deriving ISO
  20022 conceptual entities from logical messages


Running in an IDE
-----------------

To run in an IDE of your choice, you must install the Lombok plugin and enable annotation processing.


License
-------

Copyright (c) 2021 [Atomic Wire Technology Limited](https://www.atomicwire.io/)

All rights reserved.

Licensed under the Apache License, Version 2.0, found in the [LICENSE](LICENSE) file in the root directory of this
repository.
