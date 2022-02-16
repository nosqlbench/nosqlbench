package io.virtdata;

import io.nosqlbench.virtdata.core.bindings.Bindings;
import io.nosqlbench.virtdata.core.bindings.BindingsTemplate;
import io.nosqlbench.virtdata.core.templates.StringBindings;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class IntegratedStringBindingsTest {

    private static BindingsTemplate template;
    private static Bindings bindings;

    @BeforeAll
    public static void setupTemplate() {
        BindingsTemplate bindingsTemplate = new BindingsTemplate();
        bindingsTemplate.addFieldBinding("ident","Identity()");
        bindingsTemplate.addFieldBinding("mod5", "Mod(5)");
        bindingsTemplate.addFieldBinding("mod-5", "Mod(5)");
        bindingsTemplate.addFieldBinding("5_mod_5", "Mod(5)");
        bindingsTemplate.addFieldBinding(".mod5", "Mod(5)");
        template = bindingsTemplate;
        bindings = bindingsTemplate.resolveBindings();
    }

    @Test
    public void testEven() {
        StringBindings c = new StringBindings("A{ident}B{ident}C{mod5}D{mod-5}",template);
        String bind3 = c.bind(3);
        assertThat(bind3).isEqualTo("A3B3C3D3");
    }

    @Test
    public void testOdd() {
        StringBindings c = new StringBindings("A{ident}B{ident}C{mod5}D{mod-5}E",template);
        String bind3 = c.bind(7);
        assertThat(bind3).isEqualTo("A7B7C2D2E");
    }

    @Test
    public void testBindValues() {
        StringBindings c = new StringBindings("A{ident}C", template);
        String s = c.apply(0);
        assertThat(s).isEqualTo("A0C");
    }

    @Test
    public void testBindValuesSpecialChars() {
        StringBindings c = new StringBindings("A{mod-5}C", template);
        String s = c.apply(6L);
        assertThat(s).isEqualTo("A1C");

        c = new StringBindings("A{5_mod_5}C", template);
        s = c.apply(7L);
        assertThat(s).isEqualTo("A2C");
    }

}
