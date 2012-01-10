package api.model;

public class PlayUpdateChange extends ScoreChange { // >>> strange, remove this later
   public PlayUpdateChange(Score s) {
      super(s);
   }

   @Override
   public void perform() {

   }

   @Override
   public ScoreChange revert() {
      return null;
   }

}
