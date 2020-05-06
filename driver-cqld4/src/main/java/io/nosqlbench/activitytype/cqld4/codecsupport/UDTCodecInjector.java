package io.nosqlbench.activitytype.cqld4.codecsupport;

import com.datastax.oss.driver.api.core.session.Session;
import com.datastax.oss.driver.api.core.type.codec.registry.CodecRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public class UDTCodecInjector {
    private final static Logger logger = LoggerFactory.getLogger(UDTCodecInjector.class);

    private List<UserCodecProvider> codecProviders = new ArrayList<>();
    private List<UserType> userTypes = new ArrayList<>();

    public void injectUserProvidedCodecs(Session session, boolean allowAcrossKeyspaces) {

        CodecRegistry registry = session.getCluster().getConfiguration().getCodecRegistry();

        ServiceLoader<UserCodecProvider> codecLoader = ServiceLoader.load(UserCodecProvider.class);

        for (UserCodecProvider userCodecProvider : codecLoader) {
            codecProviders.add(userCodecProvider);
        }

        for (UserCodecProvider codecProvider : codecProviders) {
            codecProvider.registerCodecsForCluster(session,true);
        }
    }
}
