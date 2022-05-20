package io.nosqlbench.activitytype.cmds;

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


import java.net.http.HttpRequest;
import java.util.regex.Pattern;

public class HttpOp {

    public final Pattern ok_status;
    public final Pattern ok_body;
    public final HttpRequest request;

    public HttpOp(HttpRequest request, Pattern ok_status, Pattern ok_body) {
        this.request = request;
        this.ok_status = ok_status;
        this.ok_body = ok_body;
    }
}
