/*
 * Copyright (c) 2022 nosqlbench
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

package io.nosqlbench.activitytype.tcpclient;

import io.nosqlbench.activitytype.stdout.StdoutActivity;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.engine.api.util.SSLKsFactory;
import io.nosqlbench.nb.api.config.standard.NBConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.SocketFactory;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class TCPClientActivity extends StdoutActivity {
    private final static Logger logger = LogManager.getLogger(TCPClientActivity.class);

    public TCPClientActivity(ActivityDef activityDef) {
        super(activityDef);
    }

    @Override
    public void onActivityDefUpdate(ActivityDef activityDef) {
        super.onActivityDefUpdate(activityDef);
    }

    @Override
    protected PrintWriter createPrintWriter() {

        SocketFactory socketFactory = SocketFactory.getDefault();
        boolean sslEnabled = activityDef.getParams().getOptionalBoolean("ssl").orElse(false);
        if (sslEnabled) {
            NBConfiguration sslCfg = SSLKsFactory.get().getConfigModel().extractConfig(activityDef.getParams());
            socketFactory = SSLKsFactory.get().createSocketFactory(sslCfg);
        }

        String host = getActivityDef().getParams().getOptionalString("host").orElse("localhost");
        int port = getActivityDef().getParams().getOptionalInteger("port").orElse(12345);

        try {
            Socket socket = socketFactory.createSocket(host, port);
            logger.info("connected to " + socket.toString());
            return new PrintWriter(socket.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException("Error opening socket:" + e, e);
        }
    }


}
