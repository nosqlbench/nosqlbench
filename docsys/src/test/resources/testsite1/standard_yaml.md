---
title: YAML Config
weight: 33
menu:
  main:
    parent: User Guide
    identifier: configfiles
    weight: 15
---

A standard YAML configuration format is
provided that makes it easy to use for any activity that requires statements,
tags, parameters and data bindings. In practice, any useful activity types have
needed these. This section describes the standard YAML format and how to use it.

A valid config file for an activity consists of statements, parameters for those
statements, bindings for the data to use with those statements, and tags for
selecting statements for an activity. In essence, the config format is *all about
configuring statements*. Every other element in the config format is in some way
modifying or otherwise helping create statements to be used in an activity.

## Statements

Statements are the single most important part of a YAML config:

```yaml
# a single statement
statement: a single statement body
```

This is a valid YAML in and of itself. It may not be valid for a particular
activity type, but that is up to each activity type to decide. That is because
the contents of the YAML will matter to each activity type in a different way.
Thus, each activity type determines what a statement means, and how it will be
used. The format is merely concerned with structure and very general semantics.

In the example above, the statement is automatically named according to its
location within the YAML document.

## Bindings

Procedural data generation is built-in to nosqlbench. Bindings are now
supported as a core concept. You can add a bindings section like this:

```yaml
bindings:
 alpha: Identity()
 beta: NumberNameToString()
 gamma: Combinations('0-9A-F;0-9;A-Z;_;p;r;o;')
 delta: WeightedStrings('one:1;six:6;three:3;')
```

Notice that bindings are represented as a map. The bindings above *mostly* match
up with the named anchors in the statement list above. There is one extra
binding, but this is ok. However, if the statement included named anchors for which no
binding was defined, an error would occur.

```
This is important for activity type designers to observe: When statement bindings
are paired with statements, a binding should not be used directly unless it is
paired with a matching anchor name in the statement. This allows activity and
scenario designers to keep a library of data binding recipes in their
configurations without incurring the cost of extraneous binding evaluations.
```

Still, the above statement block is a valid config file. It may or may not be
valid for a given activity type. The 'stdout' activity will synthesize a
statement template from the provided bindings if needed, so this is valid:

```text
[test]$ cat > stdout-test.yaml
    bindings:
     alpha: Identity()
     beta: NumberNameToString()
     gamma: Combinations('0-9A-F;0-9;A-Z;_;p;r;o;')
     delta: WeightedStrings('one:1;six:6;three:3;')
# EOF (control-D in your terminal)

[test]$ ./eb run driver=stdout yaml=stdout-test cycles=10
0,zero,00A_pro,six
1,one,00B_pro,six
2,two,00C_pro,three
3,three,00D_pro,three
4,four,00E_pro,six
5,five,00F_pro,six
6,six,00G_pro,six
7,seven,00H_pro,six
8,eight,00I_pro,six
9,nine,00J_pro,six
```

If you combine the statement section and the bindings sections above into one
activity yaml, you get a slightly different result, as the bindings apply
to the statements that are provided, rather than creating a default statement
for the bindings. See the example below:

```text
[test]$ cat > stdout-test.yaml
statements:
 - |
  This is a statement, and the file format doesn't
  know how statements will be used!
 - |
  submit job {alpha} on queue {beta} with options {gamma};
bindings:
 alpha: Identity()
 beta: NumberNameToString()
 gamma: Combinations('0-9A-F;0-9;A-Z;_;p;r;o;')
 delta: WeightedStrings('one:1;six:6;three:3;')
# EOF (control-D in your terminal)

[test]$ ./eb run driver=stdout yaml=stdout-test cycles=10
This is a statement, and the file format doesn't
know how statements will be used!
submit job 1 on queue one with options 00B_pro;
This is a statement, and the file format doesn't
know how statements will be used!
submit job 3 on queue three with options 00D_pro;
This is a statement, and the file format doesn't
know how statements will be used!
submit job 5 on queue five with options 00F_pro;
This is a statement, and the file format doesn't
know how statements will be used!
submit job 7 on queue seven with options 00H_pro;
This is a statement, and the file format doesn't
know how statements will be used!
submit job 9 on queue nine with options 00J_pro;
```

There are a few things to notice here. First, the statements that are executed
are automatically alternated between. If you had 10 different statements listed,
they would all get their turn with 10 cycles. Since there were two, each was run
5 times.

