package api.model;

import api.util.BackRef;

public class KeySignatureChange extends ScoreChange {
   @BackRef public final Key oldKey;
   @BackRef public final Key newKey;
   
   public KeySignatureChange(Key newKey, Score s) {
      super(s);
      this.newKey=newKey;
      this.oldKey=s.keySignature;
   }
   public void perform() {
      score.keySignature=newKey;
   }
   public ScoreChange revert() {
      final ScoreChange event=new KeySignatureChange(oldKey, score);
      event.perform();
      return event;
   }
}
