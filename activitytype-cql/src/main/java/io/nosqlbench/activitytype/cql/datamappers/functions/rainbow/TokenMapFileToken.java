package io.nosqlbench.activitytype.cql.datamappers.functions.rainbow;

import io.nosqlbench.virtdata.annotations.ThreadSafeMapper;

@ThreadSafeMapper
public class TokenMapFileToken extends TokenMapFileBaseFunction {

    public TokenMapFileToken(String filename, boolean loopdata, boolean ascending) {
        super(filename, loopdata, false, ascending);
    }

    @Override
    public long applyAsLong(int value) {
        TokenMapFileAPIService datasvc = tl_DataSvc.get();
        return datasvc.getToken();
    }
}
