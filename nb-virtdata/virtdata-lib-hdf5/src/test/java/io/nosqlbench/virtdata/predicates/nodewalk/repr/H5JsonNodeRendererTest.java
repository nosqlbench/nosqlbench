package io.nosqlbench.virtdata.predicates.nodewalk.repr;

import io.nosqlbench.virtdata.predicates.nodewalk.types.ConjugateNode;
import io.nosqlbench.virtdata.predicates.nodewalk.types.OpType;
import io.nosqlbench.virtdata.predicates.nodewalk.types.PredicateNode;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.assertj.core.api.Assertions.assertThat;

public class H5JsonNodeRendererTest {

  public static final String test1 = """
      {
        "field": 0,
        "op": "EQ",
        "v": [
          123
        ]
      }""";

  @Test
  public void testEx1() {
    PredicateNode p = new PredicateNode(0, OpType.EQ,123);
    H5JsonNodeRenderer h5r = new H5JsonNodeRenderer(new String[]{"firstname","lastname"});
    String result = h5r.apply(p);
    assertThat(result).isEqualTo(test1);
  }

    @Test
    public void testEx2() {
        ByteBuffer buffer = ByteBuffer.allocate(100);
        buffer.put((byte) 1); // Type AND
        buffer.put((byte) 2); // 2 Nodes
        buffer.put((byte) 0); // Type PRED
        buffer.put((byte) 0); // field 0
        buffer.put((byte) 2); // op EQ
        buffer.putShort((short) 1); // 1 value
        buffer.putLong(12); // value 12
        buffer.put((byte) 2); // Type OR
        buffer.put((byte) 2); // 2 Nodes
        buffer.put((byte) 0); // Type PRED
        buffer.put((byte) 1); // field 1
        buffer.put((byte) 6); // op IN
        buffer.putShort((short) 2); // 2 values
        buffer.putLong(11); // value 11
        buffer.putLong(13); // value 13
        buffer.put((byte) 0); // Type PRED
        buffer.put((byte) 2); // field 2
        buffer.put((byte) 2); // op EQ
        buffer.putShort((short) 1); // 1 value
        buffer.putLong(15); // value 15
        buffer.flip();

        ConjugateNode p =new ConjugateNode(buffer);
        H5JsonNodeRenderer h5r = new H5JsonNodeRenderer(new String[]{"firstname","middlename","lastname"});
        String result = h5r.apply(p);
        assertThat(result).isEqualTo(test2);
    }

  public static final String test2 = """
  {
    "type": "AND",
    "values": [
      {
        "field": 0,
        "op": "EQ",
        "v": [
          12
        ]
      },
      {
        "type": "OR",
        "values": [
          {
            "field": 1,
            "op": "IN",
            "v": [
              11,
              13
            ]
          },
          {
            "field": 2,
            "op": "EQ",
            "v": [
              15
            ]
          }
        ]
      }
    ]
  }""";

  public static final String testExample = """
        {
            "conjunction": "none",
            "terms": [
                {
                    "field": {"name": "firstname"},
                    "operator": "in",
                    "comparator": {"value":
                        ["Mark","Mark's friend Joe"]
                        }
                }
            ]
        }
        """;
  public static final String test3 = """
        {
            "conjunction": "or",
            "terms": [
                {
                    "field": {"name": "highprice"},
                    "operator": "gt",
                    "comparator": {"value": 1000}
                },
                {
                    "field": {"name": "lowprice"},
                    "operator": "lt",
                    "comparator": {"value": 1}
                }
            ]
        }
        """;

}
