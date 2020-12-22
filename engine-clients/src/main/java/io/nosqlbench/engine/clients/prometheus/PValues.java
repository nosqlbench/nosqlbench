package io.nosqlbench.engine.clients.prometheus;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;

public class PValues extends ArrayList<PValue> {
    private final static Gson gson = new GsonBuilder().create();

    public String toString() {
        return gson.toJson(this);
    }
}
