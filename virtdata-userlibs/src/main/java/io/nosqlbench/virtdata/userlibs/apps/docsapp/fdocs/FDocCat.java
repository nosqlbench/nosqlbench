package io.nosqlbench.virtdata.userlibs.apps.docsapp.fdocs;

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
