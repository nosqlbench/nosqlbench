package io.nosqlbench.driver.jmx.formats;

import javax.management.*;
import java.util.Map;

public class MBeanInfoConsoleFormat {

    private static final Map<Integer,String> MbeanOpImpacts = Map.of(
            MBeanOperationInfo.ACTION,"ACTION",
            MBeanOperationInfo.ACTION_INFO,"ACTION_INFO",
            MBeanOperationInfo.UNKNOWN,"UNKNOWN",
            MBeanOperationInfo.INFO,"INFO");

    // Not including Descriptors here
    public static String formatAsText(MBeanInfo info, ObjectName objectName) {
        StringBuilder sb = new StringBuilder();
        sb.append("### MBeanInfo for '").append(objectName).append("'\n");

        String className = info.getClassName();
        sb.append("# classname: ").append(className).append("\n");

        String description = info.getDescription();
        sb.append("# ").append(description).append("\n");

        MBeanConstructorInfo[] constructors = info.getConstructors();
        if (constructors.length > 0) {
            sb.append("## constructors:\n");
            for (MBeanConstructorInfo constructor : constructors) {

                String ctorDesc = constructor.getDescription();
                sb.append("# ").append(ctorDesc).append("\n");

                String name = constructor.getName();
                sb.append("# ").append(name).append("(");
                sb.append(pramDetail(constructor.getSignature(), "  "));
                sb.append("  )\n");
//                sb.append("  [").append(descriptorDetail(constructor.getDescriptor())).append("]\n");
            }
        }

        MBeanAttributeInfo[] attributes = info.getAttributes();
        if (attributes.length > 0) {
            sb.append("## attributes:\n");

            for (MBeanAttributeInfo attribute : attributes) {
                String attrDesc = attribute.getDescription();
                String attrName = attribute.getName();
                String attrType = attribute.getType();
                sb.append("# ").append(attrDesc).append("\n");
                sb.append("- '").append(attrName).append("' type=").append(attrType);
                sb.append("readable=").append(attribute.isReadable()).append(" writable=").append(attribute.isWritable()).append(" is_is=").append(attribute.isIs());
                sb.append("\n");
//                sb.append("   [").append(descriptorDetail(attribute.getDescriptor())).append("]\n");

            }
        }

        MBeanNotificationInfo[] notifications = info.getNotifications();
        if (notifications.length > 0) {
            sb.append("## notifications:\n");
            for (MBeanNotificationInfo notification : notifications) {
                String notifName = notification.getName();
                String notifDesc = notification.getDescription();
                String[] notifTypes = notification.getNotifTypes();
                Class<? extends MBeanNotificationInfo> notifClass = notification.getClass();
                sb.append("# ").append(notifDesc).append("\n");
                sb.append("- ").append(notifName).append(" [").append(descriptorDetail(notification.getDescriptor())).append("]\n");

                if (notifTypes.length > 0) {
                    for (String notifType : notifTypes) {
                        sb.append(" - ").append(notifType).append("\n");
                    }
                }
            }
        }

        MBeanOperationInfo[] operations = info.getOperations();
        if (operations.length > 0) {
            sb.append("## operations:\n");
            for (MBeanOperationInfo operation : operations) {
                String opDesc = operation.getDescription();
                String opName = operation.getName();
                MBeanParameterInfo[] opSig = operation.getSignature();
                Class<? extends MBeanOperationInfo> opClass = operation.getClass();

                sb.append("# ").append(opDesc).append("\n");
                sb.append("- ").append(opName).append("(");
                sb.append(pramDetail(operation.getSignature(), " "));
                sb.append(") -> ").append(operation.getReturnType());
                sb.append(" impact=").append(MbeanOpImpacts.get(operation.getImpact())).append("\n");
            }
        }
        return sb.toString();
    }

    private static String descriptorDetail(Descriptor descriptor) {
        StringBuilder sb = new StringBuilder();
        sb.append("valid=").append(descriptor.isValid());
        String[] fieldNames = descriptor.getFieldNames();
        for (String field : fieldNames) {
            sb.append(" ").append(field).append("=").append(descriptor.getFieldValue(field));
        }
        return sb.toString();
    }

    private static String pramDetail(MBeanParameterInfo[] signature, String prefix) {
        StringBuilder sb = new StringBuilder();

        for (MBeanParameterInfo paramInfo : signature) {
            String desc = paramInfo.getDescription();
            if (desc != null) {
                sb.append(prefix).append(" # ").append(desc).append("\n");
            }
            sb.append(prefix).append(" - ").append(paramInfo.getName()).append("\n");
        }
        return sb.toString();

    }
}
