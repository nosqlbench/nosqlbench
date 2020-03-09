---
title: 06 More on Statements
weight: 06
---

# More on Statements

## Statement Delimiting

Sometimes, you want to specify the text of a statement in different ways. Since statements are strings, the simplest way for small statements is in double quotes. If you need to express a much longer statement with special characters an newlines, then you can use YAML's literal block notation (signaled by the '|' character) to do so:

```yaml
statements:
 - |
  This is a statement, and the file format doesn't
  know how statements will be used!
 - |
  submit job {alpha} on queue {beta} with options {gamma};
```

Notice that the block starts on the following line after the pipe symbol. This is a very popular form in practice because it treats the whole block exactly as it is shown, except for the initial indentations, which are removed.

Statements in this format can be raw statements, statement templates, or anything that is appropriate for the specific activity type they are being used with. Generally, the statements should be thought of as a statement form that you want to use in your activity -- something that has place holders for data bindings. These place holders are called *named anchors*. The second line above is an example of a statement template, with anchors that can be replaced by data for each cycle of an activity.

There is a variety of ways to represent block statements, with folding, without, with the newline removed, with it retained, with trailing newlines trimmed or not, and so forth. For a more comprehensive guide on the YAML conventions regarding multi-line blocks, see [YAML Spec 1.2, Chapter 8, Block Styles](http://www.yaml.org/spec/1.2/spec.html#Block)

## Statement Sequences

To provide a degree of flexibility to the user for statement definitions,
multiple statements may be provided together as a sequence.

```yaml
# a list of statements
statements:
 - "This a statement."
 - "The file format doesn't know how statements will be used."
 - "submit job {job} on queue {queue} with options {options};"

# an ordered map of statements by name
statements:
 name1: statement one
 name2: "statement two"
```

In the first form, the names are provided automatically by the YAML loader. In the second form, they are specified as ordered map keys.

## Statement Properties

You can also configure individual statements with named properties, using the **statement properties** form:

```yaml
# a list of statements with properties
statements:
 - name: name1
   stmt: statement one
 - name: name2
   stmt: statement two
```

This is the most flexible configuration format at the statement level. It is also the most verbose. Because this format names each property of the statement, it allows for other properties to be defined at this level as well. This includes all of the previously described configuration elements: `name`, `bindings`, `params`, `tags`, and additionally `stmt`. A detailed example follows:

```yaml
statements:
 - name: foostmt
   stmt: "{alpha},{beta}\n"
   bindings:
    beta: Combinations('COMBINATIONS;')
   params:
    parm1: pvalue1
   tags:
    tag1: tvalue1
   freeparam3: a value, as if it were assigned under the params block.
```

In this case, the values for `bindings`, `params`, and `tags` take precedence, overriding those set by the enclosing block or document or activity when the names match. Parameters called **free parameters** are allowed here, such as `freeparam3`. These are simply values that get assigned to the params map once all other processing has completed.

It is possible to mix the **`<name>: <statement>`** form as above in the example for mapping statement by name, so long as some specific rules are followed. An example, which is equivalent to the above:

```yaml
statements:
 - foostmt: "{alpha},{beta}\n"
   parm1: pvalue1
   bindings:
    beta: Combinations('COMBINATIONS;')
   tags:
    tag1: tvalue1
```

The rules:

1. You must avoid using both the name property and the initial
   **`<name>: <statement>`** together. Doing so will cause an error to be thrown.
2. Do not use the **`<name>: <statement>`** form in combination with a
   **`stmt: <statement>`** property. It is not possible to detect if this occurs. Use caution if you choose to mix these forms.

As explained above, `parm1: pvalue1` is a *free parameter*, and is simply short-hand for setting values in the params map for the statement.

### Per-Statement Format

It is indeed possible to use any of the three statement formats within each entry of a statement sequence:

```yaml
statements:
 - first statement body
 - second: second statement body
 - name: statement3
   stmt: third statement body
 - forth: fourth statement body
   freeparam1: freeparamvalue1
   tags:
    type: preload
```

Specifically, the first statement is a simple statement body, the second is a named statement (via free param `<name>: statement` form), the third is a statement config map, and the fourth is a combination of the previous two.

The above is valid nosqlbench YAML, although a reader would need
to know about the rules explained above in order to really make sense of it. For most cases, it is best to follow one format convention, but there is flexibility for overrides and naming when you need it.
