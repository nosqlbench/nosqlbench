- DateRangeFunc() would create zero-length ranges
  before. Now it correctly uses start and end times. If you have a test with this function,
  it may return larger results than before.
- 7c7ea039 (HEAD -> main) post-merge fixups
- 9403f51f improve ToDate semantics and docs
- 0edbfb1f fix test for TagFilter
- 365ad619 remove extraneous var in ActivityExecutor
- 34855a39 (origin/main) Merge pull request #336 from XN137/fix-errorprone-errors
- 60ff1d9c Merge pull request #335 from XN137/avoid-threadsafety-warnings
- c5caf51a Merge pull request #334 from XN137/remove-junit4-dependency
- a02df22d Merge pull request #333 from XN137/jdk-string-join
- 950b31fb fix errors found by errorprone
- b98451ab upgrade antlr4-maven-plugin to v4.9.2 avoid threadsafety warnings
- 5e8e6bf1 use assertj v3.19.0
- 8dcac028 use junit5 v5.7.2 with proper scoping
- 2cd393ae use junit5 apis to remove junit4 dependency
- 0e68abf1 prefer jdk over lib internals for String join
- dc1c6c5e Merge pull request #332 from XN137/fix-virtdata-api-test-folder
- 6232edff move virtdata-api tests to correct folder


