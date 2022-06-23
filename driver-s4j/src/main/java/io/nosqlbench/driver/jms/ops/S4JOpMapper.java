package io.nosqlbench.driver.jms.ops;

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


import io.nosqlbench.driver.jms.S4JActivity;
import io.nosqlbench.driver.jms.S4JSpace;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.jms.Session;
import java.util.function.LongFunction;

public abstract class S4JOpMapper implements LongFunction<S4JOp> {

    private final static Logger logger = LogManager.getLogger(S4JMsgSendOp.class);

    protected final S4JSpace s4JSpace;
    protected final S4JActivity s4JActivity;
    protected final String s4jOpType;

    protected final LongFunction<Boolean> tempDestBoolFunc;
    protected final LongFunction<String> destTypeStrFunc;
    protected final LongFunction<String> destNameStrFunc;
    protected final LongFunction<Boolean> reuseClntBoolFunc;
    protected final LongFunction<Boolean> asyncAPIBoolFunc;
    protected final LongFunction<Integer> txnBatchNumFunc;

    public S4JOpMapper(S4JSpace s4JSpace,
                       S4JActivity s4JActivity,
                       String s4jOpType,
                       LongFunction<Boolean> tempDestBoolFunc,
                       LongFunction<String> destTypeStrFunc,
                       LongFunction<String> destNameStrFunc,
                       LongFunction<Boolean> reuseClntBoolFunc,
                       LongFunction<Boolean> asyncAPIBoolFunc,
                       LongFunction<Integer> txnBatchNumFunc)
    {
        this.s4JSpace = s4JSpace;
        this.s4JActivity = s4JActivity;
        this.s4jOpType = s4jOpType;
        this.tempDestBoolFunc = tempDestBoolFunc;
        this.destTypeStrFunc = destTypeStrFunc;
        this.destNameStrFunc = destNameStrFunc;
        this.reuseClntBoolFunc = reuseClntBoolFunc;
        this.asyncAPIBoolFunc = asyncAPIBoolFunc;
        this.txnBatchNumFunc = txnBatchNumFunc;
    }

    protected boolean commitTransaction(int txnBatchNum, int jmsSessionMode, long curCycleNum) {
        // Whether to commit the transaction which happens when:
        // - session mode is equal to "SESSION_TRANSACTED"
        // - "txn_batch_num" has been reached since last reset
        boolean commitTransaction = (Session.SESSION_TRANSACTED == jmsSessionMode);
        if (commitTransaction) {
            int txnBatchTackingCnt = s4JSpace.getTxnBatchTrackingCnt();

            if ( ( (txnBatchTackingCnt >=  txnBatchNum) && ((txnBatchTackingCnt % txnBatchNum) == 0) ) ||
                 (curCycleNum == (s4JActivity.getActivityDef().getCycleCount() - 1)) ) {
                commitTransaction = true;
                if (logger.isDebugEnabled()) {
                    logger.debug("Commit transaction ({}, {}, {})",
                        txnBatchTackingCnt,
                        s4JSpace.getTotalOpResponseCnt(), curCycleNum);
                }
            }
            else {
                commitTransaction = false;
            }

            s4JSpace.incTxnBatchTrackingCnt();
        }

        return commitTransaction;
    }
}
