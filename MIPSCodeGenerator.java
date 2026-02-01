import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

/**
 * Generates MIPS assembly code from the AST.
 * 
 * - $t0-$t9: temporary registers for expression evaluation
 * - $s0-$s7: saved registers for local variables
 * - $a0-$a3: argument registers
 * - $v0-$v1: return value registers
 * - $sp: stack pointer
 * - $fp: frame pointer
 * - $ra: return address
 */
public class MIPSCodeGenerator {
    
    private StringBuilder dataSection;
    private StringBuilder textSection;
    private int labelCounter;
    private int stringCounter;
    private int tempRegister;
    private int floatTempRegister;
    
    // Symbol table for variable locations (offset from $fp)
    private Map<String, Integer> variableOffsets;
    private Map<String, String> variableTypes;
    private int currentOffset;
    
    // Track string literals for data section
    private Map<String, String> stringLiterals;
    
    // Current method context
    private String currentMethodName;
    private List<String> methodEndLabels;
    
    public MIPSCodeGenerator() {
        dataSection = new StringBuilder();
        textSection = new StringBuilder();
        labelCounter = 0;
        stringCounter = 0;
        tempRegister = 0;
        floatTempRegister = 0;
        variableOffsets = new HashMap<>();
        variableTypes = new HashMap<>();
        stringLiterals = new HashMap<>();
        methodEndLabels = new ArrayList<>();
        currentOffset = 0;
    }
    
    public String generate(Program program) {
        // Initialize data section
        dataSection.append(".data\n");
        dataSection.append("_newline: .asciiz \"\\n\"\n");
        dataSection.append("_true: .asciiz \"true\"\n");
        dataSection.append("_false: .asciiz \"false\"\n");
        
        // Initialize text section
        textSection.append("\n.text\n");
        textSection.append(".globl main\n\n");
        
        // Generate code for the program
        generateProgram(program);
        
        // Add string literals to data section
        for (Map.Entry<String, String> entry : stringLiterals.entrySet()) {
            dataSection.append(entry.getValue()).append(": .asciiz ").append(entry.getKey()).append("\n");
        }
        
        return dataSection.toString() + textSection.toString();
    }
    
    private void generateProgram(Program program) {
        if (program.memberDeclarations != null) {
            generateMemberDecls(program.memberDeclarations);
        }
    }
    
    private void generateMemberDecls(MemberDecls decls) {
        if (decls.fieldsAndMethods != null) {
            generateFieldsNMethods(decls.fieldsAndMethods);
        }
    }
    
    private void generateFieldsNMethods(FieldsNMethods fnm) {
        if (fnm == null) return;
        
        if (fnm.fieldDeclaration != null) {
            generateFieldDecl(fnm.fieldDeclaration);
        }
        if (fnm.methodDeclaration != null) {
            generateMethodDecl(fnm.methodDeclaration);
        }
        if (fnm.fieldsAndMethods != null) {
            generateFieldsNMethods(fnm.fieldsAndMethods);
        }
        if (fnm.methodDeclarations != null) {
            generateMethodDecls(fnm.methodDeclarations);
        }
    }
    
    private void generateMethodDecls(MethodDecls decls) {
        if (decls == null) return;
        if (decls.methodDeclaration != null) {
            generateMethodDecl(decls.methodDeclaration);
        }
        if (decls.methodDeclarations != null) {
            generateMethodDecls(decls.methodDeclarations);
        }
    }
    
