/*
 * @Library
 * Mathematical helper functions for expressions.
 */

// Define functions as closures
square = { n -> n * n }
cube = { n -> n * n * n }
factorial = { n ->
    if (n <= 1) return 1
    return n * factorial(n - 1)
}
