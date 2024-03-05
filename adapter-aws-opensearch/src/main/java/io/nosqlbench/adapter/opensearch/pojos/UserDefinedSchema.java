/*
 * Copyright (c) 2024 nosqlbench
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

package io.nosqlbench.adapter.opensearch.pojos;

public class UserDefinedSchema {
    private float[] vectorValues;
    private String recordKey;
    private String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public UserDefinedSchema() {
    }

    public UserDefinedSchema(float[] value, String key, String type) {
        this.vectorValues = value;
        this.recordKey = key;
        this.type = type;
    }

    public float[] getVectorValues() {
        return vectorValues;
    }

    public void setVectorValues(float[] vectorValues) {
        this.vectorValues = vectorValues;
    }

    @Override
    public String toString() {
        return "{" + "values=" + vectorValues + "}";
    }

    public String getRecordKey() {
        return recordKey;
    }

    public void setRecordKey(String recordKey) {
        this.recordKey = recordKey;
    }
}
