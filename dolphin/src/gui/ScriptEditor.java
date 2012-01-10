package gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.MidiDevice.Info;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;

import api.util.FlowPane;
import api.util.Util;


public class ScriptEditor extends JPanel {
   public final JTextArea console=new JTextArea();
   public final JTextArea input=new JTextArea();
   
   final ScriptEngineManager manager = new ScriptEngineManager();
   ScriptEngine engine;// = manager.getEngineByName("beanshell"); //: default
   final static String userName=System.getProperty("user.name");
   private SwingWorker currentWorker=null;
   
   public ScriptEditor() {
      
      //[ setup gui
      setLayout(new BorderLayout());
      
      final List<ScriptEngineFactory> factories=manager.getEngineFactories();
      if(factories.isEmpty()) throw new RuntimeException(); //>>>
      //setEngineByFactory(factories.get(0));
      setEngine(manager.getEngineByName("js")); //>>> for demo
      
      final JComboBox factoryCombo=new JComboBox(factories.toArray());
      factoryCombo.setRenderer(new FactoryCell());
      factoryCombo.setSelectedItem(engine.getFactory());
      factoryCombo.addItemListener(new ItemListener() {
         public void itemStateChanged(ItemEvent e) {
            if(e.getStateChange()!=ItemEvent.SELECTED) return;
            setEngineByFactory(
                  (ScriptEngineFactory)factoryCombo.getSelectedItem());
            console.append("I speak in "+engine.getFactory().getLanguageName()+" now.");
            console.append("\n");
         }
      });
      
      if(engine==null) throw new IllegalArgumentException();
      final JButton runButton=new JButton("Run");
      runButton.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            execute(input.getText());
         }
      });
      
      console.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
      console.append("Hello! "+userName+"\n");
      console.setTabSize(3);
      console.setEditable(false);
      //console.setLineWrap(true);
      //console.setWrapStyleWord(true);
      
      input.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
      input.setTabSize(3);
      input.addKeyListener(new KeyAdapter() {
         @Override
         public void keyPressed(KeyEvent e) {
            if(e.isControlDown() && e.getKeyCode()==KeyEvent.VK_ENTER) {
               execute(input.getText());
            } else if(e.isControlDown() && e.getKeyCode()==KeyEvent.VK_UP) {
               if(!log.isEmpty()) {
                  if(logIndex>0) logIndex--;
                  final String previous=log.get(logIndex);
//                  if(logIndex==log.size()-1) {
//                     final String current=input.getText();
//                     addLog(current);
//                  }
                  input.setText(previous);
                  
               }
            } else if(e.isControlDown() && e.getKeyCode()==KeyEvent.VK_DOWN) {
               if(logIndex<log.size()) logIndex++;
               if(logIndex==log.size()) input.setText("");
               else {
                  final String next=log.get(logIndex);
                  input.setText(next);
               }
            }
         }
      });
      
      final JScrollPane consolePane=new JScrollPane(console);
      final JScrollPane inputPane=new JScrollPane(input);
      consolePane.setPreferredSize(new Dimension(320, 256));
      inputPane.setPreferredSize(new Dimension(320, 128));
      final JSplitPane splitPane=new JSplitPane(JSplitPane.VERTICAL_SPLIT, 
            consolePane, 
            inputPane);
      splitPane.setResizeWeight(1.0);
      add(factoryCombo, BorderLayout.NORTH);
      add(splitPane, BorderLayout.CENTER);
      //add(new JScrollPane(input), BorderLayout.CENTER);
      add(new FlowPane(
            runButton, 
            new JButton(new ExecuteFileAction()),
            new JButton(new StopAction())), 
               BorderLayout.SOUTH);
      
//      pack();
//      setVisible(true);
   }
   
