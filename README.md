# Wiki Page - Planned Architecture

### Define Design Purpose

### Primary Quality Attributes
Efficiency: Users will be able to easily identify how to select design patterns, principle violations, and cursory checks to apply on their written code. It will be made to support repetitive use.
Consistency: The project will maintain both internal and external consistency through the usage of the three-layer design and adequate affordance of our UI elements.
Learnability: The project will be inuitive; users can utilize the UI without confusion or frustration. It is made to be repetitive, so they won't run into anything unexpected after the initial learning curve.
Error Minimizing: The project will provide detailed error messages, should it receive invalid user input or runs into other unexpected errors. Providing the users with accurate feedback is crucial.
Flexibility: The project will be built to allow for extension and enhancement while maintaining the existing code.
Regression: When testing, all tests can be run easily from the same interface.
Isolation: When testing, specific tests can be selected for more precise testing.

### How our primary functionality works and how it will interact with users

Our system takes a Java project and scans it for design problems using a set of linter checks, where each check looks for a specific issue like a design principle violation or a bad pattern. Users run the tool and choose which project and checks they want to use, and the system then executes those checks on the code. As the checks run, the system collects any issues it finds. Once finished, it generates a simple report. This report explains what design problems were found in a clear manner.

### Which reference architecture are we using
