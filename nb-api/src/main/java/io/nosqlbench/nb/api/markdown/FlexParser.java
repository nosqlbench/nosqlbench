package io.nosqlbench.nb.api.markdown;

import com.vladsch.flexmark.ext.yaml.front.matter.YamlFrontMatterExtension;
import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import com.vladsch.flexmark.parser.Parser;

import java.util.List;

public class FlexParser {
    public static Parser parser = Parser.builder().extensions(List.of(YamlFrontMatterExtension.create())).build();
    public static FlexmarkHtmlConverter converter = FlexmarkHtmlConverter.builder().build();
}
