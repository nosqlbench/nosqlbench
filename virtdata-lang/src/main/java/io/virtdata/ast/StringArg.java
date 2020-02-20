package io.virtdata.ast;

public class StringArg implements ArgType {
    private final String rawEscapedText;
    private final String unEscapedText;

    public StringArg(String rawEscapedText) {
        this.rawEscapedText = rawEscapedText;
        this.unEscapedText = unEscape(rawEscapedText);
    }

    private static String unEscape(String value) {
        String innervalue= value.substring(1, value.length() - 1);
        if (value.startsWith("\"")) {
            innervalue = innervalue.replaceAll("\\\\(.)","$1");
        }
        return innervalue;
    }

    public String getRawValue() {
        return rawEscapedText;
    }
    public String getStringValue() {
        return unEscapedText;
    }

    @Override
    public String toString() {
        return "'"+unEscapedText+"'";
    }
}
