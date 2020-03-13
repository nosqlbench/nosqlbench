---
title: distribution functions
weight: 30
---

## Beta

@see [Wikipedia: Beta distribution](https://en.wikipedia.org/wiki/Beta_distribution) @see [Commons JavaDoc: BetaDistribution](https://commons.apache.org/proper/commons-statistics/commons-statistics-distribution/apidocs/org/apache/commons/statistics/distribution/BetaDistribution.html)

- int -> Beta(double: alpha, double: beta, java.lang.String[]...: mods) -> double
- long -> Beta(double: alpha, double: beta, java.lang.String[]...: mods) -> double


## Binomial

@see [Wikipedia: Binomial distribution](http://en.wikipedia.org/wiki/Binomial_distribution) @see [Commons JavaDoc: BinomialDistribution](https://commons.apache.org/proper/commons-statistics/commons-statistics-distribution/apidocs/org/apache/commons/statistics/distribution/BinomialDistribution.html)

- int -> Binomial(int: trials, double: p, java.lang.String[]...: modslist) -> int
- int -> Binomial(int: trials, double: p, java.lang.String[]...: modslist) -> long
- long -> Binomial(int: trials, double: p, java.lang.String[]...: modslist) -> int
- long -> Binomial(int: trials, double: p, java.lang.String[]...: modslist) -> long


## Cauchy

@see [Wikipedia: Cauchy_distribution](http://en.wikipedia.org/wiki/Cauchy_distribution) @see [Commons Javadoc: CauchyDistribution](https://commons.apache.org/proper/commons-statistics/commons-statistics-distribution/apidocs/org/apache/commons/statistics/distribution/CauchyDistribution.html)

- int -> Cauchy(double: median, double: scale, java.lang.String[]...: mods) -> double
- long -> Cauchy(double: median, double: scale, java.lang.String[]...: mods) -> double


## ChiSquared

@see [Wikipedia: Chi-squared distribution](https://en.wikipedia.org/wiki/Chi-squared_distribution) @see [Commons JavaDoc: ChiSquaredDistribution](https://commons.apache.org/proper/commons-statistics/commons-statistics-distribution/apidocs/org/apache/commons/statistics/distribution/ChiSquaredDistribution.html)

- int -> ChiSquared(double: degreesOfFreedom, java.lang.String[]...: mods) -> double
- long -> ChiSquared(double: degreesOfFreedom, java.lang.String[]...: mods) -> double


## CoinFunc

This is a higher-order function which takes an input value, and flips a coin. The first parameter is used as the threshold for choosing a function. If the sample values derived from the input is lower than the threshold value, then the first following function is used, and otherwise the second is used. For example, if the threshold is 0.23, and the input value is hashed and sampled in the unit interval to 0.43, then the second of the two provided functions will be used. The input value does not need to be hashed beforehand, since the user may need to use the full input value before hashing as the input to one or both of the functions. This function will accept either a LongFunction or a {@link Function} or a LongUnaryOperator in either position. If necessary, use {@link java.util.function.ToLongFunction} to adapt other function forms to be compatible with these signatures.

- java.lang.Long -> CoinFunc(double: threshold, java.lang.Object: first, java.lang.Object: second) -> java.lang.Object
  - *ex:* `CoinFunc(0.15,NumberNameToString(),Combinations('A:1:B:23'))` - *use the first function 15% of the time*


## ConstantContinuous

Always yields the same value. @see [Commons JavaDoc: ConstantContinuousDistribution](https://commons.apache.org/proper/commons-statistics/commons-statistics-distribution/apidocs/org/apache/commons/statistics/distribution/ConstantContinuousDistribution.html)

- int -> ConstantContinuous(double: value, java.lang.String[]...: mods) -> double
- long -> ConstantContinuous(double: value, java.lang.String[]...: mods) -> double


## Enumerated

Creates a probability density given the values and optional weights provided, in "value:weight value:weight ..." form. The weight can be elided for any value to use the default weight of 1.0d. @see [Commons JavaDoc: EnumeratedRealDistribution](http://commons.apache.org/proper/commons-math/apidocs/org/apache/commons/math4/distribution/EnumeratedRealDistribution.html)

- int -> Enumerated(java.lang.String: data, java.lang.String[]...: mods) -> double
  - *ex:* `Enumerated('1 2 3 4 5 6')` - *a fair six-sided die roll*
  - *ex:* `Enumerated('1:2.0 2 3 4 5 6')` - *an unfair six-sided die roll, where 1 has probability mass 2.0, and everything else has only 1.0*
- long -> Enumerated(java.lang.String: data, java.lang.String[]...: mods) -> double
  - *ex:* `Enumerated('1 2 3 4 5 6')` - *a fair 6-sided die*
  - *ex:* `Enumerated('1:2.0 2 3 4 5:0.5 6:0.5')` - *an unfair fair 6-sided die, where ones are twice as likely, and fives and sixes are half as likely*


## Exponential

@see [Wikipedia: Exponential distribution](https://en.wikipedia.org/wiki/Exponential_distribution) @see [Commons JavaDoc: ExponentialDistribution](https://commons.apache.org/proper/commons-statistics/commons-statistics-distribution/apidocs/org/apache/commons/statistics/distribution/ExponentialDistribution.html)

- int -> Exponential(double: mean, java.lang.String[]...: mods) -> double
- long -> Exponential(double: mean, java.lang.String[]...: mods) -> double


## F

@see [Wikipedia: F-distribution](https://en.wikipedia.org/wiki/F-distribution) @see [Commons JavaDoc: FDistribution](https://commons.apache.org/proper/commons-statistics/commons-statistics-distribution/apidocs/org/apache/commons/statistics/distribution/FDistribution.html) @see [Mathworld: F-Distribution](http://mathworld.wolfram.com/F-Distribution.html)

- int -> F(double: numeratorDegreesOfFreedom, double: denominatorDegreesOfFreedom, java.lang.String[]...: mods) -> double
- long -> F(double: numeratorDegreesOfFreedom, double: denominatorDegreesOfFreedom, java.lang.String[]...: mods) -> double


## Gamma

@see [Wikipedia: Gamma distribution](https://en.wikipedia.org/wiki/Gamma_distribution) @see [Commons JavaDoc: GammaDistribution](https://commons.apache.org/proper/commons-statistics/commons-statistics-distribution/apidocs/org/apache/commons/statistics/distribution/GammaDistribution.html)

- int -> Gamma(double: shape, double: scale, java.lang.String[]...: mods) -> double
- long -> Gamma(double: shape, double: scale, java.lang.String[]...: mods) -> double


## Geometric

@see [Wikipedia: Geometric distribution](http://en.wikipedia.org/wiki/Geometric_distribution) @see [Commons JavaDoc: GeometricDistribution](https://commons.apache.org/proper/commons-statistics/commons-statistics-distribution/apidocs/org/apache/commons/statistics/distribution/GeometricDistribution.html)

- int -> Geometric(double: p, java.lang.String[]...: modslist) -> int
- int -> Geometric(double: p, java.lang.String[]...: modslist) -> long
- long -> Geometric(double: p, java.lang.String[]...: modslist) -> int
- long -> Geometric(double: p, java.lang.String[]...: modslist) -> long


## Gumbel

@see [Wikipedia: Gumbel distribution](https://en.wikipedia.org/wiki/Gumbel_distribution) @see [Commons JavaDoc: GumbelDistribution](https://commons.apache.org/proper/commons-statistics/commons-statistics-distribution/apidocs/org/apache/commons/statistics/distribution/GumbelDistribution.html)

- int -> Gumbel(double: mu, double: beta, java.lang.String[]...: mods) -> double
- long -> Gumbel(double: mu, double: beta, java.lang.String[]...: mods) -> double


## Hypergeometric

@see [Wikipedia: Hypergeometric distribution](http://en.wikipedia.org/wiki/Hypergeometric_distribution) @see [Commons JavaDoc: HypergeometricDistribution](https://commons.apache.org/proper/commons-statistics/commons-statistics-distribution/apidocs/org/apache/commons/statistics/distribution/HypergeometricDistribution.html)

- int -> Hypergeometric(int: populationSize, int: numberOfSuccesses, int: sampleSize, java.lang.String[]...: modslist) -> int
- int -> Hypergeometric(int: populationSize, int: numberOfSuccesses, int: sampleSize, java.lang.String[]...: modslist) -> long
- long -> Hypergeometric(int: populationSize, int: numberOfSuccesses, int: sampleSize, java.lang.String[]...: modslist) -> int
- long -> Hypergeometric(int: populationSize, int: numberOfSuccesses, int: sampleSize, java.lang.String[]...: modslist) -> long


## Laplace

@see [Wikipedia: Laplace distribution](https://en.wikipedia.org/wiki/Laplace_distribution) @see [Commons JavaDoc: LaplaceDistribution](https://commons.apache.org/proper/commons-statistics/commons-statistics-distribution/apidocs/org/apache/commons/statistics/distribution/LaplaceDistribution.html)

- int -> Laplace(double: mu, double: beta, java.lang.String[]...: mods) -> double
- long -> Laplace(double: mu, double: beta, java.lang.String[]...: mods) -> double


## Levy

@see [Wikipedia: Lévy distribution](https://en.wikipedia.org/wiki/L%C3%A9vy_distribution) @see [Commons JavaDoc: LevyDistribution](https://commons.apache.org/proper/commons-statistics/commons-statistics-distribution/apidocs/org/apache/commons/statistics/distribution/LevyDistribution.html)

- int -> Levy(double: mu, double: c, java.lang.String[]...: mods) -> double
- long -> Levy(double: mu, double: c, java.lang.String[]...: mods) -> double


## LogNormal

@see [Wikipedia: Log-normal distribution](https://en.wikipedia.org/wiki/Log-normal_distribution) @see [Commons JavaDoc: LogNormalDistribution](https://commons.apache.org/proper/commons-statistics/commons-statistics-distribution/apidocs/org/apache/commons/statistics/distribution/LogNormalDistribution.html)

- int -> LogNormal(double: scale, double: shape, java.lang.String[]...: mods) -> double
- long -> LogNormal(double: scale, double: shape, java.lang.String[]...: mods) -> double


## Logistic

@see [Wikipedia: Logistic distribution](https://en.wikipedia.org/wiki/Logistic_distribution) @see [Commons JavaDoc: LogisticDistribution](https://commons.apache.org/proper/commons-statistics/commons-statistics-distribution/apidocs/org/apache/commons/statistics/distribution/LogisticDistribution.html)

- int -> Logistic(double: mu, double: scale, java.lang.String[]...: mods) -> double
- long -> Logistic(double: mu, double: scale, java.lang.String[]...: mods) -> double


## Nakagami

@see [Wikipedia: Nakagami distribution](https://en.wikipedia.org/wiki/Nakagami_distribution) @see [Commons JavaDoc: NakagamiDistribution](https://commons.apache.org/proper/commons-statistics/commons-statistics-distribution/apidocs/org/apache/commons/statistics/distribution/NakagamiDistribution.html)

- int -> Nakagami(double: mu, double: omega, java.lang.String[]...: mods) -> double
- long -> Nakagami(double: mu, double: omega, java.lang.String[]...: mods) -> double


## Normal

@see [Wikipedia: Normal distribution](https://en.wikipedia.org/wiki/Normal_distribution) @see [Commons JavaDoc: NormalDistribution](https://commons.apache.org/proper/commons-statistics/commons-statistics-distribution/apidocs/org/apache/commons/statistics/distribution/NormalDistribution.html)

- int -> Normal(double: mean, double: sd, java.lang.String[]...: mods) -> double
- long -> Normal(double: mean, double: sd, java.lang.String[]...: mods) -> double


## Pareto

@see [Wikipedia: Pareto distribution](https://en.wikipedia.org/wiki/Pareto_distribution) @see [Commons JavaDoc: ParetoDistribution](https://commons.apache.org/proper/commons-statistics/commons-statistics-distribution/apidocs/org/apache/commons/statistics/distribution/ParetoDistribution.html)

- int -> Pareto(double: scale, double: shape, java.lang.String[]...: mods) -> double
- long -> Pareto(double: scale, double: shape, java.lang.String[]...: mods) -> double


## Pascal

@see [Commons JavaDoc: PascalDistribution](https://commons.apache.org/proper/commons-statistics/commons-statistics-distribution/apidocs/org/apache/commons/statistics/distribution/PascalDistribution.html) @see [Wikipedia: Negative binomial distribution](https://en.wikipedia.org/wiki/Negative_binomial_distribution)

- int -> Pascal(int: r, double: p, java.lang.String[]...: modslist) -> int
- int -> Pascal(int: r, double: p, java.lang.String[]...: modslist) -> long
- long -> Pascal(int: r, double: p, java.lang.String[]...: modslist) -> int
- long -> Pascal(int: r, double: p, java.lang.String[]...: modslist) -> long


## Poisson

@see [Wikipedia: Poisson distribution](http://en.wikipedia.org/wiki/Poisson_distribution) @see [Commons JavaDoc: PoissonDistribution](https://commons.apache.org/proper/commons-statistics/commons-statistics-distribution/apidocs/org/apache/commons/statistics/distribution/PoissonDistribution.html)

- int -> Poisson(double: p, java.lang.String[]...: modslist) -> int
- int -> Poisson(double: p, java.lang.String[]...: modslist) -> long
- long -> Poisson(double: p, java.lang.String[]...: modslist) -> int
- long -> Poisson(double: p, java.lang.String[]...: modslist) -> long


## T

@see [Wikipedia: Student's t-distribution](https://en.wikipedia.org/wiki/Student's_t-distribution) @see [Commons JavaDoc: TDistribution](https://commons.apache.org/proper/commons-statistics/commons-statistics-distribution/apidocs/org/apache/commons/statistics/distribution/TDistribution.html)

- int -> T(double: degreesOfFreedom, java.lang.String[]...: mods) -> double
- long -> T(double: degreesOfFreedom, java.lang.String[]...: mods) -> double


## Triangular

@see [Wikipedia: Triangular distribution](https://en.wikipedia.org/wiki/Triangular_distribution) @see [Commons JavaDoc: TriangularDistribution](https://commons.apache.org/proper/commons-statistics/commons-statistics-distribution/apidocs/org/apache/commons/statistics/distribution/TriangularDistribution.html)

- int -> Triangular(double: a, double: c, double: b, java.lang.String[]...: mods) -> double
- long -> Triangular(double: a, double: c, double: b, java.lang.String[]...: mods) -> double


## Uniform

@see [Wikipedia: Uniform distribution (continuous)](https://en.wikipedia.org/wiki/Uniform_distribution_(continuous)) @see [Commons JavaDoc: UniformContinuousDistribution](https://commons.apache.org/proper/commons-statistics/commons-statistics-distribution/apidocs/org/apache/commons/statistics/distribution/UniformContinuousDistribution.html)

- int -> Uniform(double: lower, double: upper, java.lang.String[]...: mods) -> double
- long -> Uniform(double: lower, double: upper, java.lang.String[]...: mods) -> double
- int -> Uniform(int: lower, int: upper, java.lang.String[]...: modslist) -> int
- int -> Uniform(int: lower, int: upper, java.lang.String[]...: modslist) -> long
- long -> Uniform(int: lower, int: upper, java.lang.String[]...: modslist) -> int
- long -> Uniform(int: lower, int: upper, java.lang.String[]...: modslist) -> long


## Weibull

@see [Wikipedia: Weibull distribution](https://en.wikipedia.org/wiki/Weibull_distribution) @see [Wolfram Mathworld: Weibull Distribution](http://mathworld.wolfram.com/WeibullDistribution.html) @see [Commons Javadoc: WeibullDistribution](https://commons.apache.org/proper/commons-statistics/commons-statistics-distribution/apidocs/org/apache/commons/statistics/distribution/WeibullDistribution.html)

- int -> Weibull(double: alpha, double: beta, java.lang.String[]...: mods) -> double
- long -> Weibull(double: alpha, double: beta, java.lang.String[]...: mods) -> double


## WeightedFuncs

Allows for easy branching over multiple functions with specific weights.

- long -> WeightedFuncs(java.lang.Object[]...: weightsAndFuncs) -> java.lang.Object


## Zipf

@see [Wikipedia: Zipf's Law](https://en.wikipedia.org/wiki/Zipf's_law) @see [Commons JavaDoc: ZipfDistribution](https://commons.apache.org/proper/commons-statistics/commons-statistics-distribution/apidocs/org/apache/commons/statistics/distribution/ZipfDistribution.html)

- int -> Zipf(int: numberOfElements, double: exponent, java.lang.String[]...: modslist) -> int
- int -> Zipf(int: numberOfElements, double: exponent, java.lang.String[]...: modslist) -> long
- long -> Zipf(int: numberOfElements, double: exponent, java.lang.String[]...: modslist) -> int
- long -> Zipf(int: numberOfElements, double: exponent, java.lang.String[]...: modslist) -> long


