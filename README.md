# Description
Answer to a producer consumer code challenge.
Requirements: code in Java or Python. No standard library usage.

# Suggested Usage
Import the `src`, `test` and `lib` directories into a new IntelliJ project.
This code uses java JDK 13 features!

# Dependencies
JUnit 4, JDK 13

# Assumptions + Comments
## Disclaimer 
- Correctness is the first requirement to a program, after correctness, we are interested in performance.

- The correctness and performance of this program depends a lot on the environment that it is run in:
Computer OS, running programs, amount of RAM, Memory allocated to JVM (heap, permgen, stack size, etc),
and of course the volume of data being processed.

- Thorough integration testing should be done and a performance 
engineering approach should be taken to find the
optimal parameters to this program based on the environment/context.

- A lot of I/O is being done in this program, so that will be the most likely bottleneck,
hence a focus on I/O input and output would speed up performance. If the format is constant,
why not use _binary files_ instead of raw text files? This also means less disk space consumption.

## About external libraries
If I had to solve this issue in a professional context, I would look to see if an existing library
already solves the issue instead of reinventing the wheel because it means less bugs, and most likely 
better performance and more time saved.
