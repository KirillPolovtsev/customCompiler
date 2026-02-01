import java.util.*;

class LangException extends Exception
{
    String error;
    String sourceLine;
    String scope;
    
    public LangException(String s)
    {
        error = s;
        this.scope = ScopeContext.getCurrentScope();
        this.sourceLine = "";
    }
    
    public LangException(String s, String line)
    {
        error = s;
        this.scope = ScopeContext.getCurrentScope();
        this.sourceLine = line;
    }

    public String toString()
    {
        if (sourceLine.isEmpty()) {
            return scope + ":" + error;
        } else {
            return scope + ":" + error + " line: " + sourceLine;
        }
    }
}

