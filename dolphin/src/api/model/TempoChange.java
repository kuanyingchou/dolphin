package api.model;

public class TempoChange extends ScoreChange {
   final float oldTempo;
   final float newTempo;
   
   public TempoChange(float t, Score s) {
      super(s);
      oldTempo=s.tempo;
      newTempo=t;
   }
   @Override
   public void perform() {
      score.tempo=newTempo;
   }

   @Override
   public ScoreChange revert() {
      final ScoreChange c=new TempoChange(oldTempo, score);
      c.perform();
      return null;
   }

}
