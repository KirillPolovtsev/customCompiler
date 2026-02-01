/**
 * General interface for nodes in the abstract syntax tree. Contains   
 * only the method toString which is already inherited from Object,   
 * so the interface doesn't add any functionality. It only provides   
 * a common super type for all elements in the AST.                   
 */
interface TI {
  public String toString(int t);   // already inherited from Object
  
}
