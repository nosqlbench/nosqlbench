package io.nosqlbench.engine.api.activityimpl.uniform;

import io.nosqlbench.adapters.api.activityimpl.OpDispenser;
import io.nosqlbench.adapters.api.activityimpl.uniform.flowtypes.CycleOp;
import io.nosqlbench.engine.api.activityapi.planning.OpSequence;

import java.util.Arrays;
import java.util.List;

public class Diagnostics {
    public static String summarizeSequencedOps(OpSequence<OpDispenser<? extends CycleOp<?>>> sequence) {
        List<OpDispenser<? extends CycleOp<?>>> ops = sequence.getOps();
        int[] seq = sequence.getSequence();

        StringBuilder sb = new StringBuilder();
        sb.append(summarizeMappedOps(ops));
        sb.append("# Summary of sequenced operations (op dispenser LUT):\n");
        sb.append("# LUT: ").append(Arrays.toString(seq)).append("\n");
        int[] freq = new int[ops.size()];
        for (int idx : seq) {
            freq[idx]++;
        }
        sb.append("# Ratios: \n");
        for (int i = 0; i < freq.length; i++) {
            sb.append(String.format("# %03d %s", freq[i], ops.get(i).getOpName()));
        }

        return sb.toString();
    }

    public static String summarizeMappedOps(List<? extends OpDispenser<?>> ops) {
        StringBuilder sb = new StringBuilder();
        sb.append("# Summary of mapped operations (op dispensers):\n");
        for (int idx = 0; idx < ops.size(); idx++) {
            sb.append(String.format("# %03d (ratio:%03d) name:%s\n#            %s\n", idx,
                ops.get(idx).getRatio(), ops.get(idx).getOpName(),
                ops.get(idx).getClass().getSimpleName()));
        }
        return sb.toString();
    }
}