    private void generateFieldDecl(FieldDecl decl) {
        // Global variables go in data section
        String varName = decl.fieldStart.id;
        String type = decl.fieldStart.type.toString(0);
        
        if (decl instanceof FieldDeclSingle) {
            FieldDeclSingle single = (FieldDeclSingle) decl;
            if (type.equals("int") || type.equals("bool")) {
                dataSection.append("_").append(varName).append(": .word 0\n");
            } else if (type.equals("float")) {
                dataSection.append("_").append(varName).append(": .float 0.0\n");
            } else if (type.equals("char")) {
                dataSection.append("_").append(varName).append(": .byte 0\n");
            }
            variableTypes.put(varName, type);
        } else if (decl instanceof FieldDeclArray) {
            FieldDeclArray arr = (FieldDeclArray) decl;
            int size = arr.intlit;
            if (type.equals("int") || type.equals("bool")) {
                dataSection.append("_").append(varName).append(": .space ").append(size * 4).append("\n");
            } else if (type.equals("float")) {
                dataSection.append("_").append(varName).append(": .space ").append(size * 4).append("\n");
            } else if (type.equals("char")) {
                dataSection.append("_").append(varName).append(": .space ").append(size).append("\n");
            }
            variableTypes.put(varName, type + "[]");
        }
    }
    
    private void generateMethodDecl(MethodDecl method) {
        String methodName = method.methodStart.id;
        currentMethodName = methodName;
        String endLabel = newLabel("end_" + methodName);
        methodEndLabels.add(endLabel);
        
        // Reset local variable tracking
        variableOffsets.clear();
        currentOffset = 0;
        
        // Generate method label
        textSection.append(methodName).append(":\n");
        
        // Prologue: set up stack frame
        emit("addi $sp, $sp, -4");
        emit("sw $fp, 0($sp)");
        emit("move $fp, $sp");
        emit("addi $sp, $sp, -4");
        emit("sw $ra, 0($sp)");
        
        // Count local variables for stack allocation
        int localVarCount = countLocalVariables(method);
        if (localVarCount > 0) {
            emit("addi $sp, $sp, -" + (localVarCount * 4));
        }
        currentOffset = -8; // Start after saved $fp and $ra
        
        // Process arguments
        if (method.argumentDeclarations != null) {
            generateArgDecls(method.argumentDeclarations);
        }
        
        // Process local field declarations
        if (method.fieldDeclarations != null) {
            generateLocalFieldDecls(method.fieldDeclarations);
        }
        
        // Generate statements
        if (method.statements != null) {
            generateStmts(method.statements);
        }
        
        // Method end label and epilogue
        textSection.append(endLabel).append(":\n");
        
        // Epilogue: restore stack frame
        emit("lw $ra, -4($fp)");
        emit("move $sp, $fp");
        emit("lw $fp, 0($sp)");
        emit("addi $sp, $sp, 4");
        emit("jr $ra");
        textSection.append("\n");
        
        methodEndLabels.remove(methodEndLabels.size() - 1);
        currentMethodName = null;
    }
    
    private int countLocalVariables(MethodDecl method) {
        int count = 0;
        if (method.argumentDeclarations != null) {
            count += countArgDecls(method.argumentDeclarations);
        }
        if (method.fieldDeclarations != null) {
            count += countFieldDecls(method.fieldDeclarations);
        }
        return count;
    }
    
    private int countArgDecls(ArgDecls decls) {
        if (decls == null) return 0;
        int count = 1;
        if (decls.argumentDeclarations != null) {
            count += countArgDecls(decls.argumentDeclarations);
        }
        return count;
    }
    
    private int countFieldDecls(FieldDecls decls) {
        if (decls == null) return 0;
        int count = 1;
        if (decls.fieldDeclarations != null) {
            count += countFieldDecls(decls.fieldDeclarations);
        }
        return count;
    }
    
    private void generateArgDecls(ArgDecls decls) {
        if (decls == null) return;
        
        if (decls.argumentDeclaration != null) {
            ArgDecl arg = decls.argumentDeclaration;
            String type;
            String id;
            
            if (arg instanceof ArgDeclSingle) {
                ArgDeclSingle single = (ArgDeclSingle) arg;
                type = single.type.toString(0);
                id = single.id;
            } else {
                ArgDeclArray arr = (ArgDeclArray) arg;
                type = arr.type.toString(0) + "[]";
                id = arr.id;
            }
            
            currentOffset -= 4;
            variableOffsets.put(id, currentOffset);
            variableTypes.put(id, type);
        }
        
        if (decls.argumentDeclarations != null) {
            generateArgDecls(decls.argumentDeclarations);
        }
    }
    
