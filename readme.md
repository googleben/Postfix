# Postfix Compiler

A quick, ~~dirty~~ naive, and overly complicated compiler for compiling floating point postfix expressions to Java bytecode.

Source files end with .postfix, output is standard Java .class files.

This project is tiny and actually terrible, as it's intended as to be a way to learn how Java class files are structured and how to manipulate them using the Bytecode Engineering Library in Apache commons. It's probably broken in more than one way.

## To run

Run PostfixCompiler with the input filename as an argument, then simply execute the resultant .class file as you would any other Java file. See `Example.postfix` for an example of a valid postfix file.

## Source code format

Any lines ending with "=" will be interpreted as an assignment statement (not expression). All other lines will evaluate as an expression and then print to stdout.

Operators:

| Operator | Description |
| --- | --- |
| `=` | Assignment |
| `*` | Multiply |
| `/` | Divide |
| `+` | Add |
| `-` | Subtract |
| `%` | Modulus |

## A note on assignment and expressions vs. statements

In the context of my postfix grammar, a statement is something that can stand by itself on a line (such as `5`, `x 2 =`, or `x`), and its impact on the JVM stack is net zero. During the execution of the statement, values may be pushed to or popped from the stack, but the number of pushes will always be equal to the number of pops. Expressions, on the other hand, may have a net positive or negative impact on the stack (in the current version, all expressions leave a single extra value on the stack). Anything not considered a statement is considered an expression - and it just so happens that, in the current version, anything that can be considered a statement can also be considered an expression. This is a side effect of always calling either `System.out.println` or assigning a variable a value at the end of every line; however, if the `=` operator is used in the middle of a statement rather than at the end, it is considered the operator of an expression, not a statement. This means that, in the middle of a line, the assignment operator (`=`) may be used to assign a variable a value and then *continue working with that value*. For example, consider the statement `x y 2 = 2 * =`. At the end of evaluation, `y` will have been assigned the value `2` and `x` the value `4`.