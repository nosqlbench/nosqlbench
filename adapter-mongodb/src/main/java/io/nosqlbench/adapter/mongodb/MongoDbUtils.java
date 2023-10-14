package io.nosqlbench.adapter.mongodb;

import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class MongoDbUtils {
    public static int[] getFieldFromResults(String field, Document document) {
        List<Document> value = document.getList(field, Document.class);
        List<String> keyStrings = new ArrayList<>();
        for (Document matchingVector : value) {
            keyStrings.add(matchingVector.get("key",String.class));
        }
        return keyStrings.stream().mapToInt(Integer::parseInt).toArray();
    }
}
