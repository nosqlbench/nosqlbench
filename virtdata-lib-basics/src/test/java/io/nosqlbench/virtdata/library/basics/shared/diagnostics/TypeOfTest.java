package io.nosqlbench.virtdata.library.basics.shared.diagnostics;

import org.junit.Test;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

public class TypeOfTest {

    @Test
    public void testGenericSignature() {
        TypeOf ft = new TypeOf();

        HashMap hm = new HashMap();
        String hmType = ft.apply(hm);
        assertThat(hmType).isEqualTo("java.util.HashMap");

        HashMap<Long,String> lshm = new HashMap<Long,String>();
        String typedType = ft.apply(lshm);
        assertThat(typedType).isEqualTo("java.util.HashMap");

    }

}