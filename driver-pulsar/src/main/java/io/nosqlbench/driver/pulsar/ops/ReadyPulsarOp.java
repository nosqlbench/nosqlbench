package io.nosqlbench.driver.pulsar.ops;

import io.nosqlbench.engine.api.activityconfig.yaml.OpTemplate;
import io.nosqlbench.engine.api.scoping.ScopedSupplier;
import io.nosqlbench.engine.api.templating.CommandTemplate;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

import java.util.Map;
import java.util.function.LongFunction;
import java.util.function.Supplier;

public class ReadyPulsarOp {

    private final CommandTemplate cmdTpl;
    private final LongFunction<PulsarOp> opFunc;
    private final Supplier<PulsarClient> clientFunc;

    public ReadyPulsarOp(OpTemplate opTemplate, Supplier<PulsarClient> clientFunc) {
        this.cmdTpl = new CommandTemplate(opTemplate);
        this.clientFunc = clientFunc;
        if (cmdTpl.isDynamic("op_scope")) {
            throw new RuntimeException("op_scope must be static");
        }
        this.opFunc = resolve();
        ScopedSupplier scope = ScopedSupplier.valueOf(cmdTpl.getStaticOr("op_scope", "singleton"));
        Supplier<LongFunction<PulsarOp>> opSupplier = scope.supplier(this::resolve);


        // thread local op construction or not
        // this allows deferment of client construction via lambda capture
        if (true) {
            tlOpFunction = ThreadLocal.withInitial(this::resolve);
        } else {
            tlOpFunction = ThreadLocal.withInitial(() -> opFunc);
        }
    }

    private LongFunction<PulsarOp> resolve() {

        if (cmdTpl.containsKey("produce")) {

        } else if (cmdTpl.containsKey("consume")) {

        }

        tlOpFunction = ThreadLocal.withInitial()

        if (cmdTpl.isStatic("type"))
            if (cmdTpl.isStatic("produce") || cmdTpl.isDynamic("produce")) {

            }

        // Create a pulsarOp which can be executed.
        // The
        public PulsarOp bind ( long value){
            PulsarClient client = clientFunc.apply(Thread.currentThread());
            try {

                Producer<byte[]> producer = client.newProducer().topic("topic").create();

            } catch (PulsarClientException e) {
                e.printStackTrace();
            }
            Map<String, String> cmd = cmdTpl.getCommand(value);


            return (Void) -> new PulsarOp() {
            };
        }


    }

    public PulsarOp bind(long value) {
        return opFunc.apply(value);
    }

}
