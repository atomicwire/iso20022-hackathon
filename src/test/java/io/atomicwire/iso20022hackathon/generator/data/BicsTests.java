package io.atomicwire.iso20022hackathon.generator.data;

import static io.atomicwire.iso20022hackathon.generator.data.Bics.chooseRandomBicPair;
import static org.assertj.core.api.Assertions.assertThat;

import io.atomicwire.iso20022hackathon.generator.data.Bics.BicPair;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

public class BicsTests {

  @Test
  void testChooseRandomBicPair() {
    List<String> fakeBics = new ArrayList<>();
    fakeBics.add("a");
    fakeBics.add("b");
    fakeBics.add("c");

    for (int i = 0; i < 1000; i++) {
      BicPair pair = chooseRandomBicPair(fakeBics);
      assertThat(pair.getBic1()).isNotEqualTo(pair.getBic2());
    }
  }
}
