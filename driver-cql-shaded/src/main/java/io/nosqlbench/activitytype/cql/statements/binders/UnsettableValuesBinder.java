package io.nosqlbench.activitytype.cql.statements.binders;

import com.datastax.driver.core.*;
import io.nosqlbench.virtdata.api.bindings.VALUE;
import io.nosqlbench.virtdata.core.bindings.ValuesArrayBinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.List;

public class UnsettableValuesBinder implements ValuesArrayBinder<PreparedStatement, Statement> {
    private final static Logger logger = LoggerFactory.getLogger(UnsettableValuesBinder.class);

    private final Session session;
    private final CodecRegistry codecRegistry;
    private final ProtocolVersion protocolVersion;

    public UnsettableValuesBinder(Session session) {
        this.session = session;
        this.codecRegistry = session.getCluster().getConfiguration().getCodecRegistry();
        this.protocolVersion = this.session.getCluster().getConfiguration().getProtocolOptions().getProtocolVersion();
    }


    // TODO: Allow for warning when nulls are passed and they aren't expected
    @Override
    public Statement bindValues(PreparedStatement preparedStatement, Object[] objects) {
        int i=-1;
        try {
            BoundStatement boundStmt = preparedStatement.bind();
            List<ColumnDefinitions.Definition> defs = preparedStatement.getVariables().asList();
            for (i = 0; i < objects.length; i++) {
                Object value = objects[i];
                if (VALUE.unset != value) {
                    if (null==value) {
                        boundStmt.setToNull(i);
                    } else {
                        DataType cqlType = defs.get(i).getType();
                        TypeCodec<Object> codec = codecRegistry.codecFor(cqlType, value);
                        ByteBuffer serialized = codec.serialize(value, protocolVersion);
                        boundStmt.setBytesUnsafe(i,serialized);
                    }
                }
            }
            return boundStmt;
        } catch (Exception e) {
            String typNam = (objects[i]==null ? "NULL" : objects[i].getClass().getCanonicalName());
            logger.error("Error binding column " + preparedStatement.getVariables().asList().get(i).getName() + " with class " + typNam, e);
            throw e;
//            StringBuilder sb = new StringBuilder();
//            sb.append("Error binding objects to prepared statement directly, falling back to diagnostic binding layer:");
//            sb.append(Arrays.toString(objects));
//            logger.warn(sb.toString(),e);
//            DiagnosticPreparedBinder diag = new DiagnosticPreparedBinder();
//            return diag.bindValues(preparedStatement, objects);
        }
    }

//        static void setObject(Session session, BoundStatement bs, int index, Object value) {
//
//            DataType cqlType = bs.preparedStatement().getVariables().getType(index);
//
//            CodecRegistry codecRegistry = session.getCluster().getConfiguration().getCodecRegistry();
//            ProtocolVersion protocolVersion =
//                    session.getCluster().getConfiguration().getProtocolOptions().getProtocolVersion();
//
//            TypeCodec<Object> codec = codecRegistry.codecFor(cqlType, value);
//            bs.setBytesUnsafe(index, codec.serialize(value, protocolVersion));
//        }


}
