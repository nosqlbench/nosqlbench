/*
 * Copyright (c) 2022 nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.nosqlbench.virtdata.library.basics.shared.distributions;

import io.nosqlbench.nb.api.nbio.NBIO;
import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.library.basics.core.stathelpers.AliasSamplerDoubleInt;
import io.nosqlbench.virtdata.library.basics.shared.from_long.to_long.Hash;
import io.nosqlbench.virtdata.library.basics.core.stathelpers.EvProbD;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.LongFunction;

/**
 * Provides sampling of a given field in a CSV file according
 * to discrete probabilities. The CSV file must have headers which can
 * be used to find the named columns for value and weight. The value column
 * contains the string result to be returned by the function. The weight
 * column contains the floating-point weight or mass associated with the
 * value on the same line. All the weights are normalized automatically.
 *
 * <P>If there are multiple file names containing the same format, then they
 * will all be read in the same way.</P>
 *
 * <p>If the first word in the filenames list is 'map', then the values will not
 * be pseudo-randomly selected. Instead, they will be mapped over in some
 * other unsorted and stable order as input values vary from 0L to Long.MAX_VALUE.</p>
 *
 * <p>Generally, you want to leave out the 'map' directive to get "random sampling"
 * of these values.</p>
 *
 * <p>This function works the same as the three-parametered form of WeightedStrings,
 * which is deprecated in lieu of this one. Use this one instead.</p>
 */
@Categories(Category.general)
@ThreadSafeMapper
public class WeightedStringsFromCSV implements LongFunction<String> {

    private final String[] filenames;
    private final String valueColumn;
    private final String weightColumn;
    private final String[] lines;
    private final AliasSamplerDoubleInt sampler;
    private final Hash hash;

    /**
     * Create a sampler of strings from the given CSV file. The CSV file must have plain CSV headers
     * as its first line.
     * @param valueColumn The name of the value column to be sampled
     * @param weightColumn The name of the weight column, which must be parsable as a double
     * @param filenames One or more file names which will be read in to the sampler buffer
     */
    public WeightedStringsFromCSV(String valueColumn, String weightColumn, String... filenames) {
        this.filenames = filenames;
        this.valueColumn = valueColumn;
        this.weightColumn = weightColumn;
        List<EvProbD> events = new ArrayList<>();
        List<String> values = new ArrayList<>();

        if (filenames[0].equals("map")) {
            filenames = Arrays.copyOfRange(filenames,1,filenames.length);
            this.hash=null;
        } else {
            if (filenames[0].equals("hash")) {
                filenames = Arrays.copyOfRange(filenames,1,filenames.length);
            }
            this.hash=new Hash();
        }
        for (String filename: filenames) {
            if (!filename.endsWith(".csv")) {
                filename = filename+".csv";
            }
            CSVParser csvdata = NBIO.readFileCSV(filename);
            for (CSVRecord csvdatum : csvdata) {
                if (csvdatum.get(valueColumn) != null && csvdatum.get(weightColumn) != null) {
                    String value = csvdatum.get(valueColumn);
                    values.add(value);
                    String weight = csvdatum.get(weightColumn);
                    if(!weight.isEmpty()) {
                        events.add(new EvProbD(values.size() - 1, Double.valueOf(weight)));
                    }
                }
            }
        }
        sampler = new AliasSamplerDoubleInt(events);
        lines = values.toArray(new String[0]);
    }

    @Override
    public String apply(long value) {
        if (hash!=null) {
            value = hash.applyAsLong(value);
        }
        double unitValue = (double) value / (double) Long.MAX_VALUE;
        int idx = sampler.applyAsInt(unitValue);
        return lines[idx];
    }
}
