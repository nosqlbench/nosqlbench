package io.nosqlbench.activitytype.cql.codecsupport;

import com.datastax.driver.core.*;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.stream.Collectors;

public abstract class UserCodecProvider {

    private final static Logger logger = LogManager.getLogger(UserCodecProvider.class);

    public List<UDTTransformCodec> registerCodecsForCluster(
            Session session,
            boolean allowAcrossKeyspaces
    ) {
        List<UDTTransformCodec> typeCodecs = new ArrayList<>();

        List<KeyspaceMetadata> ksMetas = new ArrayList<>(session.getCluster().getMetadata().getKeyspaces());

        for (KeyspaceMetadata keyspace : ksMetas) {

            List<UDTTransformCodec> keyspaceCodecs = registerCodecsForKeyspace(session, keyspace.getName());

            for (UDTTransformCodec typeCodec : keyspaceCodecs) {
                if (typeCodecs.contains(typeCodec) && !allowAcrossKeyspaces) {
                    throw new RuntimeException("codec " + typeCodec + " could be registered" +
                            "in multiple keyspaces, but this is not allowed.");
                }
                typeCodecs.add(typeCodec);
                logger.debug("Found user-provided codec for ks:" + keyspace + ", udt:" + typeCodec);
            }
        }
        return typeCodecs;
    }

    public List<UDTTransformCodec> registerCodecsForKeyspace(Session session, String keyspace) {

        CodecRegistry registry = session.getCluster().getConfiguration().getCodecRegistry();

        List<UDTTransformCodec> codecsForKeyspace = new ArrayList<>();

        KeyspaceMetadata ksMeta = session.getCluster().getMetadata().getKeyspace(keyspace);
        if (ksMeta==null) {
            logger.warn("No metadata for " + keyspace);
            return Collections.emptyList();
        }
        Collection<UserType> typesInKeyspace = ksMeta.getUserTypes();

        List<Class<? extends UDTTransformCodec>> providedCodecClasses = getUDTCodecClasses();

        Map<UserType, Class<? extends UDTTransformCodec>> codecMap = new HashMap<>();

        for (Class<? extends TypeCodec> providedCodecClass : providedCodecClasses) {
            Class<? extends UDTTransformCodec> udtCodecClass = (Class<? extends UDTTransformCodec>) providedCodecClass;

            List<String> targetUDTTypes = getUDTTypeNames(udtCodecClass);
            for (UserType keyspaceUserType : typesInKeyspace) {
                String ksTypeName = keyspaceUserType.getTypeName();
                String globalTypeName = (ksTypeName.contains(".") ? ksTypeName.split("\\.",2)[1] : ksTypeName);
                if (targetUDTTypes.contains(ksTypeName) || targetUDTTypes.contains(globalTypeName)) {
                    codecMap.put(keyspaceUserType, udtCodecClass);
                }
            }
        }

        for (UserType userType : codecMap.keySet()) {
            Class<? extends UDTTransformCodec> codecClass = codecMap.get(userType);
            Class<?> udtJavaType = getUDTJavaType(codecClass);
            UDTTransformCodec udtCodec = instantiate(userType, codecClass, udtJavaType);
            codecsForKeyspace.add(udtCodec);
            registry.register(udtCodec);
            logger.info("registered codec:" + udtCodec);
        }

        return codecsForKeyspace;

    }

    private UDTTransformCodec instantiate(UserType key, Class<? extends UDTTransformCodec> codecClass, Class<?> javaType) {
        try {
            Constructor<? extends UDTTransformCodec> ctor = codecClass.getConstructor(UserType.class, Class.class);
            UDTTransformCodec typeCodec = ctor.newInstance(key, javaType);
            return typeCodec;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private List<Class<? extends UDTTransformCodec>> getUDTCodecClasses() {
        UDTCodecClasses[] annotationsByType = this.getClass().getAnnotationsByType(UDTCodecClasses.class);
        List<Class<? extends UDTTransformCodec>> codecClasses = Arrays.stream(annotationsByType)
                .map(UDTCodecClasses::value)
                .flatMap(Arrays::stream)
                .collect(Collectors.toList());
        return codecClasses;
    }

    /**
     * Allows simple annotation of implementations of this class to use
     * {@code @CQLUserTypeNames({"type1","type2",...}}
     *
     * @param codecClass the UDTTransformCode class which is to be inspected
     * @return THe list of target UDT type names, as defined in CQL
     */
    private List<String> getUDTTypeNames(Class<? extends UDTTransformCodec> codecClass) {
        CQLUserTypeNames[] annotationsByType = codecClass.getAnnotationsByType(CQLUserTypeNames.class);
        List<String> cqlTypeNames = new ArrayList<>();

        for (CQLUserTypeNames cqlUserTypeNames : annotationsByType) {
            cqlTypeNames.addAll(Arrays.asList(cqlUserTypeNames.value()));
        }
        return cqlTypeNames;
    }

    /**
     * Allows simple annotation of implementations of this class to use
     * {@code @UDTJavaType(POJOType.class)}
     *
     * @param codecClass the UDTTransformCode class which is to be inspected
     * @return The class type of the POJO which this codec maps to and from
     */
    private Class<?> getUDTJavaType(Class<? extends UDTTransformCodec> codecClass) {
        UDTJavaType[] annotationsByType = codecClass.getAnnotationsByType(UDTJavaType.class);
        Class<?> javaType = Arrays.stream(annotationsByType)
                .map(UDTJavaType::value)
                .findFirst()
                .orElseThrow(
                        () -> new RuntimeException("Unable to find UDTJavaType annotation for " + codecClass.getCanonicalName())
                );
        return javaType;
    }


}
