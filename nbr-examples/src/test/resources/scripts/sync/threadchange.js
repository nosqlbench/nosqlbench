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

scenario.start('type=diag;alias=threadchange;cycles=0..60000;threads=1;interval=2000;modulo=1000000;rate=1000');
activities.threadchange.threads=1;
print("threads now " + activities.threadchange.threads);
print('waiting 500 ms');
activities.threadchange.threads=5;
print("threads now " + activities.threadchange.threads);
scenario.stop('threadchange');