Also, the statement that had named anchors acted as a template, whereas the
other one was evaluated just as it was. In fact, they were both treated as
templates, but one of the had no anchors.

On more minor but important detail is that the fourth binding *delta* was not
referenced directly in the statements. Since the statements did not pair up an
anchor with this binding name, it was not used. No values were generated for it.
This is how activities are expected to work when they are implemented correctly.
This means that the bindings themselves are templates for data generation, only
to be used when necessary.

## Params

As with the bindings, a params section can be added at the same level, setting
additional parameters to be used with statements. Again, this is an example of
modifying or otherwise creating a specific type of statement, but always in a
way specific to the activity type. Params can be thought of as statement
properties. As such, params don't really do much on their own, although they
have the same basic map syntax as bindings:

```yaml
params:
 ratio: 1
```

As with statements, it is up to each activity type to interpret params in a
useful way.

## Tags

Tags are used to mark and filter groups of statements for controlling which ones
get used in a given scenario:

```yaml
tags:
 name: foxtrot
 unit: bravo
```

### Tag Filtering

The tag filters provide a flexible set of conventions for filtering tagged statements.
Tag filters are usually provided as an activity parameter when an activity is launched.
The rules for tag filtering are:

1. If no tag filter is specified, then the statement matches.
2. A tag name predicate like `tags=name` asserts the presence of a specific
   tag name, regardless of its value.
3. A tag value predicate like `tags=name:foxtrot` asserts the presence of
   a specific tag name and a specific value for it.
4. A tag pattern predicate like `tags=name:'fox.*'` asserts the presence of a
   specific tag name and a value that matches the provided regular expression.
5. Multiple tag predicates may be specified as in `tags=name:'fox.*',unit:bravo`
6. Tag predicates are joined by *and* when more than one is provided -- If any predicate
   fails to match a tagged element, then the whole tag filtering expression fails
   to match.

A demonstration...

```text
[test]$ cat > stdout-test.yaml
tags:
 name: foxtrot
 unit: bravo
statements:
 - "I'm alive!\n"
# EOF (control-D in your terminal)

# no tag filter matches any
[test]$ ./eb run driver=stdout yaml=stdout-test
I'm alive!

# tag name assertion matches
[test]$ ./eb run driver=stdout yaml=stdout-test tags=name
I'm alive!

# tag name assertion does not match
[test]$ ./eb run driver=stdout yaml=stdout-test tags=name2
02:25:28.158 [scenarios:001] ERROR i.e.activities.stdout.StdoutActivity - Unable to create a stdout statement if you have no active statements or bindings configured.

# tag value assertion does not match
[test]$ ./eb run driver=stdout yaml=stdout-test tags=name:bravo
02:25:42.584 [scenarios:001] ERROR i.e.activities.stdout.StdoutActivity - Unable to create a stdout statement if you have no active statements or bindings configured.

# tag value assertion matches
[test]$ ./eb run driver=stdout yaml=stdout-test tags=name:foxtrot
I'm alive!

# tag pattern assertion matches
[test]$ ./eb run driver=stdout yaml=stdout-test tags=name:'fox.*'
I'm alive!

# tag pattern assertion does not match
[test]$ ./eb run driver=stdout yaml=stdout-test tags=name:'tango.*'
02:26:05.149 [scenarios:001] ERROR i.e.activities.stdout.StdoutActivity - Unable to create a stdout statement if you have no active statements or bindings configured.

# compound tag predicate matches every assertion
[test]$ ./eb run driver=stdout yaml=stdout-test tags='name=fox.*',unit=bravo
I'm alive!

# compound tag predicate does not fully match
[test]$ ./eb run driver=stdout yaml=stdout-test tags='name=fox.*',unit=delta
11:02:53.490 [scenarios:001] ERROR i.e.activities.stdout.StdoutActivity - Unable to create a stdout statement if you have no active statements or bindings configured.


```

## Blocks

All the basic primitives described above (names, statements, bindings, params,
tags) can be used to describe and parameterize a set of statements in a yaml
document. In some scenarios, however, you may need to structure your statements
in a more sophisticated way. You might want to do this if you have a set of
common statement forms or parameters that need to apply to many statements, or
perhaps if you have several *different* groups of statements that need to be
configured independently.

This is where blocks become useful:

