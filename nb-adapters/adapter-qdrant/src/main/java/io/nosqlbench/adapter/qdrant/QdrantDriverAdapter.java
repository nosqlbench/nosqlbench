/*
 * Copyright (c) 2020-2024 nosqlbench
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

package io.nosqlbench.adapter.qdrant;

import io.nosqlbench.adapter.qdrant.ops.QdrantBaseOp;
import io.nosqlbench.adapters.api.activityimpl.OpMapper;
import io.nosqlbench.adapters.api.activityimpl.uniform.BaseDriverAdapter;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.nb.annotations.Service;
import io.nosqlbench.nb.api.components.core.NBComponent;
import io.nosqlbench.nb.api.config.standard.NBConfigModel;
import io.nosqlbench.nb.api.config.standard.NBConfiguration;
import io.nosqlbench.nb.api.labels.NBLabels;

import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.LongFunction;

import static io.nosqlbench.adapter.qdrant.QdrantAdapterUtils.QDRANT;

@Service(value = DriverAdapter.class, selector = QDRANT)
public class QdrantDriverAdapter extends BaseDriverAdapter<QdrantBaseOp<?,?>, QdrantSpace> {

    public QdrantDriverAdapter(NBComponent parentComponent, NBLabels labels) {
        super(parentComponent, labels);
    }

    @Override
    public OpMapper<QdrantBaseOp<?,?>,QdrantSpace> getOpMapper() {
        return new QdrantOpMapper(this);
    }

    @Override
    public LongFunction<QdrantSpace> getSpaceInitializer(NBConfiguration cfg) {
        return (long s) -> new QdrantSpace(this, s, cfg);
    }

    @Override
    public NBConfigModel getConfigModel() {
        return super.getConfigModel().add(QdrantSpace.getConfigModel());
    }
}
