class IfStmt extends Stmt implements TI {
  IfBase ifBase;
  Stmt state1;
  Stmt state2;
  
  public IfStmt(IfBase i, ScopeStmt s1)
  {
    ifBase = i;
    state1 = s1;
    state2 = null;
  }

  public IfStmt(IfBase i, ScopeStmt s1, ScopeStmt s2)
  {
    ifBase = i;
    state1 = s1;
    state2 = s2;
  }
  
  public String typeCheck() throws LangException {
    ifBase.typeCheck();
    state1.typeCheck();
    if(state2 != null) {
      state2.typeCheck();
    }
    return "";
  }

  public String toString(int t)
  {
    if (state2 == null)
      return ( T(t) + ifBase.toString(t) + state1.toString(t));
    else
        return ( T(t) + ifBase.toString(t) + state1.toString(t) + T(t) + "else\n"
                 + state2.toString(t));
  }
}

