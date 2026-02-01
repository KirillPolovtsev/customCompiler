The entire project should be runnable from the makefile.

To test for project 3, which will include any .as files in the p3tests/p3tests directory.

Run, in terminal:

make clean

make typecheck

The outputs of the command should be piped to the main directory and end in -output.txt

To compile a source file to MIPS assembly (with optimization enabled):

make compile FILE=yourfile.as

To compile without optimization:

make compile-no-opt FILE=yourfile.as

To compile and display AST before and after optimization:

make compile-verbose FILE=yourfile.as

To compile all .as files in the directory:

make compile-all

The compiled output will be saved as .asm files in the same directory.
