package io.nosqlbench.adapters.stdout;

import io.nosqlbench.nb.api.config.standard.*;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.Writer;

public class StdoutSpace {

    Writer writer;
    private PrintWriter console;

    public StdoutSpace(NBConfiguration cfg) {
        String filename = cfg.get("filename");
        this.writer = createPrintWriter(filename);
    }

    public void write(String text) {
        try {
            writer.write(text);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected Writer createPrintWriter(String filename) {
        PrintWriter pw;
        if (filename.equalsIgnoreCase("stdout")) {
            pw = getConsoleOut();
        } else {
            try {
                pw = new PrintWriter(filename);
                pw.print("");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                throw new RuntimeException("Error initializing printwriter:" + e, e);
            }
        }
        return pw;
    }

    public static NBConfigModel getConfigModel() {
        return ConfigModel.of(StdoutSpace.class)
            .add(
                Param.defaultTo("filename","stdout")
                    .setDescription("this is the name of the output file. If 'stdout', output is sent to stdout, not a file.")
            )
            .add(
                Param.defaultTo("newline",true)
                    .setDescription("whether to automatically add a missing newline to the end of any output\n")
            )
            .add(
                Param.optional("format")
                    .setRegex("csv|readout|json|inlinejson|assignments|diag")
                    .setDescription("Which format to use.\n" +
                        "If provided, the format will override any statement formats provided by the YAML")
            )
            .add(
                Param.defaultTo("bindings","doc")
                    .setDescription("This is a simple way to specify a filter for the names of bindings that you want to use.\n" +
                        "If this is 'doc', then all the document level bindings are used. If it is any other value, it is taken\n" +
                        "as a pattern (regex) to subselect a set of bindings by name. You can simply use the name of a binding\n" +
                        "here as well.")

            )
            .asReadOnly();
    }

    public synchronized PrintWriter getConsoleOut() {
        if (this.console == null) {
            this.console = new PrintWriter(System.out);
        }
        return this.console;
    }

}
