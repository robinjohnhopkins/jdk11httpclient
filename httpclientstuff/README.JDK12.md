## JDK12 notes


### Files mismatch find first byte difference between two files

```
Files.mismatch(Path.of("/file1"), Path.of("/file2));
-1 or first mismatching byte
```


### Compact Number Formats

```
NumberFormat germanNF =
   NumberFormat.getCompactNumberInstance(
     Locale.GERMAN, NumberFormat.Style.SHORT);
String short = shortNF.format(1_000_000);
```


### ApiUpdates

tee split out stream to two functions, combine is third function that recombines.


### Preview Feature (might change) : switch

--enable-preview flag

```
java -cp  jdk12investigation/target/jdk12investigation-1.0-SNAPSHOT.jar jdk12/investigate/switchexpressions/Main
Relax
```


## Micro Benchmarking

measure execution time of small pieces of code
typically compare alternatives;prevent performance regressions.

Dead code elimination - try to return a value from benchmark so that compiler does actually run code :)

Other compiler optimisations

When compiled, jdk11httpclient/httpclientstuff/jmh.benchmark/target/benchmarks.jar
can be run to start the benchmarks.

```
java -jar target/benchmarks.jar
```

From the same target directory, it uses:
```
target/jmh.benchmark-1.0-SNAPSHOT.jar
target/original-benchmarks.jar
```

Example run:
```
Result "parseInt":
  30.279 ±(99.9%) 1.821 ns/op [Average]
  (min, avg, max) = (28.154, 30.279, 36.180), stdev = 2.097
  CI (99.9%): [28.458, 32.100] (assumes normal distribution)


# Run complete. Total time: 00:02:01

Benchmark              (toParse)  Mode  Cnt   Score   Error  Units
MyBenchmark.parseInt           1  avgt   20   9.076 ± 0.024  ns/op
MyBenchmark.parseInt       12345  avgt   20  21.634 ± 0.153  ns/op
MyBenchmark.parseInt  2147483647  avgt   20  30.279 ± 1.821  ns/op


NB: If Error column looked like this, then 9.821 might be a flag
    to use a profiler perhaps to investigate further.
   Error  Units
 ± 0.024  ns/op
 ± 0.153  ns/op
 ± 9.821  ns/op
```

java12 man command line to create micro-benchmark project
```
mvn archetype:generate \
          -DinteractiveMode=false \
          -DarchetypeGroupId=org.openjdk.jmh \
          -DarchetypeArtifactId=jmh-java-benchmark-archetype \
          -DgroupId=org.sample \
          -DartifactId=jmh-number-verification-performance-test \
          -Dversion=1.0
```

