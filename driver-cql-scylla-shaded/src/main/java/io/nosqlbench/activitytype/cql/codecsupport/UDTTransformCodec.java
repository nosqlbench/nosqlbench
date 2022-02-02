package io.nosqlbench.activitytype.cql.codecsupport;

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
