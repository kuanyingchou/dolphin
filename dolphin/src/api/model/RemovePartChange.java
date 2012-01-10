package api.model;

import api.util.BackRef;

public class RemovePartChange extends ScoreChange {
   public final int index;
   @BackRef public Part part;
   
   public RemovePartChange(int index, Score s) {
      super(s);
      this.index=index;
   }
   public void perform() {
      part=score.parts.remove(index);
      part.setScore(null);
   }
   public ScoreChange revert() {
      final ScoreChange event=new AddPartChange(part, index, score);
      event.perform();
      return event;
   }
}
