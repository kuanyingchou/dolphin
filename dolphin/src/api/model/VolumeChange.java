package api.model;

public class VolumeChange extends PartChange {
   private final int oldVolume;
   private final int newVolume;
   
   public VolumeChange(int volume, Part p, Score s) {
      super(p, s);
      oldVolume=p.getVolume();
      newVolume=volume;
   }
   @Override
   public void perform() {
      part.volume=newVolume;
   }

   @Override
   public PartChange revert() {
      final PartChange c=new VolumeChange(oldVolume, part, score);
      c.perform();
      return c;
   }
}
