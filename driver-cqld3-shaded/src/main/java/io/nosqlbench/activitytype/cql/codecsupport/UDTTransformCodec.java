package io.nosqlbench.activitytype.cql.codecsupport;

/*
 * Copyright (c) 2022 nosqlbench
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import com.datastax.driver.core.TypeCodec;
import com.datastax.driver.core.UDTValue;
import com.datastax.driver.core.UserType;
import com.datastax.driver.extras.codecs.MappingCodec;

public abstract class UDTTransformCodec<T> extends MappingCodec<T,UDTValue> {

    protected UserType userType;

    public UDTTransformCodec(UserType userType, Class<T> javaType) {
        super(TypeCodec.userType(userType), javaType);
        this.userType = userType;
    }

    public UserType getUserType() {
        return userType;
    }


}