    private void generateLocalFieldDecls(FieldDecls decls) {
        if (decls == null) return;
        
        if (decls.fieldDeclaration != null) {
            FieldDecl decl = decls.fieldDeclaration;
            String varName = decl.fieldStart.id;
            String type = decl.fieldStart.type.toString(0);
            
            currentOffset -= 4;
            variableOffsets.put(varName, currentOffset);
            variableTypes.put(varName, type);
            
            // Initialize if there's an expression
            if (decl instanceof FieldDeclSingle) {
                FieldDeclSingle single = (FieldDeclSingle) decl;
                if (single.expression != null && single.expression.expression != null) {
                    generateExpr(single.expression.expression);
                    int offset = variableOffsets.get(varName);
                    emit("sw $t0, " + offset + "($fp)");
                }
            }
        }
        
        if (decls.fieldDeclarations != null) {
            generateLocalFieldDecls(decls.fieldDeclarations);
        }
    }
    
    private void generateStmts(Stmts stmts) {
        if (stmts == null) return;
        
        if (stmts.statement != null) {
            generateStmt(stmts.statement);
        }
        
        if (stmts.statements != null) {
            generateStmts(stmts.statements);
        }
    }
    
    private void generateStmt(Stmt stmt) {
        if (stmt == null) return;
        
        if (stmt instanceof AssmntStmt) {
            generateAssignment((AssmntStmt) stmt);
        } else if (stmt instanceof IfStmt) {
            generateIf((IfStmt) stmt);
        } else if (stmt instanceof WhileStmt) {
            generateWhile((WhileStmt) stmt);
        } else if (stmt instanceof PrintStmt) {
            generatePrint((PrintStmt) stmt);
        } else if (stmt instanceof PrintLnStmt) {
            generatePrintLn((PrintLnStmt) stmt);
        } else if (stmt instanceof ReadStmt) {
            generateRead((ReadStmt) stmt);
        } else if (stmt instanceof ValueReturn) {
            generateValueReturn((ValueReturn) stmt);
        } else if (stmt instanceof VoidReturn) {
            generateVoidReturn();
        } else if (stmt instanceof ScopeStmt) {
            generateScope((ScopeStmt) stmt);
        } else if (stmt instanceof VoidFuncStmt) {
            generateVoidFuncStmt((VoidFuncStmt) stmt);
        } else if (stmt instanceof ArgFuncStmt) {
            generateArgFuncStmt((ArgFuncStmt) stmt);
        } else if (stmt instanceof UnaryStmt) {
            generateUnaryStmt((UnaryStmt) stmt);
        }
    }
    
    private void generateAssignment(AssmntStmt stmt) {
        // Generate code for RHS expression (result in $t0)
        generateExpr(stmt.expression);
        
        // Store to variable
        String varName = stmt.name.id;
        
        if (stmt.name instanceof NameArray) {
            // Array assignment
            NameArray arr = (NameArray) stmt.name;
            emit("addi $sp, $sp, -4");
            emit("sw $t0, 0($sp)"); // Save value
            generateExpr(arr.expression); // Index in $t0
            emit("sll $t1, $t0, 2"); // Multiply by 4
            
            if (variableOffsets.containsKey(varName)) {
                // Local array
                int offset = variableOffsets.get(varName);
                emit("addi $t2, $fp, " + offset);
                emit("add $t2, $t2, $t1");
            } else {
                // Global array
                emit("la $t2, _" + varName);
                emit("add $t2, $t2, $t1");
            }
            emit("lw $t0, 0($sp)");
            emit("addi $sp, $sp, 4");
            emit("sw $t0, 0($t2)");
        } else {
            // Simple variable assignment
            if (variableOffsets.containsKey(varName)) {
                // Local variable
                int offset = variableOffsets.get(varName);
                emit("sw $t0, " + offset + "($fp)");
            } else {
                // Global variable
                emit("la $t1, _" + varName);
                emit("sw $t0, 0($t1)");
            }
        }
    }
    
