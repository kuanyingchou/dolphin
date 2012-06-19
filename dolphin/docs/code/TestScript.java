import javax.script.*;
import javax.swing.JFrame;

class TestScript {
    public static final ScriptEngine js=
      new ScriptEngineManager().getEngineByName("JavaScript");

    public static void test_hello() throws javax.script.ScriptException {
        js.eval("print('hello, js!');");
    }
    public static void test_put() throws javax.script.ScriptException {
        final java.util.List<String> names=new java.util.ArrayList<String>();
        names.add("Alice");
        names.add("Bob");
        js.put("n", names);
        js.eval("println(n);");
        js.eval("n.add('Cindy');");
        System.out.println(names);
    
    }
    public static void test_import() throws javax.script.ScriptException {
        js.eval("importClass(javax.swing.JFrame)");
        js.eval("var jf=new JFrame('hi!');");
        js.eval("jf.setSize(100, 100);");
        js.eval("jf.setVisible(true);");
        js.eval("importPackage(javax.sound)");
    }
    public static void main(String[] args) throws javax.script.ScriptException {
        //test_hello();
        test_put();
        //test_import();
    }
}
