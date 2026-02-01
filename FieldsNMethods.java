class FieldsNMethods extends Token implements TI {
  FieldDecl fieldDeclaration;
  FieldsNMethods fieldsAndMethods;
  MethodDecl methodDeclaration;
  MethodDecls methodDeclarations;

  public FieldsNMethods(FieldDecl f, FieldsNMethods s) {
    fieldDeclaration = f;
    fieldsAndMethods = s;
    methodDeclaration = null;
    methodDeclarations = null;
  }

  public FieldsNMethods(MethodDecl m, MethodDecls s) {
    fieldDeclaration = null;
    fieldsAndMethods = null;
    methodDeclaration = m;
    methodDeclarations = s;
  }

  public String typeCheck() throws LangException {
    if(fieldDeclaration != null) {
      fieldDeclaration.typeCheck();
      if(fieldsAndMethods != null) {
        return fieldsAndMethods.typeCheck();
      }
      return "";
    }

    if(methodDeclaration != null){
      methodDeclaration.typeCheck();
      if(methodDeclarations != null){
        return methodDeclarations.typeCheck();
      }
      return "";
    }
    return "";
  }


  public String toString(int t) {
    if (methodDeclaration != null)
      return (methodDeclaration.toString(t) + (methodDeclarations != null ? methodDeclarations.toString(t) : ""));
    return (fieldDeclaration.toString(t) + (fieldsAndMethods != null ? fieldsAndMethods.toString(t) : ""));
  }
}
