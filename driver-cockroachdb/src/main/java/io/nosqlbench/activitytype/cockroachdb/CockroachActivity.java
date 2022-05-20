package io.nosqlbench.activitytype.cockroachdb;

/*
 * Copyright (c) 2022 nosqlbench
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import io.nosqlbench.activitytype.jdbc.api.JDBCActivity;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;
import java.util.Arrays;

public class CockroachActivity extends JDBCActivity {
    private static final Logger LOGGER = LogManager.getLogger(CockroachActivity.class);

    public CockroachActivity(ActivityDef activityDef) {
        super(activityDef);
    }

    // TODO provide an error handler with sane defaults including
    //   * retry on 40001 SQL state code (CockroachDB txn retry)
    //   * retry (implement exponential, to avoid stampeding herd) on timeout getting connection from connection pool
    //
    //@Override
    //public NBErrorHandler getErrorHandler() {
    //}

    @Override
    protected DataSource newDataSource() {
        PGSimpleDataSource ds = new PGSimpleDataSource();

        // serverName is required
        String serverName = getParams().
            getOptionalString("serverName").
            orElseThrow(() -> new RuntimeException("serverName parameter required"));

        // portNumber, databaseName, user, password are optional
        Integer portNumber = getParams().getOptionalInteger("portNumber").orElse(26257);
        String databaseName = getParams().getOptionalString("databaseName").orElse(null);
        String user = getParams().getOptionalString("user").orElse(null);
        String password = getParams().getOptionalString("password").orElse(null);

        ds.setServerNames(new String[]{serverName});
        ds.setPortNumbers(new int[]{portNumber});
        if (databaseName != null) {
            ds.setDatabaseName(databaseName);
        }
        if (user != null) {
            ds.setUser(user);
        }
        if (password != null) {
            ds.setPassword(password);
        }

        LOGGER.debug("Final DataSource fields:"
            + " serverNames=" + Arrays.toString(ds.getServerNames())
            + " portNumbers=" + Arrays.toString(ds.getPortNumbers())
            + " databaseName=" + ds.getDatabaseName()
            + " user=" + ds.getUser()
            + " password=" + ds.getPassword());

        return ds;
    }
}
