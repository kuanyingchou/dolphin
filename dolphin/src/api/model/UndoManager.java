package api.model;

import java.util.ArrayList;

public interface UndoManager {
   public void logChange(ScoreChange sc);
   public boolean canUndo();
   public boolean canRedo();
   public ScoreChange undo();
   public ScoreChange redo();
}

class ListUndoManager implements UndoManager {
   private java.util.List<ScoreChange> changeLog
      =new ArrayList<ScoreChange>(); //>>> limit size
   private int changeLogIndex=0;
   //private boolean comboMode=false;
   //private final java.util.List<ScoreChange> combo=new ArrayList<ScoreChange>();
   public void logChange(ScoreChange e) {
      if(changeLogIndex<changeLog.size()) {
         for(int i=changeLog.size()-1; i>=changeLogIndex; i--) {
            changeLog.remove(i);
         }
      }
      changeLog.add(e);
      changeLogIndex++;
   }
   
   public boolean canUndo() {
      return changeLogIndex>0;
   }
   public boolean canRedo() {
      return changeLogIndex<changeLog.size();
   }
   public ScoreChange undo() {
      if(!canUndo()) throw new RuntimeException("nothing to undo"); //: double check
      final ScoreChange change=changeLog.get(--changeLogIndex);
      final ScoreChange rEvent=change.revert();
      return rEvent;
   }
   public ScoreChange redo() {
      if(!canRedo()) throw new RuntimeException("nothing to redo"); //: double check
      final ScoreChange change=changeLog.get(changeLogIndex++);
      change.perform();
      return change;
   }
}
class StackUndoManager implements UndoManager {
   private java.util.Stack<ScoreChange> performed=new java.util.Stack<ScoreChange>();
   private java.util.Stack<ScoreChange> reverted=new java.util.Stack<ScoreChange>();
   
   // private boolean comboMode=false;
   // private final java.util.List<ScoreChange> combo=new
   // ArrayList<ScoreChange>();
   public void logChange(ScoreChange e) {
      if(!reverted.isEmpty()) {
         reverted.clear();
      }
      performed.add(e);
   }

   public boolean canUndo() {
      return !performed.isEmpty();
   }

   public boolean canRedo() {
      return !reverted.isEmpty();
   }

   public ScoreChange undo() {
      if(!canUndo())
         throw new RuntimeException("nothing to undo"); // : double check
      final ScoreChange change=performed.pop();
      final ScoreChange rev=change.revert();
      reverted.push(change);
      return rev;
   }

   public ScoreChange redo() {
      if(!canRedo())
         throw new RuntimeException("nothing to redo"); // : double check
      final ScoreChange change=reverted.pop();
      change.perform();
      performed.push(change);
      return change;
   }
}