//   public void setEngineByName(String name) {
//      engine=manager.getEngineByName(name);
//      engine.put("sys", this);
//   }
   public void setEngine(ScriptEngine e) {
    //Bindings globalBindings=null;
      //Bindings engineBindings=null;
      ScriptContext context=null;
      if(engine!=null) {
         context=engine.getContext();
         //globalBindings=engine.getBindings(ScriptContext.GLOBAL_SCOPE);
         //engineBindings=engine.getBindings(ScriptContext.ENGINE_SCOPE);
      }
      //>>> other bindings?
      engine=e;
      if(context!=null) {
         engine.setContext(context);
      }
      engine.getContext().setWriter(new DocumentWriter(console.getDocument()));
      engine.getContext().setErrorWriter(new DocumentWriter(console.getDocument()));
      
//      if(globalBindings!=null) {
//         engine.setBindings(globalBindings, ScriptContext.GLOBAL_SCOPE);
//      }
//      if(engineBindings!=null) {
//         engine.setBindings(engineBindings, ScriptContext.ENGINE_SCOPE);
//      }
   }
   public void setEngineByFactory(ScriptEngineFactory f) {
      setEngine(f.getScriptEngine());
   }
   
   public void addBinding(String key, Object value) {
      engine.put(key, value);
   }
   
   public List<String> log=new LinkedList<String>();
   private final int logSizeLimit=100;
   private int logIndex=0;
   public void addLog(String s) {
      log.add(s);
      logIndex=log.size();
   }
   public void execute(final String script) {
      execute(script, false);
   }
   public void execute(final String script, final boolean hidden) {
      if(!hidden) {
         console.append(">");
         console.append(script);
         console.append("\n");
         input.setText("");
         addLog(script);
         if(log.size()>logSizeLimit) log.remove(0); //>>>
      }
      currentWorker=new SwingWorker<String, Object>() { //>>> why String, Object?
         @Override
         public String doInBackground() {
            input.setEnabled(false);
            try {
               engine.eval(script);
            } catch(ScriptException e1) {
               e1.printStackTrace();
               console.append("Oops! Error Occurred.\n");
               console.append(e1.getMessage());
               console.append("\n");
            }
            return "";
         }

         @Override
         protected void done() {
            console.setCaretPosition(console.getDocument().getLength());
            input.setEnabled(true);
            input.requestFocusInWindow();
         }
      };
      currentWorker.execute();

//      try {
//         engine.eval(input.getText());
//      } catch(ScriptException e1) {
//         console.append("Oops! Error Occurred.\n");
//         console.append(e1.getMessage());
//         console.append("\n");
//         return;
//      }
        
//      console.validate();
//      console.revalidate();
   }
   public void executeFromFile(final File f) {
      console.append("Load \""+f.getPath()+"\"");
      console.append("\n");
      currentWorker=new SwingWorker<String, Object>() { //>>> why String, Object?
         @Override
         public String doInBackground() {
            input.setEnabled(false);
            try {
               engine.eval(new FileReader(f));
            } catch(FileNotFoundException e) {
               e.printStackTrace();               
            } catch(ScriptException e1) {
               e1.printStackTrace();
               console.append("Oops! Error Occurred.\n");
               console.append(e1.getMessage());
               console.append("\n");
            }
            return "";
         }

         @Override
         protected void done() {
            console.setCaretPosition(console.getDocument().getLength());
            input.setEnabled(true);
            input.requestFocusInWindow();
         }
      };
      currentWorker.execute();
      
//      console.validate();
//      console.revalidate();
   }
   
   private static class DocumentWriter extends Writer {
      final Document doc;
      public DocumentWriter(Document d) {
         doc=d;
      }
      @Override
      public void close() throws IOException {
         
      }

      @Override
      public void flush() throws IOException {
         
      }

      @Override
      public void write(char[] cbuf, int off, int len) throws IOException {
         try {
            doc.insertString(doc.getLength(), new String(cbuf, off, len), null);
         } catch(BadLocationException e) {
            e.printStackTrace();
         }
      }
      
      public void print(String s) { //>>> for javascript
         try {
            doc.insertString(doc.getLength(), s, null);
         } catch(BadLocationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
      }
   }
   private class ExecuteFileAction extends AbstractAction {
      public ExecuteFileAction() {
         putValue(Action.NAME, "Load File...");
      }
      @Override
      public void actionPerformed(ActionEvent e) {
         final JFileChooser jfc=new JFileChooser(Util.curDir);
         jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
         //jfc.setFileFilter(
         //      new FileNameExtensionFilter("midi files", "mid"));
         final int ret=jfc.showOpenDialog(ScriptEditor.this);
         if(ret==JFileChooser.APPROVE_OPTION) {
            final File f=jfc.getSelectedFile();
            if(f==null) return;
            executeFromFile(f);
         }
      }
      
   }
   private class StopAction extends AbstractAction {
      public StopAction() {
         putValue(Action.NAME, "Stop");
      }
      @Override
      public void actionPerformed(ActionEvent e) {
         if(currentWorker!=null) {
            currentWorker.cancel(true);
         }
      }
   }
   private static class FactoryCell extends JLabel implements ListCellRenderer {
      //private ImageIcon partIcon=new ImageIcon("part.png");
      public Component getListCellRendererComponent(JList list, // the list
            Object value, // value to display
            int index, // cell index
            boolean isSelected, // is the cell selected
            boolean cellHasFocus) // does the cell have focus
      {
         //String s=value.toString();
         final ScriptEngineFactory f=(ScriptEngineFactory)value;
         setText(f.getLanguageName()+" "+f.getLanguageVersion()+
               " - "+f.getEngineName());

         if(isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
         } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
         }
         setEnabled(list.isEnabled());
         setFont(list.getFont());
         setOpaque(true);
         return this;
      }
   }
   
   public static void main(String[] args) throws ScriptException {
      SwingUtilities.invokeLater(new Runnable() {
         public void run() {
            final ScriptEditor editor=new ScriptEditor();
            editor.addBinding("editor", editor);
            
            final JFrame jf=new JFrame("Script Editor");
            jf.add(editor);
            jf.pack();
            jf.setVisible(true);
         }
      });
   }
   

}