    private void generateIf(IfStmt stmt) {
        String elseLabel = newLabel("else");
        String endLabel = newLabel("endif");
        
        // Generate condition
        generateExpr(stmt.ifBase.expression);
        
        // Branch if false
        if (stmt.state2 != null) {
            emit("beq $t0, $zero, " + elseLabel);
        } else {
            emit("beq $t0, $zero, " + endLabel);
        }
        
        // Then branch
        if (stmt.state1 != null) {
            generateStmt(stmt.state1);
        }
        
        if (stmt.state2 != null) {
            emit("j " + endLabel);
            textSection.append(elseLabel).append(":\n");
            generateStmt(stmt.state2);
        }
        
        textSection.append(endLabel).append(":\n");
    }
    
    private void generateWhile(WhileStmt stmt) {
        String loopLabel = newLabel("while");
        String endLabel = newLabel("endwhile");
        
        textSection.append(loopLabel).append(":\n");
        
        // Generate condition
        generateExpr(stmt.whileBase.expression);
        emit("beq $t0, $zero, " + endLabel);
        
        // Loop body
        if (stmt.body != null) {
            generateStmt(stmt.body);
        }
        
        emit("j " + loopLabel);
        textSection.append(endLabel).append(":\n");
    }
    
    private void generatePrint(PrintStmt stmt) {
        if (stmt.printList != null) {
            generatePrintList(stmt.printList);
        }
    }
    
    private void generatePrintList(PrintList list) {
        if (list == null) return;
        
        if (list.expression != null) {
            generateExpr(list.expression);
            
            // Determine type and print accordingly
            String type = getExprType(list.expression);
            
            if (type.equals("int")) {
                emit("move $a0, $t0");
                emit("li $v0, 1"); // print_int syscall
                emit("syscall");
            } else if (type.equals("float")) {
                emit("mtc1 $t0, $f12");
                emit("li $v0, 2"); // print_float syscall
                emit("syscall");
            } else if (type.equals("char")) {
                emit("move $a0, $t0");
                emit("li $v0, 11"); // print_char syscall
                emit("syscall");
            } else if (type.equals("bool")) {
                String trueLabel = newLabel("print_true");
                String endPrint = newLabel("end_print");
                emit("beq $t0, $zero, " + trueLabel);
                emit("la $a0, _true");
                emit("j " + endPrint);
                textSection.append(trueLabel).append(":\n");
                emit("la $a0, _false");
                textSection.append(endPrint).append(":\n");
                emit("li $v0, 4"); // print_string syscall
                emit("syscall");
            } else if (type.equals("string")) {
                emit("move $a0, $t0");
                emit("li $v0, 4"); // print_string syscall
                emit("syscall");
            } else {
                // Default: print as integer
                emit("move $a0, $t0");
                emit("li $v0, 1");
                emit("syscall");
            }
        }
        
        if (list.printList != null) {
            generatePrintList(list.printList);
        }
    }
    
    private void generatePrintLn(PrintLnStmt stmt) {
        // Print newline
        emit("la $a0, _newline");
        emit("li $v0, 4");
        emit("syscall");
    }
    
    private void generateRead(ReadStmt stmt) {
        if (stmt.readList != null) {
            generateReadList(stmt.readList);
        }
    }
    
