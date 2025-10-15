/*
 * @Library
 * Test Groovy library for expression auto-loading.
 * This library provides helper functions for testing.
 */

// Define functions as closures so they're available in the binding
greet = { name -> "Hello, ${name}!" }
multiply = { a, b -> a * b }
sum = { numbers -> numbers.sum() }

// Classes are also supported
class Calculator {
    static int add(int a, int b) {
        return a + b
    }

    static int subtract(int a, int b) {
        return a - b
    }
}
