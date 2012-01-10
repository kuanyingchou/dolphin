package api.model;

public abstract class PartChange extends ScoreChange {
   public final Part part;
   public PartChange(Part p, Score s) {
      super(s);
      part=p;
   }
   
}
