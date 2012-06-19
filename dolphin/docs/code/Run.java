import javax.script.*;
import java.io.*;

class Run {
  public static void printUsage() {
    System.out.println("Usage: java <script file> <language>");
  }
  public static void main(String[] args) {
    if(args.length < 2) {
      printUsage();
      return;
    }
    final String scriptFile=args[0];
    final String language=args[1];
    FileReader reader;
    try {
      reader=new FileReader(scriptFile);
    } catch(FileNotFoundException e) {
      System.out.println("File not found: "+scriptFile);
      return;
    }
    final ScriptEngine engine=
      new ScriptEngineManager().getEngineByName(language);
    try {
      engine.eval(reader);
    } catch(ScriptException e) {
      System.out.println("Error occurred while running \""+scriptFile+"\": "+e);
    }
  }
}
