package io.nosqlbench.driver.pulsar;

import io.nosqlbench.engine.api.activityconfig.yaml.OpTemplate;
import io.nosqlbench.engine.api.templating.CommandTemplate;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.client.api.transaction.Transaction;

import java.util.Map;
import java.util.function.Function;
import java.util.function.LongFunction;

public class ReadyPulsarOp {

    private final Function<Thread, PulsarClient> clientFunc;
    private final CommandTemplate cmdTpl;
    private final LongFunction<PulsarOp> opFunc;

    public ReadyPulsarOp(OpTemplate opTemplate, Function<Thread, PulsarClient> clientFunc) {
        this.cmdTpl = new CommandTemplate(opTemplate);
        this.clientFunc = clientFunc;
        this.opFunc = resolve();
    }

    private LongFunction<PulsarOp> resolve() {

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


            return new PulsarOp() {
            };
        }


    }

    public PulsarOp bind(long value) {
        return opFunc.apply(value);
    }

}
