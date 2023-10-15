package io.nosqlbench.adapter.mongodb;

import com.mongodb.client.MongoCursor;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class MongoDbUtils {
    public static int[] getFieldFromResults(String field, Document document) {
        Document _cursor = document.get("cursor", Document.class);
        List<Document> _firstBatch = _cursor.getList("firstBatch", Document.class);
        List<String> keyStrings = new ArrayList<>();
        for (Document matchingVector : _firstBatch) {
            keyStrings.add(matchingVector.get("key",String.class));
        }
        return keyStrings.stream().mapToInt(Integer::parseInt).toArray();
    }
}
