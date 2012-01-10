package api.model;

public class InstrumentChange extends PartChange {
   private final Instrument oldInstrument;
   private final Instrument newInstrument;
   
   public InstrumentChange(Instrument inst, Part p, Score s) {
      super(p, s);
      oldInstrument=p.getInstrument();
      newInstrument=inst;
   }
   @Override
   public void perform() {
      part.instrument=newInstrument;
   }

   @Override
   public PartChange revert() {
      final PartChange c=new InstrumentChange(oldInstrument, part, score);
      c.perform();
      return c;
   }
}


