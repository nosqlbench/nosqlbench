package io.nosqlbench.activitytype.cql.datamappers.functions.rainbow;

import io.nosqlbench.virtdata.annotations.ThreadSafeMapper;

@ThreadSafeMapper
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
