package api.model;

import api.util.BackRef;

public class RemoveNoteChange extends PartChange {
   public final int index;
   @BackRef public Note note;
   public RemoveNoteChange(int index, Part p, Score s) {
      super(p, s);
      this.index=index;
   }
   public void perform() {
      note=part.notes.remove(index);
   }
   public ScoreChange revert() {
      final ScoreChange event=new AddNoteChange(note, part, index, score);
      event.perform();
      return event;
   }
}
