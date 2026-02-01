import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

public class Optimizer {
    
    private Map<String, Expr> constantValues = new HashMap<>();
    private Set<String> modifiedVariables = new HashSet<>();
    private boolean changed = false;
    
    public Program optimize(Program program) {
        // Run optimization passes until no more changes
        do {
            changed = false;
            constantValues.clear();
            modifiedVariables.clear();
            program.memberDeclarations = optimizeMemberDecls(program.memberDeclarations);
        } while (changed);
        
        return program;
    }
    
    private MemberDecls optimizeMemberDecls(MemberDecls decls) {
        if (decls == null) return null;
        decls.fieldsAndMethods = optimizeFieldsNMethods(decls.fieldsAndMethods);
        return decls;
    }
    
    private FieldsNMethods optimizeFieldsNMethods(FieldsNMethods fnm) {
        if (fnm == null) return null;
        
        if (fnm.fieldDeclaration != null) {
            fnm.fieldDeclaration = optimizeFieldDecl(fnm.fieldDeclaration);
        }
        if (fnm.methodDeclaration != null) {
            fnm.methodDeclaration = optimizeMethodDecl(fnm.methodDeclaration);
        }
        fnm.fieldsAndMethods = optimizeFieldsNMethods(fnm.fieldsAndMethods);
        return fnm;
    }
    
    private FieldDecl optimizeFieldDecl(FieldDecl decl) {
        if (decl instanceof FieldDeclSingle) {
            FieldDeclSingle single = (FieldDeclSingle) decl;
            if (single.expression != null && single.expression.expression != null) {
                single.expression.expression = optimizeExpr(single.expression.expression);
                
                // Track constant for propagation if it's a final or simple constant init
                if (single.isFinal && isConstant(single.expression.expression)) {
                    constantValues.put(single.fieldStart.id, single.expression.expression);
                }
            }
        }
        return decl;
    }
    
    private MethodDecl optimizeMethodDecl(MethodDecl method) {
        // Clear method-local constant tracking
        Map<String, Expr> savedConstants = new HashMap<>(constantValues);
        
        if (method.fieldDeclarations != null) {
            method.fieldDeclarations = optimizeFieldDecls(method.fieldDeclarations);
        }
        if (method.statements != null) {
            method.statements = optimizeStmts(method.statements);
        }
        
        // Restore only global constants
        constantValues = savedConstants;
        return method;
    }
    
    private FieldDecls optimizeFieldDecls(FieldDecls decls) {
        if (decls == null) return null;
        if (decls.fieldDeclaration != null) {
            decls.fieldDeclaration = optimizeFieldDecl(decls.fieldDeclaration);
        }
        decls.fieldDeclarations = optimizeFieldDecls(decls.fieldDeclarations);
        return decls;
    }
    
    private Stmts optimizeStmts(Stmts stmts) {
        if (stmts == null) return null;
        
        if (stmts.statement != null) {
            stmts.statement = optimizeStmt(stmts.statement);
            
            // If statement was optimized away (e.g., if(false)), skip to next statements
            if (stmts.statement == null) {
                changed = true;
                return optimizeStmts(stmts.statements);
            }
            
            // Dead code elimination: remove statements after return
            if (stmts.statement instanceof ValueReturn || stmts.statement instanceof VoidReturn) {
                if (stmts.statements != null) {
                    changed = true;
                    stmts.statements = null; // Remove dead code after return
                }
            }
        }
        
        if (stmts.statements != null) {
            stmts.statements = optimizeStmts(stmts.statements);
        }
        
        return stmts;
    }
    
    private Stmt optimizeStmt(Stmt stmt) {
        if (stmt == null) return null;
        
        if (stmt instanceof AssmntStmt) {
            return optimizeAssignment((AssmntStmt) stmt);
        } else if (stmt instanceof IfStmt) {
            return optimizeIf((IfStmt) stmt);
        } else if (stmt instanceof WhileStmt) {
            return optimizeWhile((WhileStmt) stmt);
        } else if (stmt instanceof ValueReturn) {
            return optimizeValueReturn((ValueReturn) stmt);
        } else if (stmt instanceof PrintStmt) {
            return optimizePrint((PrintStmt) stmt);
        } else if (stmt instanceof ScopeStmt) {
            return optimizeScope((ScopeStmt) stmt);
        }
        
        return stmt;
    }
    
    private Stmt optimizeAssignment(AssmntStmt stmt) {
        stmt.expression = optimizeExpr(stmt.expression);
        
        // Track constant for propagation
        if (isConstant(stmt.expression)) {
            constantValues.put(stmt.name.id, stmt.expression);
        } else {
            // Variable is modified with non-constant, invalidate
            constantValues.remove(stmt.name.id);
            modifiedVariables.add(stmt.name.id);
        }
        
        return stmt;
    }
    
