---
title: 06 More on Statements
weight: 06
---

# More on Statements

The template forms available in nosqlbench are very flexible. That means that there are multiple ways
of expressing templates for statements or operations. Thankfully, in most cases, the forms look like
what they do, and most of the ways you can imagine constructing a statement will simply work, as long
as the required details are provided for which driver you are using.

## Statement Delimiting

Sometimes, you want to specify the text of a statement in different ways. Since statements are strings, the simplest way
for small statements is in double quotes. If you need to express a much longer statement with special characters an
newlines, then you can use YAML's literal block notation (signaled by the '|' character) to do so:

```yaml
statements:
 - |
  This is a statement, and the file format doesn't
  know how statements will be used!
 - |
  submit job {alpha} on queue {beta} with options {gamma};
```

Notice that the block starts on the following line after the pipe symbol. This is a very popular form in practice
because it treats the whole block exactly as it is shown, except for the initial indentations, which are removed.

Statements in this format can be raw statements, statement templates, or anything that is appropriate for the specific
activity type they are being used with. Generally, the statements should be thought of as a statement form that you want
to use in your activity -- something that has place holders for data bindings. These place holders are called *named
anchors*. The second line above is an example of a statement template, with anchors that can be replaced by data for
each cycle of an activity.

There is a variety of ways to represent block statements, with folding, without, with the newline removed, with it
retained, with trailing newlines trimmed or not, and so forth. For a more comprehensive guide on the YAML conventions
regarding multi-line blocks, see
[YAML Spec 1.2, Chapter 8, Block Styles](http://www.yaml.org/spec/1.2/spec.html#Block)

## Statement Sequences

To provide a degree of flexibility to the user for statement definitions, multiple statements may be provided together
as a sequence.

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

In the first form, the names are provided automatically by the YAML loader. In the second form, they are specified as
ordered map keys.

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

This is the most flexible configuration format at the statement level. It is also the most verbose. Because this format
names each property of the statement, it allows for other properties to be defined at this level as well. This includes
all of the previously described configuration elements: `name`, `bindings`, `params`, `tags`, and additionally `stmt`. A
detailed example follows:

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

In this case, the values for `bindings`, `params`, and `tags` take precedence, overriding those set by the enclosing
block or document or activity when the names match. Parameters called **free parameters** are allowed here, such as
`freeparam3`. These are simply values that get assigned to the params map once all other processing has completed.

## Named Statement form

It is possible to mix the **`<name>: <statement>`** form as above in the example for mapping statement by name, so long
as some specific rules are followed. An example, which is equivalent to the above:

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

As explained above, `parm1: pvalue1` is a *free parameter*, and is simply short-hand for setting values in the params
map for the statement.


## Named Statement Maps

By combining all the forms together with a map in the middle, we get this form, which allows for the
enumeration of multiple statements, each with an obvious name, and a set of properties:

```
statements:
 - foostmt:
     stmt: "{alpha},{beta}\n"
     parm1: pvalue1
     bindings:
      beta: Combinations('COMBINATIONS;')
     tags:
      tag1: tvalue1
 - barstmt:
     optype: setvar
     parm3: 42
     parm5: true
     userid: 2342
```

This form is arguably the easiest to read, but retains all the expressive power of the other forms too.
The distinction between this form and the named properties form is that the structure underneath the
first value is a map rather than a single value. Particularly, under the 'foostmt' name above, all of
content contained within it is formatted as properties of it -- indented properties.

Here are the basic rules for using this form:

1. Each statement is indicated by a YAML list entry like '-'.
2. Each entry is a map with a single key. This key is taken as the statement name.
3. The properties of this map work exactly the same as for named properties above, but repeating
   the name will throw an error since this is ambiguous.
4. If the template is being used for CQL or another driver type which expects a 'stmt' property,
   it must be provided as an explicitly named 'stmt' property as in the foostmt example above.

Notice in the 'barstmt' example above that there is no "stmt" property. Some drivers
have more flexible op templates may not require this. This is just a property name that was chosen
to represent the "main body" of a statement template in the shorter YAML forms. While the 'stmt'
property is required for drivers like CQL which have a solid concept for "statement body", it isn't
required for all driver types which may build their operations from other properties.

### Per-Statement Format

It is indeed possible to use any of the three statement formats within each entry of a statement sequence:

```yaml
statements:
 - first statement body
 - name: statement3
   stmt: third statement body
 - second: second statement body
 - forth: fourth statement body
   freeparam1: freeparamvalue1
   tags:
    type: preload
 - fifth:
    stmt: fifth statement body
    freeparam2: freeparamvalue2
    tags:
     tag2: tagvalue2
```

The above is valid nosqlbench YAML, although a reader would need to know about the rules explained above in order to
really make sense of it. For most cases, it is best to follow one format convention, but there is flexibility for
overrides and naming when you need it. The main thing to remember is that the statement form is determined on an
element-by-element basis for maximum flexibility.

## Detailed Examples

The above examples are explained in detail below in JSON schematic form, to assist users and developers
understanding of the structural rules:

```yaml
statements:
  
 # ---------------------------------------------------------------------------------------
 
 # string form
 # detected when the element is a single string value

 - first statement body

 # read as:
 # {
 #   name: 'stmt1', // a generated name is also added
 #   stmt: 'first stmt body'
 # }
 
 # ---------------------------------------------------------------------------------------

 # properties form
   
 # detected when the element is a map and the value of the first entry is not a map
   
 - name: statement3
   stmt: third statement body

 # read as:
 # {
 #   name: 'statement3', 
 #   stmt: 'third statement body'
 # }
 
 # ---------------------------------------------------------------------------------------

 # named statement form:
 # detected when reading properties form and the first property name is not a reserved 
 # word, like stmt, name, params, bindings, tags, ...

 - second: second statement body

 # read as:
 # {
 #   name: 'second',
 #   stmt: 'second statement body'
 # }
 
 # ---------------------------------------------------------------------------------------
  
 # properties form with free parameters:
 # detected when properties are used which are not reserved words.
 # Unrecognized words are pushed into the parameters map automatically.

 - forth: fourth statement body
   freeparam1: freeparamvalue1
   tags:
    type: preload

 # read as: 
 # {
 #   name: 'fourth', 
 #   stmt: 'fourth statement body',
 #   params: {
 #     freeparam1: 'freeparamvalue1'
 #   },
 #   tags: {
 #     tag2: 'tagvalue2'
 #   }
 #  }
   
 # ---------------------------------------------------------------------------------------
 
 # named statement maps
 # detected when the element is a map and the only entry is a map.

 - fifth:
    stmt: fifth statement body
    freeparam2: freeparamvalue2
    tags:
     tag2: tagvalue2

 # read as:
 # {
 #    name: 'fifth',
 #    stmt: 'fifth statement body'
 #    params: {
 #      freeparam2: 'freeparamvalue2'
 #    },
 #    tags: {
 #      tag2: 'tagvalue2'
 #    }
 # }

 # ---------------------------------------------------------------------------------------
    
```

