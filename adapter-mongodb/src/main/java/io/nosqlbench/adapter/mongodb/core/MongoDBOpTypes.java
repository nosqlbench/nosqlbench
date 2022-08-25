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

package io.nosqlbench.adapter.mongodb.core;

public enum MongoDBOpTypes {
    /**
     * Use direct command structure....
     * @see <a href="https://docs.mongodb.com/manual/reference/method/db.runCommand/#command-response">command-response</a>
     */
    command,

    /**
     * @see <a href="https://www.mongodb.com/docs/manual/reference/command/update/#mongodb-dbcommand-dbcmd.update">update</a>
     */
    update,

//    /**
//     * @see <a href="https://www.mongodb.com/docs/manual/reference/command/insert/">insert</a>
//     */
//    insert,
//
//    /**
//     * @see <a href="https://www.mongodb.com/docs/manual/reference/command/delete/">delete</a>
//     */
//    delete,
//
//    /**
//     * @see <a href="https://www.mongodb.com/docs/manual/reference/command/find/">find</a>
//     */
//    find,
//
//    /**
//     * @see <a href="https://www.mongodb.com/docs/manual/reference/command/findAndModify/">findAndModify</a>
//     */
//    findAndModify,
//
//    /**
//     * @see <a href="https://www.mongodb.com/docs/manual/reference/command/getMore/">getMore</a>
//     */
//    getMore

}
