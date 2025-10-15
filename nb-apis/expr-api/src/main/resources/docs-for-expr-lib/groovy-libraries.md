# Groovy Expression Libraries

NoSQLBench automatically loads Groovy libraries from the `lib/groovy/` directory to extend expression functionality. Libraries are marked with the `@Library` annotation and provide themed collections of utility functions.

## Available Libraries

### strings.groovy - String Manipulation
Functions for common string operations including padding, truncation, case conversion, and formatting.

#### String Formatting

**padLeft(str, length, padChar=' ')**
```groovy
{{= padLeft('42', 5, '0') }}          // "00042"
{{= padLeft('test', 10) }}            // "      test"
```

**padRight(str, length, padChar=' ')**
```groovy
{{= padRight('test', 10, '.') }}      // "test......"
{{= padRight('ID', 6) }}              // "ID    "
```

**truncate(str, maxLength, addEllipsis=false)**
```groovy
{{= truncate('very long string', 10, true) }}   // "very lo..."
{{= truncate('short', 10, false) }}             // "short"
```

#### Case Conversion

**toCamelCase(str)**
```groovy
{{= toCamelCase('hello world') }}     // "helloWorld"
{{= toCamelCase('user_id') }}         // "userId"
```

**toSnakeCase(str)**
```groovy
{{= toSnakeCase('helloWorld') }}      // "hello_world"
{{= toSnakeCase('UserID') }}          // "user_id"
```

**toKebabCase(str)**
```groovy
{{= toKebabCase('helloWorld') }}      // "hello-world"
{{= toKebabCase('UserID') }}          // "user-id"
```

**titleCase(str)**
```groovy
{{= titleCase('hello world') }}       // "Hello World"
```

#### String Operations

**reverseString(str)**
```groovy
{{= reverseString('hello') }}         // "olleh"
```

**countOccurrences(str, substring)**
```groovy
{{= countOccurrences('hello world', 'l') }}    // 3
```

**hashString(str)**
```groovy
{{= hashString('hello') }}            // "5d41402abc4b2a76b9719d911017c592"
```

**repeatString(str, times)**
```groovy
{{= repeatString('ab', 3) }}          // "ababab"
```

**removeWhitespace(str)**
```groovy
{{= removeWhitespace('hello world  test') }}   // "helloworldtest"
```

---

### datagen.groovy - Data Generation
Functions for generating test data including random values, UUIDs, and formatted patterns.

#### Random Values

**randomUUID()**
```groovy
{{= randomUUID() }}                   // "550e8400-e29b-41d4-a716-446655440000"
```

**randomInt(min, max)**
```groovy
{{= randomInt(1, 100) }}              // Random int between 1-99
{{= randomInt(1000, 9999) }}          // Random 4-digit number
```

**randomLong(min, max)**
```groovy
{{= randomLong(1000000, 9999999) }}   // Random 7-digit long
```

**randomDouble(min, max)**
```groovy
{{= randomDouble(0.0, 1.0) }}         // Random double 0.0-1.0
{{= randomDouble(10.5, 99.9) }}       // Random double in range
```

#### Random Strings

**randomAlphanumeric(length)**
```groovy
{{= randomAlphanumeric(10) }}         // "aB3xR9mK2p"
{{= randomAlphanumeric(32) }}         // 32-char alphanumeric string
```

**randomAlpha(length)**
```groovy
{{= randomAlpha(8) }}                 // "xKmRpQzB"
```

**randomNumeric(length)**
```groovy
{{= randomNumeric(6) }}               // "483927"
```

**randomHex(length)**
```groovy
{{= randomHex(8) }}                   // "a3f5d2c8"
```

#### Collection Sampling

**randomElement(list)**
```groovy
{{= randomElement(['red', 'green', 'blue']) }}        // "green"
{{= randomElement(['usa', 'uk', 'de', 'fr']) }}       // Random country
```

**randomElements(list, count)**
```groovy
{{= randomElements(['a', 'b', 'c', 'd'], 2) }}        // ['a', 'c']
```

**shuffleList(list)**
```groovy
{{= shuffleList([1, 2, 3, 4]) }}      // [3, 1, 4, 2]
```

**weightedChoice(weightsMap)**
```groovy
{{= weightedChoice([low: 70, medium: 20, high: 10]) }}    // "low" 70% of time
{{= weightedChoice([bronze: 60, silver: 30, gold: 10]) }} // Weighted tier
```

#### Boolean Generation

**randomBoolean()**
```groovy
{{= randomBoolean() }}                // true or false
```

**randomBooleanWeighted(probability)**
```groovy
{{= randomBooleanWeighted(0.7) }}     // true 70% of time
{{= randomBooleanWeighted(0.1) }}     // true 10% of time
```

#### Formatted Data

