package api.model;

import java.util.ArrayList;


public class ComboChange extends ScoreChange {
   final java.util.List<ScoreChange> events;
   
   public ComboChange(Score s) {
      super(s);
      events=new ArrayList<ScoreChange>();
   }
//   public ComboChange(java.util.List<ScoreChange> e, Score s) {
//      super(s);
//      events=new ArrayList<ScoreChange>(e);
//   }
   
   public void add(ScoreChange change) {
      events.add(change);
   }
   public int size() {
      return events.size();
   }
   public boolean isEmpty() { return events.isEmpty(); }
   public ScoreChange get(int index) {
      return events.get(index);
   }
   
   public void perform() {
      for(int i=0; i<events.size(); i++) {
         events.get(i).perform();
      }
   }
   public ScoreChange revert() {
      final ComboChange res=new ComboChange(score);
      for(int i=events.size()-1; i>=0; i--) {
         res.add(events.get(i).revert());
      }
      return res;
   }
}