    private Stmt optimizeIf(IfStmt stmt) {
        stmt.ifBase.expression = optimizeExpr(stmt.ifBase.expression);
        
        // Dead branch elimination: if condition is constant
        if (stmt.ifBase.expression instanceof BoolLit) {
            BoolLit cond = (BoolLit) stmt.ifBase.expression;
            boolean value = cond.bool.equals("true");
            changed = true;
            
            if (value) {
                // Condition is always true, return only the then branch
                if (stmt.state1 != null) {
                    stmt.state1 = optimizeStmt(stmt.state1);
                }
                return stmt.state1;
            } else {
                // Condition is always false, return else branch or null
                if (stmt.state2 != null) {
                    stmt.state2 = optimizeStmt(stmt.state2);
                    return stmt.state2;
                }
                return null;
            }
        }
        
        // Check for integer constant condition (0 = false, non-zero = true)
        if (stmt.ifBase.expression instanceof IntLit) {
            IntLit cond = (IntLit) stmt.ifBase.expression;
            boolean value = cond.integer != 0;
            changed = true;
            
            if (value) {
                if (stmt.state1 != null) {
                    stmt.state1 = optimizeStmt(stmt.state1);
                }
                return stmt.state1;
            } else {
                if (stmt.state2 != null) {
                    stmt.state2 = optimizeStmt(stmt.state2);
                    return stmt.state2;
                }
                return null;
            }
        }
        
        // Optimize branches
        if (stmt.state1 != null) {
            stmt.state1 = optimizeStmt(stmt.state1);
        }
        if (stmt.state2 != null) {
            stmt.state2 = optimizeStmt(stmt.state2);
        }
        
        return stmt;
    }
    
    private Stmt optimizeWhile(WhileStmt stmt) {
        stmt.whileBase.expression = optimizeExpr(stmt.whileBase.expression);
        
        // Dead code elimination: while(false) is never executed
        if (stmt.whileBase.expression instanceof BoolLit) {
            BoolLit cond = (BoolLit) stmt.whileBase.expression;
            if (cond.bool.equals("false")) {
                changed = true;
                return null; // Remove entire while loop
            }
        }
        
        if (stmt.whileBase.expression instanceof IntLit) {
            IntLit cond = (IntLit) stmt.whileBase.expression;
            if (cond.integer == 0) {
                changed = true;
                return null; // Remove entire while loop
            }
        }
        
        // Invalidate constants modified in loop body
        // (conservative: don't propagate into loops)
        if (stmt.body != null) {
            stmt.body = (ScopeStmt) optimizeScope(stmt.body);
        }
        
        return stmt;
    }
    
    private Stmt optimizeValueReturn(ValueReturn stmt) {
        stmt.expression = optimizeExpr(stmt.expression);
        return stmt;
    }
    
    private Stmt optimizePrint(PrintStmt stmt) {
        if (stmt.printList != null) {
            stmt.printList = optimizePrintList(stmt.printList);
        }
        return stmt;
    }
    
    private PrintList optimizePrintList(PrintList list) {
        if (list == null) return null;
        if (list.expression != null) {
            list.expression = optimizeExpr(list.expression);
        }
        if (list.printList != null) {
            list.printList = optimizePrintList(list.printList);
        }
        return list;
    }
    
    private Stmt optimizeScope(ScopeStmt stmt) {
        if (stmt.fieldDeclarations != null) {
            stmt.fieldDeclarations = optimizeFieldDecls(stmt.fieldDeclarations);
        }
        if (stmt.statements != null) {
            stmt.statements = optimizeStmts(stmt.statements);
        }
        return stmt;
    }
    
    private Expr optimizeExpr(Expr expr) {
        if (expr == null) return null;
        
        // First, recursively optimize sub-expressions
        if (expr instanceof BinaryExpr) {
            return optimizeBinaryExpr((BinaryExpr) expr);
        } else if (expr instanceof UnaryExpr) {
            return optimizeUnaryExpr((UnaryExpr) expr);
        } else if (expr instanceof ParenExpr) {
            ParenExpr paren = (ParenExpr) expr;
            paren.expression = optimizeExpr(paren.expression);
            // Remove unnecessary parentheses around literals
            if (isConstant(paren.expression)) {
                changed = true;
                return paren.expression;
            }
            return paren;
        } else if (expr instanceof TernaryExpr) {
            return optimizeTernaryExpr((TernaryExpr) expr);
        } else if (expr instanceof Name) {
            return optimizeName((Name) expr);
        }
        
        return expr;
    }
    
