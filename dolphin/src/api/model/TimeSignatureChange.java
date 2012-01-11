package api.model;

public class TimeSignatureChange extends ScoreChange {
   
   private final int oldNumerator;
   private final int oldDenominator;
   private final int newNumerator;
   private final int newDenominator;
   
   public TimeSignatureChange(int numerator, int denominator, Score s) {
      super(s);
      oldNumerator=s.numerator;
      oldDenominator=s.denominator;
      newNumerator=numerator;
      newDenominator=denominator;
   }
   @Override
   public void perform() {
      score.numerator=newNumerator;
      score.denominator=newDenominator;
      
      //>>> one time sig. may have many patterns
      switch(score.numerator) {
      case 2: score.beatPattern=new int[] {Score.STRONG, Score.WEAK}; break;
      case 3: score.beatPattern=new int[] {Score.STRONG, Score.WEAK, Score.WEAK}; break;
      case 4: score.beatPattern=new int[] {Score.STRONG, Score.WEAK, Score.STRONG, Score.WEAK}; break;
      case 5: score.beatPattern=new int[] {Score.STRONG, Score.WEAK, Score.STRONG, Score.WEAK, Score.WEAK}; break;
      case 6: score.beatPattern=new int[] {Score.STRONG, Score.WEAK, Score.WEAK, Score.STRONG, Score.WEAK, Score.WEAK}; break;
      default:score.beatPattern=new int[] {Score.STRONG, Score.WEAK}; break;
      }
   }

   @Override
   public ScoreChange revert() {
      final ScoreChange c=new TimeSignatureChange(oldNumerator, oldDenominator, score);
      c.perform();
      return c;
   }

}