```text
[test]$ cat > stdout-test.yaml
bindings:
 alpha: Identity()
 beta: Combinations('u;n;u;s;e;d;')
blocks:
 - statements:
   - "{alpha},{beta}\n"
   bindings:
    beta: Combinations('b;l;o;c;k;1;-;COMBINATIONS;')
 - statements:
   - "{alpha},{beta}\n"
   bindings:
    beta: Combinations('b;l;o;c;k;2;-;COMBINATIONS;')
# EOF (control-D in your terminal)

[test]$ ./eb run driver=stdout yaml=stdout-test cycles=10
0,block1-C
1,block2-O
2,block1-M
3,block2-B
4,block1-I
5,block2-N
6,block1-A
7,block2-T
8,block1-I
9,block2-O
```

This shows a couple of important features of blocks. All blocks inherit defaults
for bindings, params, and tags from the root document level. Any of these values
that are defined at the base document level apply to all blocks contained in
that document, unless specifically overridden within a given block.

## More Statements

### Statement Delimiting

Sometimes, you want to specify the text of a statement in different ways. Since
statements are strings, the simplest way for small statements is in double
quotes. If you need to express a much longer statement with special characters
an newlines, then you can use YAML's literal block notation (signaled by the '|'
character) to do so:

```yaml
statements:
 - |
  This is a statement, and the file format doesn't
  know how statements will be used!
 - |
  submit job {alpha} on queue {beta} with options {gamma};
```

Notice that the block starts on the following line after the pipe symbol. This
is a very popular form in practice because it treats the whole block exactly as
it is shown, except for the initial indentations, which are removed.

Statements in this format can be raw statements, statement templates, or
anything that is appropriate for the specific activity type they are being used
with. Generally, the statements should be thought of as a statement form that
you want to use in your activity -- something that has place holders for data
bindings. These place holders are called *named anchors*. The second line above
is an example of a statement template, with anchors that can be replaced by data
for each cycle of an activity.

