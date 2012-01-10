package test;

import java.util.*;
import javax.swing.*;
import java.awt.*;

class CAModel {
   private int rule;
   private int[] cells; //>>> can't be final in beanshell
   private int[] buffer;
   public CAModel(int size) {
      this(150, size);
   }
   public CAModel(int r, int size) {
      rule=r;
      cells=new int[size];
      buffer=new int[size];
   }
   public void setRule(int r) {
      rule=r;
   }
   public int getRule() { return rule; }
   public void setCell(int index, int status) {
      if(index<0 || index>=cells.length) 
         throw new IllegalArgumentException();
      cells[index]=status;
   }
   public int size() { return cells.length; }
   public void step() {
      for(int i=0; i<cells.length; i++) {
         buffer[i]=getNextState(cells[i], cells[i==0?cells.length-1:i-1],
                                         cells[i==cells.length-1?0:i+1]);
      }
      System.arraycopy(buffer, 0, cells, 0, buffer.length);
      //print();
   }
   public int[] getCells() { return cells; } //>>> safe?
   public void clearCells() {
      for(int i=0; i < cells.length; i++) {
         cells[i]=0;
      }
   }

   private int getNextState(int center, int left, int right) {
      final int index=left*4+center*2+right;
      return (rule>>index)%2;
   }
   public void print() {
      System.out.println(Arrays.toString(cells));
   }

}
public class CA extends JComponent {
   private CAModel model; //>>> can't be final in beanshell
   private int[][] history;
   private int historyIndex=0;
   private int cellWidth=5, cellHeight=5;
   
   
   public CA(int rule, int width, int historySize) {
      this(rule, width, historySize, 5, 5);
   }
   public CA(int rule, int width, int historySize, int cw, int ch) {
      this(new CAModel(rule, width), historySize, cw, ch);
   }
   public CA(CAModel ca, int historySize, int cw, int ch) {
      if(historySize<1) throw new IllegalArgumentException();
      this.model=ca;
      history=new int[historySize][ca.size()];
      //for(int i=0; i<history.length; i++) {
      //   history[i]=new int[ca.getSize()];
      //}
      cellWidth=cw; cellHeight=ch;
      setPreferredSize(new Dimension(cellWidth*ca.size(), 
               cellHeight*historySize));
   }
   //[ forward methods
   public void step() {
      model.step();
      final int[] entry=history[historyIndex];
      historyIndex=(historyIndex+1)%history.length;
      System.arraycopy(model.getCells(), 0, entry, 0, entry.length);
      repaint();
   }
   public void setRule(int rule) {
      model.setRule(rule);
   }
   public void setCell(int index, int status) {
      model.setCell(index, status);
      repaint();
   }
   public void clearCells() {
      model.clearCells();
   }
   public int getRule() {
      return model.getRule();
   }
   public int getCell(int index) {
      return model.getCells()[index];
   }
   public int getLength() {
      return model.size();
   }

   public void paintComponent(Graphics g) {
      g.setColor(Color.white);
      g.fillRect(0, 0, getWidth(), getHeight());
      g.setColor(Color.black);
      for(int i=0; i<history.length; i++) {
         for(int j=0; j<history[i].length; j++) {
            if(history[i][j]>0) {
               g.fillRect(j*cellWidth, i*cellHeight, 
                     cellWidth, cellHeight);
            }
         }
      }
      g.setColor(Color.white);
      g.setXORMode(Color.red);
      g.fillRect(0, cellHeight*historyIndex, getWidth(), cellHeight);
      g.setPaintMode();
   }
   public void display() {
      final JFrame frame=new JFrame("Cellular Automata");
      frame.add(this);
      frame.pack();
      frame.setVisible(true);
   }

   public static void main(String[] args) {
      final CAModel ca=new CAModel(256);
      ca.setRule(30);
      ca.setCell(ca.size()/2, 1);

      final CA view=new CA(ca, 500, 2, 2);
      view.display();

      //ca.print();
      for(int i=0; i<200; i++) {
         //ca.step();
         //ca.print();
         //view.repaint();
         view.step();
      }
   }
}

