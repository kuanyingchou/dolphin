package api.model;

public class MuteChange extends PartChange {
   private final boolean oldMute; //toggle
   
   public MuteChange(Part p, Score s) {
      super(p, s);
      oldMute=p.isMute();
   }
   @Override
   public void perform() {
      part.mute=!oldMute;
   }

   @Override
   public PartChange revert() {
      final PartChange c=new MuteChange(part, score);
      c.perform();
      return c;
   }
}