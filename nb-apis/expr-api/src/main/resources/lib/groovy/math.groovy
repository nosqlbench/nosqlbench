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
 * Math and statistics utilities for NoSQLBench expressions.
 * Provides functions for mathematical operations, statistical calculations,
 * and numeric utilities beyond basic arithmetic.
 */

import io.nosqlbench.nb.api.expr.annotations.ExprFunctionSpec
import io.nosqlbench.nb.api.expr.annotations.ExprExample

@ExprFunctionSpec(
    synopsis = "sum(numbers)",
    description = "Calculate the sum of a list of numbers"
)
@ExprExample(args = ["[1, 2, 3, 4, 5]"], expect = "15")
def sum(numbers) {
    numbers.sum() ?: 0
}

@ExprFunctionSpec(
    synopsis = "average(numbers)",
    description = "Calculate the average (mean) of a list of numbers"
)
@ExprExample(args = ["[1, 2, 3, 4, 5]"], expect = "3.0")
def average(numbers) {
    if (numbers.isEmpty()) return 0
    numbers.sum() / numbers.size()
}

@ExprFunctionSpec(
    synopsis = "median(numbers)",
    description = "Calculate the median of a list of numbers"
)
@ExprExample(args = ["[1, 2, 3, 4, 5]"], expect = "3")
def median(numbers) {
    if (numbers.isEmpty()) return 0
    def sorted = numbers.sort()
    def middle = sorted.size() / 2
    if (sorted.size() % 2 == 0) {
        (sorted[middle - 1] + sorted[middle]) / 2
    } else {
        sorted[middle]
    }
}

@ExprFunctionSpec(
    synopsis = "minValue(numbers)",
    description = "Find the minimum value in a list"
)
@ExprExample(args = ["[3, 1, 4, 1, 5]"], expect = "1")
def minValue(numbers) {
    numbers.min()
}

@ExprFunctionSpec(
    synopsis = "maxValue(numbers)",
    description = "Find the maximum value in a list"
)
@ExprExample(args = ["[3, 1, 4, 1, 5]"], expect = "5")
def maxValue(numbers) {
    numbers.max()
}

@ExprFunctionSpec(
    synopsis = "range(numbers)",
    description = "Calculate the range (max - min) of a list"
)
@ExprExample(args = ["[1, 5, 3, 9, 2]"], expect = "8")
def range(numbers) {
    if (numbers.isEmpty()) return 0
    numbers.max() - numbers.min()
}

@ExprFunctionSpec(
    synopsis = "product(numbers)",
    description = "Calculate the product of all numbers in a list"
)
@ExprExample(args = ["[2, 3, 4]"], expect = "24")
def product(numbers) {
    numbers.inject(1) { acc, val -> acc * val }
}

@ExprFunctionSpec(
    synopsis = "abs(number)",
    description = "Calculate the absolute value"
)
@ExprExample(args = ["-5"], expect = "5")
def abs(number) {
    Math.abs(number)
}

@ExprFunctionSpec(
    synopsis = "roundTo(number, decimals)",
    description = "Round a number to specified decimal places"
)
@ExprExample(args = ["3.14159", "2"], expect = "3.14")
def roundTo(number, decimals) {
    def factor = Math.pow(10, decimals)
    Math.round(number * factor) / factor
}

@ExprFunctionSpec(
    synopsis = "ceiling(number)",
    description = "Round up to the nearest integer"
)
@ExprExample(args = ["3.14"], expect = "4")
def ceiling(number) {
    Math.ceil(number) as int
}

@ExprFunctionSpec(
    synopsis = "floor(number)",
    description = "Round down to the nearest integer"
)
@ExprExample(args = ["3.14"], expect = "3")
def floor(number) {
    Math.floor(number) as int
}

@ExprFunctionSpec(
    synopsis = "power(base, exponent)",
    description = "Calculate power (base^exponent)"
)
@ExprExample(args = ["2", "10"], expect = "1024")
def power(base, exponent) {
    Math.pow(base, exponent)
}

@ExprFunctionSpec(
    synopsis = "sqrt(number)",
    description = "Calculate square root"
)
@ExprExample(args = ["16"], expect = "4.0")
def sqrt(number) {
    Math.sqrt(number)
}

@ExprFunctionSpec(
    synopsis = "ln(number)",
    description = "Calculate natural logarithm"
)
@ExprExample(args = ["Math.E"], expect = "1.0")
def ln(number) {
    Math.log(number)
}

@ExprFunctionSpec(
    synopsis = "log10(number)",
    description = "Calculate base-10 logarithm"
)
@ExprExample(args = ["100"], expect = "2.0")
def log10(number) {
    Math.log10(number)
}

@ExprFunctionSpec(
    synopsis = "factorial(n)",
    description = "Calculate factorial of a number"
)
@ExprExample(args = ["5"], expect = "120")
def factorial(n) {
    if (n <= 1) return 1
    (2..n).inject(1) { acc, val -> acc * val }
}

@ExprFunctionSpec(
    synopsis = "gcd(a, b)",
    description = "Calculate greatest common divisor of two numbers"
)
@ExprExample(args = ["48", "18"], expect = "6")
def gcd(a, b) {
    while (b != 0) {
        def temp = b
        b = a % b
        a = temp
    }
    Math.abs(a as int)
}

