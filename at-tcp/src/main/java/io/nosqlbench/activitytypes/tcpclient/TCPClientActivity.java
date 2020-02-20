/*
 *
 *    Copyright 2016 jshook
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * /
 */

package io.nosqlbench.activitytypes.tcpclient;

import io.nosqlbench.activitytypes.stdout.StdoutActivity;
import io.nosqlbench.activityimpl.ActivityDef;
import io.nosqlbench.util.SSLKsFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.SocketFactory;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class TCPClientActivity extends StdoutActivity {
    private final static Logger logger = LoggerFactory.getLogger(TCPClientActivity.class);

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
            socketFactory = SSLKsFactory.get().createSocketFactory(activityDef);
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
