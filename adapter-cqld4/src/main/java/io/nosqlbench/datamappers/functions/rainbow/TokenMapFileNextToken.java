package io.nosqlbench.datamappers.functions.rainbow;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

/**
 * Utility function used for advanced data generation experiments.
 */
@ThreadSafeMapper
@Categories({Category.experimental})
public class TokenMapFileNextToken extends TokenMapFileBaseFunction {

    public TokenMapFileNextToken(String filename, boolean loopdata, boolean ascending) {
        super(filename, loopdata, false, ascending);
    }

    @Override
    public long applyAsLong(int value) {
        TokenMapFileAPIService datasvc = tl_DataSvc.get();
        datasvc.next(value);
        return datasvc.getToken();
    }
}
