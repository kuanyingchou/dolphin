package api.model;

import api.util.BackRef;

public class AddNoteChange extends PartChange {
   @BackRef public final Note note;
   public final int index;
   public AddNoteChange(Note n, Part part, int index, Score s) {
      super(part, s);
      this.note=n;
      this.index=index;
   }
   public void perform() {
      part.notes.add(index, note);
   }
   public ScoreChange revert() {
      final ScoreChange event=new RemoveNoteChange(index, part, score);
      event.perform();
      return event;
   }
   @Override
   public String toString() {
      return "add note";
   }
}