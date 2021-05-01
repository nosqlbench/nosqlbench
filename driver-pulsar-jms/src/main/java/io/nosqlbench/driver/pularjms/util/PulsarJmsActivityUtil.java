package io.nosqlbench.driver.pularjms.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.client.impl.schema.generic.GenericAvroSchema;
import org.apache.pulsar.common.schema.SchemaInfo;
import org.apache.pulsar.common.schema.SchemaType;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.stream.Collectors;

public class PulsarJmsActivityUtil {

    private final static Logger logger = LogManager.getLogger(PulsarJmsActivityUtil.class);

    // Supported message operation types
    public enum OP_TYPES {
        MSG_SEND("msg_send"),
        MSG_READ("msg_read");

        public final String label;

        OP_TYPES(String label) {
            this.label = label;
        }
    }
    public static boolean isValidClientType(String type) {
        return Arrays.stream(OP_TYPES.values()).anyMatch(t -> t.label.equals(type));
    }

    public enum DOC_LEVEL_PARAMS {
        TOPIC_URI("topic_uri"),
        ASYNC_API("async_api");

        public final String label;

        DOC_LEVEL_PARAMS(String label) {
            this.label = label;
        }
    }
    public static boolean isValidDocLevelParam(String param) {
        return Arrays.stream(OP_TYPES.values()).anyMatch(t -> t.label.equals(param));
    }

    public static String encode(String... strings) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String str : strings) {
            if (!StringUtils.isBlank(str))
                stringBuilder.append(str).append("::");
        }

        String concatenatedStr =
            StringUtils.substringBeforeLast(stringBuilder.toString(), "::");

        return Base64.getEncoder().encodeToString(concatenatedStr.getBytes());
    }
}