@ExprFunctionSpec(
    synopsis = "lcm(a, b)",
    description = "Calculate least common multiple of two numbers"
)
@ExprExample(args = ["12", "18"], expect = "36")
def lcm(a, b) {
    Math.abs((a * b) / gcd(a, b)) as int
}

@ExprFunctionSpec(
    synopsis = "isPrime(n)",
    description = "Check if a number is prime"
)
@ExprExample(args = ["17"], expect = "true")
def isPrime(n) {
    if (n <= 1) return false
    if (n <= 3) return true
    if (n % 2 == 0 || n % 3 == 0) return false
    def i = 5
    while (i * i <= n) {
        if (n % i == 0 || n % (i + 2) == 0) return false
        i += 6
    }
    return true
}

@ExprFunctionSpec(
    synopsis = "isEven(n)",
    description = "Check if a number is even"
)
@ExprExample(args = ["4"], expect = "true")
def isEven(n) {
    n % 2 == 0
}

@ExprFunctionSpec(
    synopsis = "isOdd(n)",
    description = "Check if a number is odd"
)
@ExprExample(args = ["3"], expect = "true")
def isOdd(n) {
    n % 2 != 0
}

@ExprFunctionSpec(
    synopsis = "clamp(value, min, max)",
    description = "Clamp a number between min and max values"
)
@ExprExample(args = ["15", "1", "10"], expect = "10")
def clamp(value, min, max) {
    Math.max(min, Math.min(max, value))
}

@ExprFunctionSpec(
    synopsis = "percentage(value, total)",
    description = "Calculate percentage of a value relative to total"
)
@ExprExample(args = ["25", "200"], expect = "12.5")
def percentage(value, total) {
    if (total == 0) return 0
    (value / total) * 100
}

@ExprFunctionSpec(
    synopsis = "stdDev(numbers)",
    description = "Calculate standard deviation of a list of numbers"
)
@ExprExample(args = ["[2, 4, 4, 4, 5, 5, 7, 9]"], expect = "2.0")
def stdDev(numbers) {
    if (numbers.isEmpty()) return 0
    def mean = average(numbers)
    def variance = numbers.collect { Math.pow(it - mean, 2) }.sum() / numbers.size()
    Math.sqrt(variance)
}

@ExprFunctionSpec(
    synopsis = "variance(numbers)",
    description = "Calculate variance of a list of numbers"
)
@ExprExample(args = ["[2, 4, 4, 4, 5, 5, 7, 9]"], expect = "4.0")
def variance(numbers) {
    if (numbers.isEmpty()) return 0
    def mean = average(numbers)
    numbers.collect { Math.pow(it - mean, 2) }.sum() / numbers.size()
}

@ExprFunctionSpec(
    synopsis = "percentile(numbers, p)",
    description = "Calculate nth percentile of a list of numbers"
)
@ExprExample(args = ["[1, 2, 3, 4, 5, 6, 7, 8, 9, 10]", "90"], expect = "9")
def percentile(numbers, p) {
    if (numbers.isEmpty()) return 0
    def sorted = numbers.sort()
    def index = (p / 100.0) * (sorted.size() - 1)
    def lower = sorted[Math.floor(index) as int]
    def upper = sorted[Math.ceil(index) as int]
    lower + (upper - lower) * (index - Math.floor(index))
}

@ExprFunctionSpec(
    synopsis = "mode(numbers)",
    description = "Calculate the mode (most frequent value) of a list"
)
@ExprExample(args = ["[1, 2, 2, 3, 3, 3, 4]"], expect = "3")
def mode(numbers) {
    if (numbers.isEmpty()) return null
    def frequency = numbers.countBy { it }
    def maxFreq = frequency.values().max()
    frequency.find { it.value == maxFreq }.key
}

@ExprFunctionSpec(
    synopsis = "normalize(numbers)",
    description = "Normalize a list of numbers to 0-1 range"
)
@ExprExample(args = ["[1, 2, 3, 4, 5]"], expect = "[0.0, 0.25, 0.5, 0.75, 1.0]")
def normalize(numbers) {
    if (numbers.isEmpty()) return []
    def min = numbers.min()
    def max = numbers.max()
    def range = max - min
    if (range == 0) return numbers.collect { 0.5 }
    numbers.collect { (it - min) / range }
}

@ExprFunctionSpec(
    synopsis = "cumulativeSum(numbers)",
    description = "Calculate cumulative sum of a list"
)
@ExprExample(args = ["[1, 2, 3, 4]"], expect = "[1, 3, 6, 10]")
def cumulativeSum(numbers) {
    def cumSum = 0
    numbers.collect { cumSum += it; cumSum }
}

@ExprFunctionSpec(
    synopsis = "lerp(start, end, t)",
    description = "Linear interpolation between two values"
)
@ExprExample(args = ["0", "100", "0.5"], expect = "50.0")
def lerp(start, end, t) {
    start + (end - start) * t
}

@ExprFunctionSpec(
    synopsis = "toRadians(degrees)",
    description = "Convert degrees to radians"
)
@ExprExample(args = ["180"], expect = "3.141592653589793")
def toRadians(degrees) {
    Math.toRadians(degrees)
}

@ExprFunctionSpec(
    synopsis = "toDegrees(radians)",
    description = "Convert radians to degrees"
)
@ExprExample(args = ["Math.PI"], expect = "180.0")
def toDegrees(radians) {
    Math.toDegrees(radians)
}
