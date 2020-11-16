package io.nosqlbench.activitytype.cqld4.codecsupport;

import com.datastax.oss.driver.api.core.session.Session;
import com.datastax.oss.driver.api.core.type.codec.registry.CodecRegistry;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public class UDTCodecInjector {
    private final static Logger logger = LogManager.getLogger(UDTCodecInjector.class);

    private final List<UserCodecProvider> codecProviders = new ArrayList<>();

    public void injectUserProvidedCodecs(Session session, boolean allowAcrossKeyspaces) {

        CodecRegistry registry = session.getContext().getCodecRegistry();

        ServiceLoader<UserCodecProvider> codecLoader = ServiceLoader.load(UserCodecProvider.class);

        for (UserCodecProvider userCodecProvider : codecLoader) {
            codecProviders.add(userCodecProvider);
        }

        for (UserCodecProvider codecProvider : codecProviders) {
            codecProvider.registerCodecsForCluster(session,true);
        }
    }
}
