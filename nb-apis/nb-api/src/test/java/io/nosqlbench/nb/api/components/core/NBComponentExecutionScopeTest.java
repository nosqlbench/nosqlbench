package io.nosqlbench.nb.api.components.core;

import io.nosqlbench.nb.api.components.events.NBEvent;
import io.nosqlbench.nb.api.config.standard.TestComponent;
import org.apache.commons.logging.Log;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class NBComponentExecutionScopeTest {

    @Test
    public void verifyComponentLifecycleHooks() {
        List<String> log = new ArrayList<>();
        try (LoggerComponent parent = new LoggerComponent(log, "parent", "parent")) {
            LoggerComponent child = new LoggerComponent(parent, log, "child", "child");
        }
        String concatLog = log.stream().collect(Collectors.joining("\n"));
        assertThat(concatLog).matches(Pattern.compile(
            """
                LoggerComponent .parent="parent".->detachChild
                LoggerComponent .child="child",parent="parent".->beforeDetach
                LoggerComponent .child="child",parent="parent".<-beforeDetach
                LoggerComponent .parent="parent".<-detachChild""",
            Pattern.MULTILINE
        ));
    }

    private final static class LoggerComponent extends TestComponent {
        public final List<String> _log;

        public LoggerComponent(List<String> log, String... labels) {
            super(labels);
            this._log = log;
        }

        public LoggerComponent(NBComponent parent, List<String> log, String... labels) {
            super(parent, labels);
            this._log = log;
        }


        @Override
        public void beforeDetach() {
            _log.add(description() + "->beforeDetach");
            super.beforeDetach();
            _log.add(description() + "<-beforeDetach");
        }

        @Override
        public void onEvent(NBEvent event) {
            _log.add(event.toString());
        }

        @Override
        public NBComponent detachChild(NBComponent... children) {
            _log.add(description() + "->detachChild");
            NBComponent result = super.detachChild(children);
            _log.add(description() + "<-detachChild");
            return result;
        }

    }
}
