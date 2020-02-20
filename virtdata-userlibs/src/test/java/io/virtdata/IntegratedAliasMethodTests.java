package io.virtdata;

import io.virtdata.libbasics.shared.distributions.DelimFrequencySampler;
import io.virtdata.libbasics.shared.distributions.WeightedStringsFromCSV;
import io.virtdata.libbasics.shared.distributions.CSVFrequencySampler;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Test
public class IntegratedAliasMethodTests {

    @Test
    public void testCensusData() {
        WeightedStringsFromCSV surnames = new WeightedStringsFromCSV("Name", "prop100k", "data/surnames");
        String n = surnames.apply(2343);
        assertThat(n).isEqualTo("Conaway");
    }
    public void testCSVFrequencySampler() {
        CSVFrequencySampler names= new CSVFrequencySampler("data/countries", "COUNTRY_CODE" );
        String n = names.apply(23);
        assertThat(n).isEqualTo("CZ");
    }

    public void testDelimFrequencySampler() {
        DelimFrequencySampler names= new DelimFrequencySampler(
                "data/countries",
                "COUNTRY_CODE",
                ','
        );
        String n = names.apply(23);
        assertThat(n).isEqualTo("CZ");
    }
}