package com.datastax.ebdrivers.dsegraph.statements;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BindableGraphStatementsTemplate {
    private List<ReadyGraphStatementTemplate> templateList = new ArrayList<>();

    public void addTemplate(ReadyGraphStatementTemplate template) {
        this.templateList.add(template);
    }

    public List<BindableGraphStatement> resolve() {
        return templateList.stream().map(ReadyGraphStatementTemplate::resolve)
                .collect(Collectors.toList());
    }

    public int size() {
        return templateList.size();
    }
}
