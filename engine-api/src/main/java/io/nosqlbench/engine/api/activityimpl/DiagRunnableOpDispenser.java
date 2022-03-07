package io.nosqlbench.engine.api.activityimpl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.nosqlbench.engine.api.templating.CommandTemplate;

import java.util.Map;

public class DiagRunnableOpDispenser<O extends Runnable> extends BaseOpDispenser<Runnable> {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final CommandTemplate cmdTpl;

    public DiagRunnableOpDispenser(CommandTemplate commandTemplate) {
        super(commandTemplate);
        this.cmdTpl = commandTemplate;
    }

    @Override
    public Runnable apply(long value) {
        Map<String, String> command = cmdTpl.getCommand(value);
        String body = gson.toJson(command);
        return new DiagRunnableOp(body);
    }
}