    private void generateReadList(ReadList list) {
        if (list == null) return;
        
        if (list.name != null) {
            String varName = list.name.id;
            String type = variableTypes.getOrDefault(varName, "int");
            
            if (type.equals("int")) {
                emit("li $v0, 5"); // read_int syscall
                emit("syscall");
                emit("move $t0, $v0");
            } else if (type.equals("float")) {
                emit("li $v0, 6"); // read_float syscall
                emit("syscall");
                emit("mfc1 $t0, $f0");
            } else if (type.equals("char")) {
                emit("li $v0, 12"); // read_char syscall
                emit("syscall");
                emit("move $t0, $v0");
            }
            
            // Store to variable
            if (variableOffsets.containsKey(varName)) {
                int offset = variableOffsets.get(varName);
                emit("sw $t0, " + offset + "($fp)");
            } else {
                emit("la $t1, _" + varName);
                emit("sw $t0, 0($t1)");
            }
        }
        
        if (list.readList != null) {
            generateReadList(list.readList);
        }
    }
    
    private void generateValueReturn(ValueReturn stmt) {
        generateExpr(stmt.expression);
        emit("move $v0, $t0");
        
        // Jump to method epilogue
        if (!methodEndLabels.isEmpty()) {
            emit("j " + methodEndLabels.get(methodEndLabels.size() - 1));
        }
    }
    
    private void generateVoidReturn() {
        if (!methodEndLabels.isEmpty()) {
            emit("j " + methodEndLabels.get(methodEndLabels.size() - 1));
        }
    }
    
    private void generateScope(ScopeStmt stmt) {
        if (stmt.fieldDeclarations != null) {
            generateLocalFieldDecls(stmt.fieldDeclarations);
        }
        if (stmt.statements != null) {
            generateStmts(stmt.statements);
        }
    }
    
    private void generateVoidFuncStmt(VoidFuncStmt stmt) {
        emit("jal " + stmt.id);
    }
    
    private void generateArgFuncStmt(ArgFuncStmt stmt) {
        // Push arguments onto stack (right to left)
        int argCount = countArgs(stmt.arguments);
        generateArgs(stmt.arguments, argCount);
        
        // Call function
        emit("jal " + stmt.id);
        
        // Clean up arguments from stack
        if (argCount > 0) {
            emit("addi $sp, $sp, " + (argCount * 4));
        }
    }
    
    private int countArgs(Args args) {
        if (args == null) return 0;
        int count = 1;
        if (args.arguments != null) {
            count += countArgs(args.arguments);
        }
        return count;
    }
    
    private void generateArgs(Args args, int argNum) {
        if (args == null) return;
        
        // Process remaining args first (right to left)
        if (args.arguments != null) {
            generateArgs(args.arguments, argNum - 1);
        }
        
        // Generate this argument
        if (args.expression != null) {
            generateExpr(args.expression);
            emit("addi $sp, $sp, -4");
            emit("sw $t0, 0($sp)");
        }
    }
    
    private void generateUnaryStmt(UnaryStmt stmt) {
        String varName = stmt.name.id;
        
        // Load current value
        if (variableOffsets.containsKey(varName)) {
            int offset = variableOffsets.get(varName);
            emit("lw $t0, " + offset + "($fp)");
        } else {
            emit("la $t1, _" + varName);
            emit("lw $t0, 0($t1)");
        }
        
        // Increment or decrement
        if (stmt.operator.equals("++")) {
            emit("addi $t0, $t0, 1");
        } else if (stmt.operator.equals("--")) {
            emit("addi $t0, $t0, -1");
        }
        
        // Store back
        if (variableOffsets.containsKey(varName)) {
            int offset = variableOffsets.get(varName);
            emit("sw $t0, " + offset + "($fp)");
        } else {
            emit("la $t1, _" + varName);
            emit("sw $t0, 0($t1)");
        }
    }
    