There is a variety of ways to represent block statements, with folding, without,
with the newline removed, with it retained, with trailing newlines trimmed or
not, and so forth. For a more comprehensive guide on the YAML conventions
regarding multi-line blocks, see [YAML Spec 1.2, Chapter 8, Block
Styles](http://www.yaml.org/spec/1.2/spec.html#Block)

### Statement Sequences

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

In the first form, the names are provided automatically by the YAML loader. In
the second form, they are specified as ordered map keys.

### Statement Properties

You can also configure individual statements with named properties, using the
**statement properties** form:

```yaml
# a list of statements with properties
statements:
 - name: name1
   stmt: statement one
 - name: name2
   stmt: statement two
```

This is the most flexible configuration format at the statement level. It is
also the most verbose. Because this format names each property of the statement,
it allows for other properties to be defined at this level as well. This
includes all of the previously described configuration elements: `name`,
`bindings`, `params`, `tags`, and additionally `stmt`. A detailed example
follows:

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

In this case, the values for `bindings`, `params`, and `tags` take precedence,
overriding those set by the enclosing block or document or activity when the
names match. Parameters called **free parameters** are allowed here, such as
`freeparam3`. These are simply values that get assigned to the params map once
all other processing has completed.

It is possible to mix the **`<name>: <statement>`** form as above in the
example for mapping statement by name, so long as some specific rules are
followed. An example, which is equivalent to the above:

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
   **`stmt: <statement>`** property. It is not possible to detect if this occurs.
   Use caution if you choose to mix these forms.

As explained above, `parm1: pvalue1` is a *free parameter*, and is simply
short-hand for setting values in the params map for the statement.

### Per-Statement Format

It is indeed possible to use any of the three statement formats within
each entry of a statement sequence:

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

Specifically, the first statement is a simple statement body, the second is a
named statement (via free param `<name>: statement` form), the third is a
statement config map, and the fourth is a combination of the previous two.

The above is valid nosqlbench YAML, although a reader would need
to know about the rules explained above in order to really make sense of it. For
most cases, it is best to follow one format convention, but there is flexibility
for overrides and naming when you need it.

## Multi-Docs

The YAML spec allows for multiple yaml documents to be concatenated in the
same file with a separator:

```yaml
---
```

This offers an additional convenience when configuring activities. If you want
to parameterize or tag some a set of statements with their own bindings, params,
or tags, but alongside another set of uniquely configured statements, you need
only put them in separate logical documents, separated by a triple-dash.
For example:

```text
[test]$ cat > stdout-test.yaml
bindings:
 docval: WeightedStrings('doc1.1:1;doc1.2:2;')
statements:
 - "doc1.form1 {docval}\n"
 - "doc1.form2 {docval}\n"
---
bindings:
 numname: NumberNameToString()
statements:
 - "doc2.number {numname}\n"
# EOF (control-D in your terminal)
[test]$ ./eb run driver=stdout yaml=stdout-test cycles=10
doc1.form1 doc1.1
doc1.form2 doc1.2
doc2.number two
doc1.form1 doc1.2
doc1.form2 doc1.1
doc2.number five
doc1.form1 doc1.2
doc1.form2 doc1.2
doc2.number eight
doc1.form1 doc1.1
```

## Template Params

All YAML formats support a parameter macro format that applies before
YAML processing starts. It is a basic macro facility that allows named
anchors to be placed in the document as a whole:

```text
<<varname:defaultval>>
```

In this example, the name of the parameter is <code>varname</code>. It is given a default
value of <code>defaultval</code>. If an activity parameter named *varname* is provided, as
in <code>varname=barbaz</code>, then this whole expression including the double angle
brackets will be replaced with <code>barbaz</code>. If none is provided then the default
value. For example:

```text
[test]$ cat > stdout-test.yaml
statements:
 - "<<linetoprint:MISSING>>\n"
# EOF (control-D in your terminal)

[test]$ ./eb run driver=stdout yaml=stdout-test cycles=1
MISSING

[test]$ ./eb run driver=stdout yaml=stdout-test cycles=1 linetoprint="THIS IS IT"
THIS IS IT
```

If an empty value is desired by default, then simply use an empty string in your template,
like `<<varname:>>`.

## Naming Things

Docs, Blocks, and Statements can all have names:

```yaml
name: doc1
blocks:
 - name: block1
   statements:
   - stmt1: statement1
   - name: st2
     stmt: statement2
---
name: doc2
...
```

This provides a layered naming scheme for the statements themselves. It is not
usually important to name things except for documentation or metric naming
purposes.

### Automatic Naming

If no names are provided, then names are automatically created for blocks and
statements. Statements assigned at the document level are assigned to
"block0". All other statements are named with the format `doc#--block#--stmt#`.
For example, the full name of statement1 above would be `doc1--block1--stmt1`.

## Diagnostics

This section describes errors that you might see if you have a YAML loading issue, and what
you can do to fix them.

### Undefined Name-Statement Tuple

This exception is thrown when the statement body is not found in a statement definition
in any of the supported formats. For example, the following block will cause an error:

    statements:
     - name: statement-foo
       params:
        aparam: avalue

This is because `name` and `params` are reserved property names -- removed from the list of name-value
pairs before free parameters are read. If the statement is not defined before free parameters
are read, then the first free parameter is taken as the name and statement in `name: statement` form.

To correct this error, supply a statement property in the map, or simply replace the `name: statement-foo` entry
with a `statement-foo: statement body` at the top of the map:

Either of these will work:

    statements:
     - name: statement-foo
       stmt: statement body
       params:
        aparam: avalue

    statements:
     - statement-foo: statement body
       params:
        aparam: avalue

In both cases, it is clear to the loader where the statement body should come from, and what (if any) explicit
naming should occur.

### Redefined Name-Statement Tuple

This exception is thrown when the statement name is defined in multiple ways. This is an explicit exception
to avoid possible ambiguity about which value the user intended. For example, the following statements
definition will cause an error:

    statements:
     - name: name1
       name2: statement body

This is an error because the statement is not defined before free parameters are read, and the `name: statement`
form includes a second definition for the statement name. In order to correct this, simply remove the separate
`name` entry, or use the `stmt` property to explicitly set the statement body. Either of these will work:

    statements:
     - name2: statement body

    statements:
     - name: name1
       stmt: statement body

In both cases, there is only one name defined for the statement according to the supported formats.

### YAML Parsing Error

This exception is thrown when the YAML format is not recognizable by the YAML parser. If you are not
working from examples that are known to load cleanly, then please review your document for correctness
according to the [YAML Specification]().

If you are sure that the YAML should load, then please [submit a bug report](https://github.com/nosqlbench/nosqlbench/issues/new?labels=bug)
 with details on the type of YAML file you are trying to load.

### YAML Construction Error

This exception is thrown when the YAML was loaded, but the configuration object was not able to be constructed
from the in-memory YAML document. If this error occurs, it may be a bug in the YAML loader implementation.
Please [submit a bug report](https://github.com/nosqlbench/nosqlbench/issues/new?labels=bug) with details
on the type of YAML file you are trying to load.
