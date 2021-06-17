package io.virtdata;

import io.nosqlbench.virtdata.library.basics.shared.distributions.DelimFrequencySampler;
import io.nosqlbench.virtdata.library.basics.shared.distributions.WeightedStringsFromCSV;
import io.nosqlbench.virtdata.library.basics.shared.distributions.CSVFrequencySampler;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class IntegratedAliasMethodTests {

    @Test
    public void testCensusData() {
        WeightedStringsFromCSV surnames = new WeightedStringsFromCSV("Name", "prop100k", "data/surnames");
        String n = surnames.apply(2343);
        assertThat(n).isEqualTo("Conaway");
    }

    @Test
    public void testCSVFrequencySampler() {
        CSVFrequencySampler names= new CSVFrequencySampler("data/countries", "COUNTRY_CODE" );
        String n = names.apply(23);
        assertThat(n).isEqualTo("TK");
    }

    @Test
    public void testDelimFrequencySampler() {
        DelimFrequencySampler names= new DelimFrequencySampler(
                "data/countries",
                "COUNTRY_CODE",
                ','
        );
        String n = names.apply(23);
        assertThat(n).isEqualTo("TK");
    }
}
