# Compiler-Design

## Overview
This project contains implementations for Java compiler as part of the Compiler Design course (CS3300) at IIT Madras. The grammar of Macrojava can be found at `macrojava-spec.html`.

## Stages of the Compiler

### Stage 1: Lexical Analysis (P1)

### Stage 2: Syntax Analysis and Type Checking (P2)

### Stage 3: Intermediate Representation (IR) Generation (P3)

### Stage 4: MicroIR Translation (P4)

### Stage 5: Assembly Code Generation (P5)

## Build Instructions

### Prerequisites
- **GCC**
- **Flex**
- **Bison**
- **Java**

### Build Steps
1. Clone the repository.
2. Navigate to the root directory.
3. Run `make` to build all stages.
4. Use `make clean` to remove generated files and binaries.

### Running the Compiler
* Input must be a single file `input.java` in this directory.
* Run the pipeline using the command:
```bash
make run
```
This will execute all stages in sequence and output the generated MIPS assembly code into `output.mips`.

## References
For more details, visit the course website: [Compiler Design - IIT Madras](https://www.cse.iitm.ac.in/~krishna/cs3300/).

