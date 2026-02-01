import java.util.ArrayList;
import java.util.HashSet;

class SymbolTable {
  ArrayList<ArrayList<Pair<String,String>>> table;
  static HashSet<String> finalVariables = new HashSet<String>();

  public SymbolTable() {
    table = new ArrayList<ArrayList<Pair<String,String>>>();
    table.add(new ArrayList<Pair<String,String>>());
  }

  public void StartScope(){
    table.add(new ArrayList<Pair<String,String>>());
  }

  public void EndScope(){
    table.remove(table.size()-1);
  }

  public void addVar(String id, String t) throws LangException{
    for (Pair<String,String> p : table.get(table.size()-1)){
      if(p.getKey().equals(id)){
        throw new LangException("Error: variable already declared " + id);
      }
    }
    table.get(table.size()-1).add(new Pair<String,String>(id,t));
    return;
  }
  
  public void addFinalVar(String id, String t) throws LangException{
    addVar(id, t);
    finalVariables.add(id);
  }
  
  public boolean isFinal(String id) {
    return finalVariables.contains(id);
  }

  public void addRoutine(String id, InOutList params) throws LangException{
    String pType = params.getType();

    for(Pair<String,String> p: table.get(table.size()-1)){
      if(p.getKey().equals(id)){
        throw new LangException("Error: tried to redeclare a routine " + id);
      }
    }
    table.get(table.size()-1).add(new Pair<String,String>(id,pType));
    return;
  }

  public String get(String s) throws LangException {
    for(int i = table.size()-1; i >= 0; --i){
      for(Pair<String,String> p: table.get(i)){
        if(p.getKey().equals(s)){
          return p.getValue();
        }
      }
    }
    throw new LangException("Error: variable not declared " + s);
  }
}
