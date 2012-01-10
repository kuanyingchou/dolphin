package api.model;

public class TitleChange extends ScoreChange {
   private final String oldTitle;
   private final String newTitle;
   
   public TitleChange(String t, Score s) {
      super(s);
      oldTitle=s.title;
      newTitle=t;
   }
   @Override
   public void perform() {
      score.title=newTitle;
   }

   @Override
   public ScoreChange revert() {
      final ScoreChange c=new TitleChange(oldTitle, score);
      c.perform();
      return c;
   }

}
