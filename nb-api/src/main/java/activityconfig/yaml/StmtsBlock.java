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

package activityconfig.yaml;

import activityconfig.MultiMapLookup;
import activityconfig.rawyaml.RawStmtDef;
import activityconfig.rawyaml.RawStmtsBlock;
import io.nosqlbench.util.Tagged;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class StmtsBlock implements Tagged, Iterable<StmtDef> {

    private final static String NameToken = "name";
    private final static String StmtToken = "stmt";
    private final RawStmtsBlock rawStmtsBlock;
    private StmtsDoc rawStmtsDoc;
    private int blockIdx;


    public StmtsBlock(RawStmtsBlock rawStmtsBlock, StmtsDoc rawStmtsDoc, int blockIdx) {
        this.rawStmtsBlock = rawStmtsBlock;
        this.rawStmtsDoc = rawStmtsDoc;
        this.blockIdx = blockIdx;
    }

    public List<StmtDef> getStmts() {
        
        List<StmtDef> rawStmtDefs = new ArrayList<>();
        List<RawStmtDef> statements = rawStmtsBlock.getRawStmtDefs();

        for (int i = 0; i < statements.size(); i++) {
            rawStmtDefs.add(
                    new StmtDef(this,statements.get(i))
            );
        }
        return rawStmtDefs;
    }

    public String getName() {
        StringBuilder sb = new StringBuilder();
        if (!rawStmtsDoc.getName().isEmpty()) {
            sb.append(rawStmtsDoc.getName()).append("--");
        }
        if (!rawStmtsBlock.getName().isEmpty()) {
            sb.append(rawStmtsBlock.getName());
        } else {
            sb.append("block").append(blockIdx);
        }
        return sb.toString();
    }

    public Map<String, String> getTags() {
        return new MultiMapLookup(rawStmtsBlock.getTags(), rawStmtsDoc.getTags());
    }

    public Map<String, String> getParams() {
        return new MultiMapLookup(rawStmtsBlock.getParams(), rawStmtsDoc.getParams());
    }

    public Map<String, String> getBindings() {
        return new MultiMapLookup(rawStmtsBlock.getBindings(), rawStmtsDoc.getBindings());
    }

    @Override
    public Iterator<StmtDef> iterator() {
        return getStmts().iterator();
    }
}