package io.nosqlbench.activitytype.cqld4.codecsupport;

import com.datastax.oss.driver.api.core.data.UdtValue;
import com.datastax.oss.driver.api.core.type.codec.MappingCodec;
import com.datastax.oss.driver.api.core.type.codec.TypeCodec;
import com.datastax.oss.driver.api.core.type.reflect.GenericType;
import edu.umd.cs.findbugs.annotations.NonNull;

public abstract class UDTTransformCodec<T> extends MappingCodec<T, UdtValue> {

    //    protected UserType userType;

    public UDTTransformCodec(
        @NonNull TypeCodec<T> innerCodec,
        @NonNull GenericType<UdtValue> outerJavaType
    ) {
        super(innerCodec, outerJavaType);
    }

//
//    public UDTTransformCodec(GenericType userType, Class<T> javaType) {
//        super(TypeCodec.userType(userType), javaType);
//        this.userType = userType;
//    }

//    public UserType getUserType() {
//        return userType;
//    }


}
