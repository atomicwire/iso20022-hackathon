package io.atomicwire.iso20022hackathon;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class AtomicSettlement {

  public static void main(String[] args) throws Exception {
    StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
    env.fromElements("a", "trivial", "Flink", "application").print();
    env.execute(AtomicSettlement.class.getSimpleName());
  }
}