    private Expr optimizeName(Name name) {
        // Constant propagation: replace variable with known constant value
        if (constantValues.containsKey(name.id)) {
            changed = true;
            Expr constVal = constantValues.get(name.id);
            // Return a copy to avoid aliasing issues
            return copyConstant(constVal);
        }
        return name;
    }
    
    private Expr copyConstant(Expr expr) {
        if (expr instanceof IntLit) {
            return new IntLit(((IntLit) expr).integer);
        } else if (expr instanceof FloatLit) {
            return new FloatLit(((FloatLit) expr).floatingPoint);
        } else if (expr instanceof BoolLit) {
            return new BoolLit(((BoolLit) expr).bool);
        } else if (expr instanceof CharLit) {
            return new CharLit(((CharLit) expr).character);
        }
        return expr;
    }
    
    private Expr optimizeBinaryExpr(BinaryExpr expr) {
        expr.leftHandSide = optimizeExpr(expr.leftHandSide);
        expr.rightHandSide = optimizeExpr(expr.rightHandSide);
        
        // Constant folding
        Expr folded = foldBinaryExpr(expr);
        if (folded != expr) {
            changed = true;
            return folded;
        }
        
        // Algebraic simplifications
        Expr simplified = simplifyBinaryExpr(expr);
        if (simplified != expr) {
            changed = true;
            return simplified;
        }
        
        return expr;
    }
    
    private Expr foldBinaryExpr(BinaryExpr expr) {
        Expr left = expr.leftHandSide;
        Expr right = expr.rightHandSide;
        String op = expr.operator;
        
        // Integer constant folding
        if (left instanceof IntLit && right instanceof IntLit) {
            int l = ((IntLit) left).integer;
            int r = ((IntLit) right).integer;
            
            switch (op) {
                case "+": return new IntLit(l + r);
                case "-": return new IntLit(l - r);
                case "*": return new IntLit(l * r);
                case "/": if (r != 0) return new IntLit(l / r); break;
                case "<": return new BoolLit(l < r ? "true" : "false");
                case ">": return new BoolLit(l > r ? "true" : "false");
                case "<=": return new BoolLit(l <= r ? "true" : "false");
                case ">=": return new BoolLit(l >= r ? "true" : "false");
                case "==": return new BoolLit(l == r ? "true" : "false");
                case "<>": return new BoolLit(l != r ? "true" : "false");
            }
        }
        
        // Float constant folding
        if ((left instanceof FloatLit || left instanceof IntLit) &&
            (right instanceof FloatLit || right instanceof IntLit)) {
            
            double l = getNumericValue(left);
            double r = getNumericValue(right);
            
            // Only fold if at least one is float
            if (left instanceof FloatLit || right instanceof FloatLit) {
                switch (op) {
                    case "+": return new FloatLit(l + r);
                    case "-": return new FloatLit(l - r);
                    case "*": return new FloatLit(l * r);
                    case "/": if (r != 0) return new FloatLit(l / r); break;
                    case "<": return new BoolLit(l < r ? "true" : "false");
                    case ">": return new BoolLit(l > r ? "true" : "false");
                    case "<=": return new BoolLit(l <= r ? "true" : "false");
                    case ">=": return new BoolLit(l >= r ? "true" : "false");
                    case "==": return new BoolLit(l == r ? "true" : "false");
                    case "<>": return new BoolLit(l != r ? "true" : "false");
                }
            }
        }
        
        // Boolean constant folding
        if (left instanceof BoolLit && right instanceof BoolLit) {
            boolean l = ((BoolLit) left).bool.equals("true");
            boolean r = ((BoolLit) right).bool.equals("true");
            
            switch (op) {
                case "&&": return new BoolLit(l && r ? "true" : "false");
                case "||": return new BoolLit(l || r ? "true" : "false");
                case "==": return new BoolLit(l == r ? "true" : "false");
                case "<>": return new BoolLit(l != r ? "true" : "false");
            }
        }
        
        return expr;
    }
    
    private double getNumericValue(Expr expr) {
        if (expr instanceof IntLit) return ((IntLit) expr).integer;
        if (expr instanceof FloatLit) return ((FloatLit) expr).floatingPoint;
        return 0;
    }
    
