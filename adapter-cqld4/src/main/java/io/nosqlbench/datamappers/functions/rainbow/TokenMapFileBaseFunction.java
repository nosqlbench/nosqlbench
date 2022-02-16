package io.nosqlbench.datamappers.functions.rainbow;

import java.util.function.IntToLongFunction;

public abstract class TokenMapFileBaseFunction implements IntToLongFunction {
    protected static ThreadLocal<TokenMapFileAPIService> tl_DataSvc;

    public TokenMapFileBaseFunction(String filename, boolean loopdata, boolean instanced, boolean ascending) {
        tl_DataSvc = ThreadLocal.withInitial(() -> new TokenMapFileAPIService(filename, loopdata, instanced, ascending));
    }

    public TokenMapFileBaseFunction(String filename) {
        this(filename, false, true, true);
    }

//    @Override
//    public long applyAsLong(long operand) {
//        BinaryCursorForTokenCycle bc;
//        bc.next(operand);
//        return 0;
//    }
}
