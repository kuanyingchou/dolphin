package api.model;

import api.util.BackRef;

public abstract class ScoreChange {
   @BackRef protected final Score score;
   public ScoreChange(Score s) {
      this.score=s;
   }
   public Score getScore() { return score; }
   public abstract void perform();
   public abstract ScoreChange revert();
} //>>>