    private void generateExpr(Expr expr) {
        if (expr == null) return;
        
        if (expr instanceof IntLit) {
            emit("li $t0, " + ((IntLit) expr).integer);
        } else if (expr instanceof FloatLit) {
            // Store float as raw bits
            float val = (float) ((FloatLit) expr).floatingPoint;
            int bits = Float.floatToIntBits(val);
            emit("li $t0, " + bits);
        } else if (expr instanceof BoolLit) {
            boolean val = ((BoolLit) expr).bool.equals("true");
            emit("li $t0, " + (val ? 1 : 0));
        } else if (expr instanceof CharLit) {
            String charStr = ((CharLit) expr).character;
            // Handle escape sequences
            char c = parseCharLiteral(charStr);
            emit("li $t0, " + (int) c);
        } else if (expr instanceof StrLit) {
            String str = ((StrLit) expr).string;
            String label = getStringLabel(str);
            emit("la $t0, " + label);
        } else if (expr instanceof Name) {
            generateNameExpr((Name) expr);
        } else if (expr instanceof NameArray) {
            generateNameArrayExpr((NameArray) expr);
        } else if (expr instanceof BinaryExpr) {
            generateBinaryExpr((BinaryExpr) expr);
        } else if (expr instanceof UnaryExpr) {
            generateUnaryExpr((UnaryExpr) expr);
        } else if (expr instanceof ParenExpr) {
            generateExpr(((ParenExpr) expr).expression);
        } else if (expr instanceof TernaryExpr) {
            generateTernaryExpr((TernaryExpr) expr);
        } else if (expr instanceof VoidFuncExpr) {
            generateVoidFuncExpr((VoidFuncExpr) expr);
        } else if (expr instanceof ArgFuncExpr) {
            generateArgFuncExpr((ArgFuncExpr) expr);
        } else if (expr instanceof TypeCastExpr) {
            generateTypeCastExpr((TypeCastExpr) expr);
        }
    }
    
    private void generateNameExpr(Name name) {
        String varName = name.id;
        
        if (variableOffsets.containsKey(varName)) {
            int offset = variableOffsets.get(varName);
            emit("lw $t0, " + offset + "($fp)");
        } else {
            emit("la $t1, _" + varName);
            emit("lw $t0, 0($t1)");
        }
    }
    
    private void generateNameArrayExpr(NameArray arr) {
        String varName = arr.id;
        
        // Generate index expression
        generateExpr(arr.expression);
        emit("sll $t1, $t0, 2"); // Multiply by 4
        
        if (variableOffsets.containsKey(varName)) {
            int offset = variableOffsets.get(varName);
            emit("addi $t2, $fp, " + offset);
            emit("add $t2, $t2, $t1");
        } else {
            emit("la $t2, _" + varName);
            emit("add $t2, $t2, $t1");
        }
        emit("lw $t0, 0($t2)");
    }
    
    private void generateBinaryExpr(BinaryExpr expr) {
        // Generate left operand
        generateExpr(expr.leftHandSide);
        emit("addi $sp, $sp, -4");
        emit("sw $t0, 0($sp)"); // Save left result
        
        // Generate right operand
        generateExpr(expr.rightHandSide);
        emit("move $t1, $t0"); // Right in $t1
        emit("lw $t0, 0($sp)"); // Left in $t0
        emit("addi $sp, $sp, 4");
        
        String op = expr.operator;
        
        switch (op) {
            case "+":
                emit("add $t0, $t0, $t1");
                break;
            case "-":
                emit("sub $t0, $t0, $t1");
                break;
            case "*":
                emit("mul $t0, $t0, $t1");
                break;
            case "/":
                emit("div $t0, $t1");
                emit("mflo $t0");
                break;
            case "<":
                emit("slt $t0, $t0, $t1");
                break;
            case ">":
                emit("slt $t0, $t1, $t0");
                break;
            case "<=":
                emit("slt $t0, $t1, $t0");
                emit("xori $t0, $t0, 1");
                break;
            case ">=":
                emit("slt $t0, $t0, $t1");
                emit("xori $t0, $t0, 1");
                break;
            case "==":
                emit("seq $t0, $t0, $t1");
                break;
            case "<>":
                emit("sne $t0, $t0, $t1");
                break;
            case "&&":
                emit("and $t0, $t0, $t1");
                emit("sltu $t0, $zero, $t0");
                break;
            case "||":
                emit("or $t0, $t0, $t1");
                emit("sltu $t0, $zero, $t0");
                break;
        }
    }
    
