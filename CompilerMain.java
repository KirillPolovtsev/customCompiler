import java.io.*;
import java_cup.runtime.*;

/**
 * Main compiler driver that integrates all phases:
 * 1. Lexical Analysis (JFlex)
 * 2. Parsing (CUP) -> AST
 * 3. Type Checking (Semantic Analysis)
 * 4. Optimization (constant folding, propagation, algebraic simplifications, dead code elimination)
 * 5. Code Generation (MIPS assembly)
 *
 * Usage: java CompilerMain [options] <input_file>
 * Options:
 *   -o <file>     Output file for MIPS assembly (default: output.asm)
 *   -O            Enable optimizations (default: enabled)
 *   -O0           Disable optimizations
 *   -ast          Print AST after parsing
 *   -opt-ast      Print AST after optimization
 *   -help         Show this help message
 */
public class CompilerMain {
    
    private static boolean enableOptimizations = true;
    private static boolean printAST = false;
    private static boolean printOptimizedAST = false;
    private static String outputFile = "output.asm";
    private static String inputFile = null;
    
    public static void main(String[] args) {
        // Parse command line arguments
        if (!parseArgs(args)) {
            System.exit(1);
        }
        
        if (inputFile == null) {
            System.err.println("Error: No input file specified");
            printUsage();
            System.exit(1);
        }
        
        try {
            compile(inputFile);
        } catch (Exception e) {
            System.err.println("Compilation failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    private static boolean parseArgs(String[] args) {
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            
            if (arg.equals("-help") || arg.equals("--help")) {
                printUsage();
                System.exit(0);
            } else if (arg.equals("-o")) {
                if (i + 1 >= args.length) {
                    System.err.println("Error: -o requires an output filename");
                    return false;
                }
                outputFile = args[++i];
            } else if (arg.equals("-O")) {
                enableOptimizations = true;
            } else if (arg.equals("-O0")) {
                enableOptimizations = false;
            } else if (arg.equals("-ast")) {
                printAST = true;
            } else if (arg.equals("-opt-ast")) {
                printOptimizedAST = true;
            } else if (arg.startsWith("-")) {
                System.err.println("Error: Unknown option: " + arg);
                return false;
            } else {
                inputFile = arg;
            }
        }
        return true;
    }
    
    private static void printUsage() {
        System.out.println("Usage: java CompilerMain [options] <input_file>");
        System.out.println("Options:");
        System.out.println("  -o <file>     Output file for MIPS assembly (default: output.asm)");
        System.out.println("  -O            Enable optimizations (default: enabled)");
        System.out.println("  -O0           Disable optimizations");
        System.out.println("  -ast          Print AST after parsing");
        System.out.println("  -opt-ast      Print AST after optimization");
        System.out.println("  -help         Show this help message");
    }
    
    private static void compile(String filename) throws Exception {
        System.out.println("=== Compiling: " + filename + " ===");
        
        // Phase 1: Lexical Analysis and Parsing
        System.out.println("Phase 1: Parsing...");
        File input = new File(filename);
        if (!input.canRead()) {
            throw new Exception("Cannot read input file: " + filename);
        }
        
        Reader reader = new FileReader(input);
        Lexer lexer = new Lexer(reader);
        parser parser = new parser(lexer);
        
        Program program = null;
        try {
            program = (Program) parser.parse().value;
        } catch (Exception e) {
            throw new Exception("Parse error: " + e.getMessage());
        }
        
        if (program == null) {
            throw new Exception("Parsing failed - no AST generated");
        }
        System.out.println("  Parsing completed successfully.");
        
        if (printAST) {
            System.out.println("\n=== AST (after parsing) ===");
            System.out.println(program.toString(0));
            System.out.println("=== End AST ===\n");
        }
        
        // Phase 2: Semantic Analysis (Type Checking)
        System.out.println("Phase 2: Type checking...");
        try {
            program.typeCheck();
        } catch (LangException e) {
            throw new Exception("Type error: " + e.toString());
        }
        System.out.println("  Type checking completed successfully.");
        
        // Phase 3: Optimization
        if (enableOptimizations) {
            System.out.println("Phase 3: Optimizing...");
            Optimizer optimizer = new Optimizer();
            program = optimizer.optimize(program);
            System.out.println("  Optimization completed.");
            
            if (printOptimizedAST) {
                System.out.println("\n=== AST (after optimization) ===");
                System.out.println(program.toString(0));
                System.out.println("=== End AST ===\n");
            }
        } else {
            System.out.println("Phase 3: Optimization skipped (disabled).");
        }
        
        // Phase 4: Code Generation
        System.out.println("Phase 4: Generating MIPS assembly...");
        MIPSCodeGenerator codeGen = new MIPSCodeGenerator();
        String mipsCode = codeGen.generate(program);
        
        // Write output
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {
            writer.print(mipsCode);
        }
        System.out.println("  MIPS assembly written to: " + outputFile);
        
        System.out.println("\n=== Compilation successful! ===");
    }
}
