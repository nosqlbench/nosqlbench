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

package io.nosqlbench.adapter.kafka.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public enum KafkaAdapterUtil {
    ;
    public static final String MSG_SEQUENCE_NUMBER = "sequence_number";
    private static final Logger logger = LogManager.getLogger(KafkaAdapterUtil.class);

    public static String DFT_CONSUMER_GROUP_NAME_PREFIX = "nbKafkaGrp";
    public static String DFT_TOPIC_NAME_PREFIX = "nbKafkaTopic";

    ///////
    // Valid document level parameters for JMS NB yaml file
    public enum DOC_LEVEL_PARAMS {
        // Blocking message producing or consuming
        ASYNC_API("async_api"),
        SEQERR_SIMU("seqerr_simu"),
        E2E_STARTING_TIME_SOURCE("e2e_starting_time_source"),
        SEQ_TRACKING("seq_tracking");
        public final String label;

        DOC_LEVEL_PARAMS(final String label) {
            this.label = label;
        }
    }

    public static final String NB_MSG_SEQ_PROP = "NBMsgSeqProp";
    public static final String NB_MSG_SIZE_PROP = "NBMsgSize";

    // Get simplified NB thread name
    public static String getSimplifiedNBThreadName(final String fullThreadName) {
        assert StringUtils.isNotBlank(fullThreadName);

        if (StringUtils.contains(fullThreadName, '/')) {
            return StringUtils.substringAfterLast(fullThreadName, "/");
        }
        return fullThreadName;
    }


    public static Map<String, String> convertJsonToMap(final String jsonStr) throws Exception {
        final ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(jsonStr, new TypeReference<Map<String, String>>(){});
    }

    public static List<Object> convertJsonToObjList(final String jsonStr) throws Exception {
        final ObjectMapper mapper = new ObjectMapper();
        return Arrays.asList(mapper.readValue(jsonStr, Object[].class));
    }

    public static void pauseCurThreadExec(final int pauseInSec) {
        if (0 < pauseInSec) try {
            Thread.sleep(pauseInSec * 1000L);
        } catch (final InterruptedException ie) {
            ie.printStackTrace();
        }
    }

    public static int getStrObjSize(final String strObj) {
        // << https://docs.oracle.com/javase/6/docs/api/java/lang/String.html >>
        // A String represents a string in the UTF-16 format ...
        return strObj.getBytes(StandardCharsets.UTF_16).length;
    }

    public static void messageErrorHandling(final Exception exception, final boolean strictErrorHandling, final String errorMsg) {
        exception.printStackTrace();

        if (strictErrorHandling) throw new RuntimeException(errorMsg + " [ " + exception.getMessage() + " ]");
        pauseCurThreadExec(1);
    }
}