**randomEmail()**
```groovy
{{= randomEmail() }}                  // "user_a3f5d2@example.com"
```

**randomPhone()**
```groovy
{{= randomPhone() }}                  // "555-382-9471"
```

**randomIPv4()**
```groovy
{{= randomIPv4() }}                   // "192.168.1.42"
```

**randomDateBetween(startDate, endDate)**
```groovy
{{= randomDateBetween('2024-01-01', '2024-12-31') }}  // "2024-06-15"
```

---

### datetime.groovy - Date and Time Utilities
Functions for date/time formatting, parsing, manipulation, and calculations.

#### Current Time

**nowMillis()**
```groovy
{{= nowMillis() }}                    // 1609459200000
```

**nowSeconds()**
```groovy
{{= nowSeconds() }}                   // 1609459200
```

**nowNanos()**
```groovy
{{= nowNanos() }}                     // 1609459200000000000
```

**currentDate()**
```groovy
{{= currentDate() }}                  // "2024-01-01"
```

**currentTime()**
```groovy
{{= currentTime() }}                  // "14:30:45"
```

**currentDateTime()**
```groovy
{{= currentDateTime() }}              // "2024-01-01T14:30:45"
```

#### Date Formatting

**formatNow(pattern)**
```groovy
{{= formatNow('yyyy-MM-dd HH:mm:ss') }}        // "2024-01-01 12:30:45"
{{= formatNow('MM/dd/yyyy') }}                 // "01/01/2024"
{{= formatNow('EEE, MMM d, yyyy') }}           // "Mon, Jan 1, 2024"
```

**reformatDate(dateStr, fromPattern, toPattern)**
```groovy
{{= reformatDate('2024-01-01', 'yyyy-MM-dd', 'MM/dd/yyyy') }}  // "01/01/2024"
```

#### Date Arithmetic

**addDays(dateStr, days)**
```groovy
{{= addDays('2024-01-01', 7) }}       // "2024-01-08"
{{= addDays('2024-01-01', -5) }}      // "2023-12-27"
```

**addWeeks(dateStr, weeks)**
```groovy
{{= addWeeks('2024-01-01', 2) }}      // "2024-01-15"
```

**addMonths(dateStr, months)**
```groovy
{{= addMonths('2024-01-01', 3) }}     // "2024-04-01"
```

**addYears(dateStr, years)**
```groovy
{{= addYears('2024-01-01', 1) }}      // "2025-01-01"
```

#### Date Calculations

**daysBetween(startDate, endDate)**
```groovy
{{= daysBetween('2024-01-01', '2024-01-15') }}     // 14
```

**dayOfWeek(dateStr)**
```groovy
{{= dayOfWeek('2024-01-01') }}        // 1 (Monday)
```

**dayOfMonth(dateStr)**
```groovy
{{= dayOfMonth('2024-01-15') }}       // 15
```

**monthOfYear(dateStr)**
```groovy
{{= monthOfYear('2024-06-15') }}      // 6
```

**yearOf(dateStr)**
```groovy
{{= yearOf('2024-01-01') }}           // 2024
```

#### Date Predicates

**isPast(dateStr)**
```groovy
{{= isPast('2020-01-01') }}           // true
```

**isFuture(dateStr)**
```groovy
{{= isFuture('2025-01-01') }}         // true
```

**isLeapYear(year)**
```groovy
{{= isLeapYear(2024) }}               // true
```

#### Date Ranges

**startOfWeek(dateStr)**
```groovy
{{= startOfWeek('2024-01-03') }}      // "2024-01-01" (Monday)
```

**endOfWeek(dateStr)**
```groovy
{{= endOfWeek('2024-01-01') }}        // "2024-01-07" (Sunday)
```

**startOfMonth(dateStr)**
```groovy
{{= startOfMonth('2024-01-15') }}     // "2024-01-01"
```

**endOfMonth(dateStr)**
```groovy
{{= endOfMonth('2024-01-15') }}       // "2024-01-31"
```

#### Timestamp Conversion

**millisToDate(millis)**
```groovy
{{= millisToDate(1609459200000) }}    // "2021-01-01"
```

**millisToDateTime(millis)**
```groovy
{{= millisToDateTime(1609459200000) }}  // "2021-01-01T00:00:00"
```

**dateToMillis(dateStr)**
```groovy
{{= dateToMillis('2021-01-01') }}     // 1609459200000
```

#### Timezone Support

**currentDateInZone(timezone)**
```groovy
{{= currentDateInZone('America/New_York') }}   // "2024-01-01"
{{= currentDateInZone('UTC') }}                // "2024-01-01"
```

**currentTimeInZone(timezone)**
```groovy
{{= currentTimeInZone('UTC') }}       // "12:30:45"
```

---

### collections.groovy - Collection Utilities
Functions for working with lists, maps, and sets including filtering, transformation, and aggregation.