    private void generateUnaryExpr(UnaryExpr expr) {
        generateExpr(expr.expression);
        
        String op = expr.operator;
        
        switch (op) {
            case "-":
                emit("sub $t0, $zero, $t0");
                break;
            case "+":
                // No operation needed
                break;
            case "~":
                emit("seq $t0, $t0, $zero"); // Logical NOT
                break;
        }
    }
    
    private void generateTernaryExpr(TernaryExpr expr) {
        String falseLabel = newLabel("ternary_false");
        String endLabel = newLabel("ternary_end");
        
        generateExpr(expr.condition);
        emit("beq $t0, $zero, " + falseLabel);
        
        generateExpr(expr.whenTrue);
        emit("j " + endLabel);
        
        textSection.append(falseLabel).append(":\n");
        generateExpr(expr.whenFalse);
        
        textSection.append(endLabel).append(":\n");
    }
    
    private void generateVoidFuncExpr(VoidFuncExpr expr) {
        emit("jal " + expr.id);
        emit("move $t0, $v0");
    }
    
    private void generateArgFuncExpr(ArgFuncExpr expr) {
        int argCount = countArgs(expr.arguments);
        generateArgs(expr.arguments, argCount);
        
        emit("jal " + expr.id);
        
        if (argCount > 0) {
            emit("addi $sp, $sp, " + (argCount * 4));
        }
        emit("move $t0, $v0");
    }
    
    private void generateTypeCastExpr(TypeCastExpr expr) {
        generateExpr(expr.expression);
        // For simplicity, most casts are no-ops at MIPS level for int/bool/char
        // Float casts would need more work
    }
    
    private char parseCharLiteral(String s) {
        if (s.length() == 1) return s.charAt(0);
        if (s.startsWith("\\")) {
            if (s.length() > 1) {
                switch (s.charAt(1)) {
                    case 'n': return '\n';
                    case 't': return '\t';
                    case 'r': return '\r';
                    case '\\': return '\\';
                    case '\'': return '\'';
                    case '\"': return '\"';
                    default: return s.charAt(1);
                }
            }
        }
        return s.charAt(0);
    }
    
    private String getStringLabel(String str) {
        if (!stringLiterals.containsKey(str)) {
            String label = "_str" + stringCounter++;
            stringLiterals.put(str, label);
        }
        return stringLiterals.get(str);
    }
    
    private String getExprType(Expr expr) {
        if (expr instanceof IntLit) return "int";
        if (expr instanceof FloatLit) return "float";
        if (expr instanceof BoolLit) return "bool";
        if (expr instanceof CharLit) return "char";
        if (expr instanceof StrLit) return "string";
        if (expr instanceof Name) {
            String varName = ((Name) expr).id;
            return variableTypes.getOrDefault(varName, "int");
        }
        if (expr instanceof BinaryExpr) {
            String op = ((BinaryExpr) expr).operator;
            if (op.equals("<") || op.equals(">") || op.equals("<=") || 
                op.equals(">=") || op.equals("==") || op.equals("<>") ||
                op.equals("&&") || op.equals("||")) {
                return "bool";
            }
            return getExprType(((BinaryExpr) expr).leftHandSide);
        }
        if (expr instanceof ParenExpr) {
            return getExprType(((ParenExpr) expr).expression);
        }
        return "int";
    }
    
    private String newLabel(String prefix) {
        return "_" + prefix + "_" + (labelCounter++);
    }
    
    private void emit(String instruction) {
        textSection.append("\t").append(instruction).append("\n");
    }
}
