/*
 * Copyright (c) nosqlbench
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

/*
 * @Library
 * String manipulation utilities for NoSQLBench expressions.
 * Provides functions for common string operations like padding, truncation,
 * case conversion, and formatting.
 */

import io.nosqlbench.nb.api.expr.annotations.ExprFunctionSpec
import io.nosqlbench.nb.api.expr.annotations.ExprExample
import java.security.MessageDigest

@ExprFunctionSpec(
    synopsis = "padLeft(str, length, padChar = ' ')",
    description = "Pad a string to the left with a specified character to reach the target length"
)
@ExprExample(args = ["'42'", "5", "'0'"], expect = "'00042'")
def padLeft(str, length, padChar = ' ') {
    str = str.toString()
    if (str.length() >= length) return str
    return padChar.toString() * (length - str.length()) + str
}

@ExprFunctionSpec(
    synopsis = "padRight(str, length, padChar = ' ')",
    description = "Pad a string to the right with a specified character to reach the target length"
)
@ExprExample(args = ["'test'", "10", "'.'"], expect = "'test......'")
def padRight(str, length, padChar = ' ') {
    str = str.toString()
    if (str.length() >= length) return str
    return str + padChar.toString() * (length - str.length())
}

@ExprFunctionSpec(
    synopsis = "truncate(str, maxLength, addEllipsis = false)",
    description = "Truncate a string to a maximum length, optionally adding an ellipsis"
)
@ExprExample(args = ["'very long string'", "10", "true"], expect = "'very lo...'")
def truncate(str, maxLength, addEllipsis = false) {
    str = str.toString()
    if (str.length() <= maxLength) return str
    def truncated = str.substring(0, maxLength)
    return addEllipsis && maxLength > 3 ? truncated.substring(0, maxLength - 3) + '...' : truncated
}

@ExprFunctionSpec(
    synopsis = "toCamelCase(str)",
    description = "Convert a string to camelCase"
)
@ExprExample(args = ["'hello world'"], expect = "'helloWorld'")
def toCamelCase(str) {
    str = str.toString()
    def words = str.split(/[\s_-]+/)
    if (words.length == 0) return ''
    return words[0].toLowerCase() + words[1..-1].collect { it.capitalize() }.join('')
}

@ExprFunctionSpec(
    synopsis = "toSnakeCase(str)",
    description = "Convert a string to snake_case"
)
@ExprExample(args = ["'helloWorld'"], expect = "'hello_world'")
def toSnakeCase(str) {
    str = str.toString()
    return str.replaceAll(/([A-Z])/, '_$1')
              .replaceAll(/[\s-]+/, '_')
              .toLowerCase()
              .replaceAll(/^_/, '')
}

@ExprFunctionSpec(
    synopsis = "toKebabCase(str)",
    description = "Convert a string to kebab-case"
)
@ExprExample(args = ["'helloWorld'"], expect = "'hello-world'")
def toKebabCase(str) {
    str = str.toString()
    return str.replaceAll(/([A-Z])/, '-$1')
              .replaceAll(/[\s_]+/, '-')
              .toLowerCase()
              .replaceAll(/^-/, '')
}

@ExprFunctionSpec(
    synopsis = "reverseString(str)",
    description = "Reverse a string"
)
@ExprExample(args = ["'hello'"], expect = "'olleh'")
def reverseString(str) {
    str.toString().reverse()
}

@ExprFunctionSpec(
    synopsis = "countOccurrences(str, substring)",
    description = "Count occurrences of a substring in a string"
)
@ExprExample(args = ["'hello world'", "'l'"], expect = "3")
def countOccurrences(str, substring) {
    str = str.toString()
    substring = substring.toString()
    return (str.length() - str.replace(substring, '').length()) / substring.length() as int
}

@ExprFunctionSpec(
    synopsis = "hashString(str)",
    description = "Generate a simple hash (MD5) of a string"
)
@ExprExample(args = ["'hello'"], expect = "'5d41402abc4b2a76b9719d911017c592'")
def hashString(str) {
    def md = MessageDigest.getInstance('MD5')
    def digest = md.digest(str.toString().bytes)
    return digest.collect { String.format('%02x', it) }.join('')
}

@ExprFunctionSpec(
    synopsis = "repeatString(str, times)",
    description = "Repeat a string n times"
)
@ExprExample(args = ["'ab'", "3"], expect = "'ababab'")
def repeatString(str, times) {
    str.toString() * times
}

@ExprFunctionSpec(
    synopsis = "removeWhitespace(str)",
    description = "Remove all whitespace from a string"
)
@ExprExample(args = ["'hello world  test'"], expect = "'helloworldtest'")
def removeWhitespace(str) {
    str.toString().replaceAll(/\s+/, '')
}

@ExprFunctionSpec(
    synopsis = "titleCase(str)",
    description = "Capitalize the first letter of each word"
)
@ExprExample(args = ["'hello world'"], expect = "'Hello World'")
def titleCase(str) {
    str.toString().split(/\s+/).collect { it.capitalize() }.join(' ')
}
