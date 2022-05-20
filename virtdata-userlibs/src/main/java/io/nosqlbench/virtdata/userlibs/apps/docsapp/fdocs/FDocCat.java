package io.nosqlbench.virtdata.userlibs.apps.docsapp.fdocs;

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


import java.util.*;

public class FDocCat implements Iterable<FDocFuncs> {
    private final Map<String, FDocFuncs> docsByFuncName= new HashMap<>();
    private final String categoryName;

    public FDocCat(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void addFunctionDoc(FDocFunc FDocFunc) {
        String name = FDocFunc.getPackageName() + "." + FDocFunc.getClassName();
        FDocFuncs fDocFuncs = docsByFuncName.computeIfAbsent(FDocFunc.getClassName(),
            FDocFuncs::new);
        fDocFuncs.addFunctionDoc(FDocFunc);
    }

    public List<FDocFuncs> getFunctionDocsList() {
        return new ArrayList<>(docsByFuncName.values());

    }

    @Override
    public Iterator<FDocFuncs> iterator() {
        ArrayList<FDocFuncs> fdocs = new ArrayList<>(docsByFuncName.values());
        fdocs.sort(Comparator.comparing(FDocFuncs::getFunctionName));
        return fdocs.iterator();
    }
}
