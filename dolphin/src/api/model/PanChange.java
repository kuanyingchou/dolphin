package api.model;

public class PanChange extends PartChange {
   private final int oldPan;
   private final int newPan;
   
   public PanChange(int pan, Part p, Score s) {
      super(p, s);
      oldPan=p.getPan();
      newPan=pan;
   }
   @Override
   public void perform() {
      part.pan=newPan;
   }

   @Override
   public PartChange revert() {
      final PartChange c=new PanChange(oldPan, part, score);
      c.perform();
      return c;
   }
}
