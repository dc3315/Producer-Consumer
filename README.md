# Description
Answer to a producer consumer code challenge.
Requirements: code in Java or Python. No standard library usage.

# Suggested Usage
Import the `src`, `test` and `lib` directories into a new IntelliJ project.
This code uses java JDK 13 features!

# Dependencies
JUnit 4, JDK 13

# Comments
## Notes
- Explanations of design decisions can be found in the code.

- Correctness is the first requirement to a program, after correctness, we are interested in performance.
The correctness and performance of this program depends a lot on the environment that it is run in:
Computer OS, running programs, amount of RAM, Memory allocated to JVM (heap, permgen, stack size, etc),
and of course the volume of data being processed.

- Thorough integration testing should be done and a performance 
engineering approach should be taken to find the
optimal parameters to this program based on the environment/context.

- A lot of I/O is being done in this program, so that will be the most likely bottleneck,
hence a focus on I/O input and output would speed up performance. If the format is constant,
why not use _binary files_ instead of raw text files? This also means less disk space consumption.

## About external libraries
If I had to solve this issue in a professional context, I would look to see if an open-source library
already solves the problem or part of the problem, this would save time, bugs and most likely provide better
performance than my own code given that it is more mature. In this particular scenario, I believe Apache Kafka would do a great job: https://kafka.apache.org/uses

# Project Structure
```
.
├── README.md
├── lib
│   ├── hamcrest-core-1.3.jar
│   └── junit-4.12.jar
├── src
│   └── com
│       └── clarity
│           └── connectionsFileParser
│               ├── ConnectionsParser.java
│               ├── Main.java
│               └── TailingConnectionsParser.java
└── test
    └── com
        └── clarity
            └── connectionsFileParser
                ├── Integration
                │   └── TailingConnectionsParserTest.java
                ├── Unit
                │   ├── ConnectionsParserTest.java
                │   └── MainTest.java
                ├── src
                │   ├── AutoDeletingTempFile.java
                │   └── RandomConnectionsSimulator.java
                └── testFiles
                    ├── input-file-0.txt
                    ├── input-file-10000.txt
                    ├── input-file-5.txt
                    ├── input-file-7.txt
                    └── randomNames.txt
```
