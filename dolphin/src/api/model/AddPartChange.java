package api.model;

import api.util.BackRef;

public class AddPartChange extends ScoreChange {
   @BackRef public final Part part;
   public final int index;
   public AddPartChange(Part p, int index, Score s) {
      super(s);
      this.part=p;
      this.index=index;
   }
   public void perform() {
      part.setScore(score);
      score.parts.add(index, part);
   }
   public ScoreChange revert() {
      final ScoreChange event=new RemovePartChange(index, score);
      event.perform();
      return event;
   }
}
