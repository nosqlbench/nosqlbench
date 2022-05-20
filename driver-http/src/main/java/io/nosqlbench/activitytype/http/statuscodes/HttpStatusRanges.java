package io.nosqlbench.activitytype.http.statuscodes;

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


enum HttpStatusRanges {
    Informational("INFORMATIONAL", 100, 199, "Request received, continuing process"),
    Success("SUCCESS",200, 299, "Request successfully received, understood, and accepted"),
    Redirection("REDIRECTION", 300, 399, "Further action must be taken in order to complete the request."),
    Client_Error("CLIENT_ERROR",400, 499, "The request contains bad syntax or cannot be fulfilled."),
    Server_Error("SERVER_ERROR",500, 599, "The server failed to fulfill an apparently valid request."),
    Unknown("UNKNOWN_ERROR",0,0,"This error type is not known based on IANA registered HTTP status codes.");

    private final String name;
    private final String description;
    private final int min;
    private final int max;

    HttpStatusRanges(String name, int min, int max, String description) {
        this.name = name;
        this.min = min;
        this.max = max;
        this.description = description;
    }

    public static HttpStatusRanges valueOfCode(int code) {
        for (HttpStatusRanges value : HttpStatusRanges.values()) {
            if (code >= value.min && code <= value.max) {
                return value;
            }
        }
        return HttpStatusRanges.Unknown;
    }

    public String toString() {
        return this.name + " (" + this.description + ")";
    }
}
