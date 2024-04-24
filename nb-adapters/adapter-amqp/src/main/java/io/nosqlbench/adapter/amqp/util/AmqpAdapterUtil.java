/*
 * Copyright (c) 2023 nosqlbench
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

package io.nosqlbench.adapter.amqp.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AmqpAdapterUtil {
    private static final Logger logger = LogManager.getLogger(AmqpAdapterUtil.class);

    public enum AMQP_EXCHANGE_TYPES {
        DIRECT("direct"),
        FANOUT("fanout"),
        TOPIC("topic"),
        HEADERS("headers");

        public final String label;
        AMQP_EXCHANGE_TYPES(String label) {
            this.label = label;
        }

        private static final Set<String> LABELS = Stream.of(values()).map(v -> v.label)
            .collect(Collectors.toUnmodifiableSet());

        public static boolean isValidLabel(String label) {
            return LABELS.contains(label);
        }
    }
    public static String getValidAmqpExchangeTypeList() {
        return StringUtils.join(AMQP_EXCHANGE_TYPES.LABELS, ", ");
    }


    public enum AMQP_PUB_CONFIRM_MODE {
        INDIVIDUAL("individual"),
        BATCH("batch"),
        ASYNC("async");

        public final String label;
        AMQP_PUB_CONFIRM_MODE(String label) {
            this.label = label;
        }

        private static final Set<String> LABELS = Stream.of(values()).map(v -> v.label)
            .collect(Collectors.toUnmodifiableSet());

        public static boolean isValidLabel(String label) {
            return LABELS.contains(label);
        }
    }
    public static String getValidAmqpPublisherConfirmModeList() {
        return StringUtils.join(AMQP_PUB_CONFIRM_MODE.LABELS, ", ");
    }

    // At least 20 messages in a publishing batch
    public static int AMQP_PUBLISH_CONFIRM_BATCH_NUM_MIN = 10;
    public static int DFT_AMQP_PUBLISH_CONFIRM_BATCH_NUM = 100;
    public static int DFT_AMQP_PUBLISH_CONFIRM_TIMEOUT_MS = 1000;

    public static void pauseCurThreadExec(final int pauseInSec) {
        if (0 < pauseInSec) try {
            Thread.sleep(pauseInSec * 1000L);
        } catch (final InterruptedException ie) {
            ie.printStackTrace();
        }
    }

    public static void messageErrorHandling(final Exception exception, final boolean strictErrorHandling, final String errorMsg) {
        exception.printStackTrace();

        if (strictErrorHandling) throw new RuntimeException(errorMsg + " [ " + exception.getMessage() + " ]");
        pauseCurThreadExec(1);
    }
}