#### List Slicing

**take(list, n)**
```groovy
{{= take([1, 2, 3, 4, 5], 3) }}       // [1, 2, 3]
```

**takeLast(list, n)**
```groovy
{{= takeLast([1, 2, 3, 4, 5], 2) }}   // [4, 5]
```

**skip(list, n)**
```groovy
{{= skip([1, 2, 3, 4, 5], 2) }}       // [3, 4, 5]
```

**chunk(list, size)**
```groovy
{{= chunk([1, 2, 3, 4, 5], 2) }}      // [[1, 2], [3, 4], [5]]
```

#### List Transformation

**flattenList(list)**
```groovy
{{= flattenList([[1, 2], [3, [4, 5]]]) }}      // [1, 2, 3, 4, 5]
```

**unique(list)**
```groovy
{{= unique([1, 2, 2, 3, 3, 3]) }}     // [1, 2, 3]
```

**reverseList(list)**
```groovy
{{= reverseList([1, 2, 3]) }}         // [3, 2, 1]
```

**sortList(list)**
```groovy
{{= sortList([3, 1, 4, 1, 5]) }}      // [1, 1, 3, 4, 5]
```

**sortDescending(list)**
```groovy
{{= sortDescending([3, 1, 4, 1, 5]) }}  // [5, 4, 3, 1, 1]
```

#### Set Operations

**intersect(list1, list2)**
```groovy
{{= intersect([1, 2, 3], [2, 3, 4]) }}         // [2, 3]
```

**union(list1, list2)**
```groovy
{{= union([1, 2], [2, 3]) }}          // [1, 2, 3]
```

**difference(list1, list2)**
```groovy
{{= difference([1, 2, 3], [2, 3, 4]) }}        // [1]
```

#### List Predicates

**contains(list, element)**
```groovy
{{= contains([1, 2, 3], 2) }}         // true
```

**allMatch(list, condition)**
```groovy
{{= allMatch([2, 4, 6], { it % 2 == 0 }) }}    // true
```

**anyMatch(list, condition)**
```groovy
{{= anyMatch([1, 2, 3], { it > 2 }) }}         // true
```

**countMatching(list, condition)**
```groovy
{{= countMatching([1, 2, 3, 4, 5], { it > 2 }) }}  // 3
```

#### List Grouping

**groupBy(list, keyFunc)**
```groovy
{{= groupBy([1, 2, 3, 4], { it % 2 }) }}       // [0: [2, 4], 1: [1, 3]]
```

**partition(list, condition)**
```groovy
{{= partition([1, 2, 3, 4], { it % 2 == 0 }) }}  // [[2, 4], [1, 3]]
```

**frequency(list)**
```groovy
{{= frequency(['a', 'b', 'a', 'c', 'a']) }}    // [a: 3, b: 1, c: 1]
```

#### List Combining

**zipLists(list1, list2)**
```groovy
{{= zipLists([1, 2, 3], ['a', 'b', 'c']) }}    // [[1, 'a'], [2, 'b'], [3, 'c']]
```

**joinList(list, delimiter)**
```groovy
{{= joinList([1, 2, 3], ', ') }}      // "1, 2, 3"
```

#### Map Operations

**mapKeys(map)**
```groovy
{{= mapKeys([a: 1, b: 2]) }}          // ['a', 'b']
```

**mapValues(map)**
```groovy
{{= mapValues([a: 1, b: 2]) }}        // [1, 2]
```

**mergeMaps(map1, map2)**
```groovy
{{= mergeMaps([a: 1, b: 2], [b: 3, c: 4]) }}   // [a: 1, b: 3, c: 4]
```

**filterMapKeys(map, keys)**
```groovy
{{= filterMapKeys([a: 1, b: 2, c: 3], ['a', 'c']) }}  // [a: 1, c: 3]
```

**invertMap(map)**
```groovy
{{= invertMap([a: 1, b: 2]) }}        // [1: 'a', 2: 'b']
```

#### List Generation

**rangeInts(start, end)**
```groovy
{{= rangeInts(1, 5) }}                // [1, 2, 3, 4, 5]
```

**repeatElement(element, times)**
```groovy
{{= repeatElement('x', 3) }}          // ['x', 'x', 'x']
```

#### List Rotation

**rotateLeft(list, n)**
```groovy
{{= rotateLeft([1, 2, 3, 4, 5], 2) }}          // [3, 4, 5, 1, 2]
```

**rotateRight(list, n)**
```groovy
{{= rotateRight([1, 2, 3, 4, 5], 2) }}         // [4, 5, 1, 2, 3]
```

**sample(list, n)**
```groovy
{{= sample([1, 2, 3, 4, 5], 3) }}     // [2, 4, 1] (random)
```

---

### math.groovy - Math and Statistics
Functions for mathematical operations, statistical calculations, and numeric utilities.

