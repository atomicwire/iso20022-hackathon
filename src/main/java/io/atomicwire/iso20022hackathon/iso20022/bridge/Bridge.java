package io.atomicwire.iso20022hackathon.iso20022.bridge;

/** An adapter between ISO 20022 logical messages and their conceptual counterparts. */
public interface Bridge<Logical, Conceptual> {

  Conceptual bridge(Logical logical);
}