    private Expr simplifyBinaryExpr(BinaryExpr expr) {
        Expr left = expr.leftHandSide;
        Expr right = expr.rightHandSide;
        String op = expr.operator;
        
        // x + 0 = x, 0 + x = x
        if (op.equals("+")) {
            if (isZero(right)) return left;
            if (isZero(left)) return right;
        }
        
        // x - 0 = x
        if (op.equals("-")) {
            if (isZero(right)) return left;
            // 0 - x is not simplified to avoid creating unary minus here
        }
        
        // x * 1 = x, 1 * x = x
        if (op.equals("*")) {
            if (isOne(right)) return left;
            if (isOne(left)) return right;
            // x * 0 = 0, 0 * x = 0
            if (isZero(right)) return new IntLit(0);
            if (isZero(left)) return new IntLit(0);
        }
        
        // x / 1 = x
        if (op.equals("/")) {
            if (isOne(right)) return left;
        }
        
        // x && true = x, true && x = x
        if (op.equals("&&")) {
            if (isTrue(right)) return left;
            if (isTrue(left)) return right;
            // x && false = false, false && x = false
            if (isFalse(right)) return new BoolLit("false");
            if (isFalse(left)) return new BoolLit("false");
        }
        
        // x || false = x, false || x = x
        if (op.equals("||")) {
            if (isFalse(right)) return left;
            if (isFalse(left)) return right;
            // x || true = true, true || x = true
            if (isTrue(right)) return new BoolLit("true");
            if (isTrue(left)) return new BoolLit("true");
        }
        
        return expr;
    }
    
    private Expr optimizeUnaryExpr(UnaryExpr expr) {
        expr.expression = optimizeExpr(expr.expression);
        
        // Constant folding for unary operators
        String op = expr.operator;
        Expr inner = expr.expression;
        
        if (op.equals("-")) {
            if (inner instanceof IntLit) {
                changed = true;
                return new IntLit(-((IntLit) inner).integer);
            }
            if (inner instanceof FloatLit) {
                changed = true;
                return new FloatLit(-((FloatLit) inner).floatingPoint);
            }
            // Double negation: --x = x
            if (inner instanceof UnaryExpr && ((UnaryExpr) inner).operator.equals("-")) {
                changed = true;
                return ((UnaryExpr) inner).expression;
            }
        }
        
        if (op.equals("+")) {
            // +x = x for numeric types
            if (inner instanceof IntLit || inner instanceof FloatLit) {
                changed = true;
                return inner;
            }
        }
        
        if (op.equals("~")) {
            if (inner instanceof BoolLit) {
                boolean val = ((BoolLit) inner).bool.equals("true");
                changed = true;
                return new BoolLit(!val ? "true" : "false");
            }
            // Double negation: ~~x = x
            if (inner instanceof UnaryExpr && ((UnaryExpr) inner).operator.equals("~")) {
                changed = true;
                return ((UnaryExpr) inner).expression;
            }
        }
        
        return expr;
    }
    
    private Expr optimizeTernaryExpr(TernaryExpr expr) {
        expr.condition = optimizeExpr(expr.condition);
        expr.whenTrue = optimizeExpr(expr.whenTrue);
        expr.whenFalse = optimizeExpr(expr.whenFalse);
        
        // Constant condition folding
        if (expr.condition instanceof BoolLit) {
            boolean cond = ((BoolLit) expr.condition).bool.equals("true");
            changed = true;
            return cond ? expr.whenTrue : expr.whenFalse;
        }
        
        if (expr.condition instanceof IntLit) {
            boolean cond = ((IntLit) expr.condition).integer != 0;
            changed = true;
            return cond ? expr.whenTrue : expr.whenFalse;
        }
        
        return expr;
    }
    
    private boolean isConstant(Expr expr) {
        return expr instanceof IntLit || 
               expr instanceof FloatLit || 
               expr instanceof BoolLit || 
               expr instanceof CharLit ||
               expr instanceof StrLit;
    }
    
    private boolean isZero(Expr expr) {
        if (expr instanceof IntLit) return ((IntLit) expr).integer == 0;
        if (expr instanceof FloatLit) return ((FloatLit) expr).floatingPoint == 0.0;
        return false;
    }
    
    private boolean isOne(Expr expr) {
        if (expr instanceof IntLit) return ((IntLit) expr).integer == 1;
        if (expr instanceof FloatLit) return ((FloatLit) expr).floatingPoint == 1.0;
        return false;
    }
    
    private boolean isTrue(Expr expr) {
        if (expr instanceof BoolLit) return ((BoolLit) expr).bool.equals("true");
        return false;
    }
    
    private boolean isFalse(Expr expr) {
        if (expr instanceof BoolLit) return ((BoolLit) expr).bool.equals("false");
        return false;
    }
}
