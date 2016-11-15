[![Build Status](https://travis-ci.org/mach6/testng-dependency-reporter.svg?branch=master)](https://travis-ci.org/mach6/testng-dependency-reporter)

Dependency Reporter for TestNG
==============================
An `IReporter` and `IResultLisener` for TestNG that can analyze the test `ISuite`,
determine the dependency graph, and produce .dot (graphviz), .png,
or .json representations of the graph at each node.

[![example png](examples/report_thumb.png)](examples/report.png)

Current version
-------------------
1.0.0-SNAPSHOT

Availability
-------------------
Not yet published. Source code only.

License
-------
[Apache Software License v2.0](http://www.apache.org/licenses/LICENSE-2.0)

Compilation
-----
```shell
$ mvn clean install
```

Installation
-----
1. Install graphviz for the executable `dot` to `/usr/local/bin/dot`.
On a Mac you can do this with homebrew from a terminal with:
```shell
brew install graphviz
```

2. Use the `testng-dependency-reporter-{version}.jar` as a test dependency for your project by
adding it to the _CLASSPATH_. With Maven, this looks like:
```xml
<dependency>
  <groupId>net.mach6</groupId>
  <artifactId>testng-dependency-reporter</artifactId>
  <version>1.0.0-SNAPSHOT</version>
  <scope>test</scope>
</dependency>
```

Usage
-----
With Maven Surefire
```shell
$ mvn test [-DdependecyReporter=[report options]] [surefire options]
```
With vanilla `java`, assuming all jars are in your CLASSPATH
```shell
$ java [-DdependencyReporter=[report options]] org.testng.TestNG [testng options]
```

__Report Options__

All "command line" options for this reporter are passed via the JVM system
property `dependencyReporter`. The format is `option:value[,option:value...]`

| Option | Values (bold is the default) |
| :----: | :--------------------------- |
| `enabled` | __true__, _false_ |
| `prescan` | _true_, __false__ |
| `mode`| __all__, _suites_, _tests_, _classes_, _methods_ |
| `output` | __all__, _dot_, _png_, _json_ |

For example:
```shell
$ mvn test -DdependencyReporter=prescan:true,output:png
```

Output
-----
All output will be in a sub-folder named `DependencyReporter` whose parent folder
is the folder that contains other TestNG output.
- In a maven-surefire project this will typically be `target/surefire-reports/`.
- In a non-maven project, this will typically be `test-output/`.

Credits
-------
Inspired by [this work](https://github.com/tomekkaczanowski/testng-test-dependencies-reporter) of Tomek Kaczanowski