#### Basic Statistics

**sum(numbers)**
```groovy
{{= sum([1, 2, 3, 4, 5]) }}           // 15
```

**average(numbers)**
```groovy
{{= average([1, 2, 3, 4, 5]) }}       // 3.0
```

**median(numbers)**
```groovy
{{= median([1, 2, 3, 4, 5]) }}        // 3
```

**minValue(numbers)**
```groovy
{{= minValue([3, 1, 4, 1, 5]) }}      // 1
```

**maxValue(numbers)**
```groovy
{{= maxValue([3, 1, 4, 1, 5]) }}      // 5
```

**range(numbers)**
```groovy
{{= range([1, 5, 3, 9, 2]) }}         // 8
```

**product(numbers)**
```groovy
{{= product([2, 3, 4]) }}             // 24
```

#### Rounding and Precision

**abs(number)**
```groovy
{{= abs(-5) }}                        // 5
```

**roundTo(number, decimals)**
```groovy
{{= roundTo(3.14159, 2) }}            // 3.14
```

**ceiling(number)**
```groovy
{{= ceiling(3.14) }}                  // 4
```

**floor(number)**
```groovy
{{= floor(3.14) }}                    // 3
```

#### Powers and Roots

**power(base, exponent)**
```groovy
{{= power(2, 10) }}                   // 1024
```

**sqrt(number)**
```groovy
{{= sqrt(16) }}                       // 4.0
```

**ln(number)**
```groovy
{{= ln(Math.E) }}                     // 1.0
```

**log10(number)**
```groovy
{{= log10(100) }}                     // 2.0
```

#### Number Theory

**factorial(n)**
```groovy
{{= factorial(5) }}                   // 120
```

**gcd(a, b)**
```groovy
{{= gcd(48, 18) }}                    // 6
```

**lcm(a, b)**
```groovy
{{= lcm(12, 18) }}                    // 36
```

**isPrime(n)**
```groovy
{{= isPrime(17) }}                    // true
```

**isEven(n)**
```groovy
{{= isEven(4) }}                      // true
```

**isOdd(n)**
```groovy
{{= isOdd(3) }}                       // true
```

#### Value Utilities

**clamp(value, min, max)**
```groovy
{{= clamp(15, 1, 10) }}               // 10
{{= clamp(-5, 0, 100) }}              // 0
```

**percentage(value, total)**
```groovy
{{= percentage(25, 200) }}            // 12.5
```

#### Advanced Statistics

**stdDev(numbers)**
```groovy
{{= stdDev([2, 4, 4, 4, 5, 5, 7, 9]) }}        // 2.0
```

**variance(numbers)**
```groovy
{{= variance([2, 4, 4, 4, 5, 5, 7, 9]) }}      // 4.0
```

**percentile(numbers, p)**
```groovy
{{= percentile([1, 2, 3, 4, 5, 6, 7, 8, 9, 10], 90) }}  // 9
```

**mode(numbers)**
```groovy
{{= mode([1, 2, 2, 3, 3, 3, 4]) }}    // 3
```

#### List Transformations

**normalize(numbers)**
```groovy
{{= normalize([1, 2, 3, 4, 5]) }}     // [0.0, 0.25, 0.5, 0.75, 1.0]
```

**cumulativeSum(numbers)**
```groovy
{{= cumulativeSum([1, 2, 3, 4]) }}    // [1, 3, 6, 10]
```

#### Interpolation

**lerp(start, end, t)**
```groovy
{{= lerp(0, 100, 0.5) }}              // 50.0
{{= lerp(10, 20, 0.25) }}             // 12.5
```

#### Angle Conversion

**toRadians(degrees)**
```groovy
{{= toRadians(180) }}                 // 3.141592653589793
```

**toDegrees(radians)**
```groovy
{{= toDegrees(Math.PI) }}             // 180.0
```

---

## Using Libraries in Workloads

Libraries are automatically loaded from `lib/groovy/` when the expression processor initializes. You can use any function in your workload expressions:

```yaml
scenarios:
  user_data:
    prepared:
      insert: |
        INSERT INTO users (id, name, email, created_at, status)
        VALUES (
          {{= randomUUID() }},
          '{{= randomAlpha(10) }}',
          '{{= randomEmail() }}',
          '{{= currentDateTime() }}',
          '{{= randomElement(['active', 'pending', 'inactive']) }}'
        )
```

## Creating Custom Libraries

To create your own library:

1. Create a `.groovy` file in `lib/groovy/`
2. Add the `@Library` comment marker
3. Define functions as closures assigned to variables:

```groovy
/*
 * @Library
 * My custom library
 */

myFunction = { param1, param2 ->
    // Your logic here
    return result
}
```

Functions are then available in expressions: `{{= myFunction('a', 'b') }}`
