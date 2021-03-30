package io.nosqlbench.activitytype.cql.codecsupport;

import com.datastax.driver.core.CodecRegistry;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.UserType;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public class UDTCodecInjector {
    private final static Logger logger = LogManager.getLogger(UDTCodecInjector.class);

    private final List<UserCodecProvider> codecProviders = new ArrayList<>();
    private final List<UserType> userTypes = new ArrayList<>();

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
