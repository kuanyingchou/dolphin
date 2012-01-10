package tablet;

public class TabletEvent {
   private final int x, y, button, pressure;
   
   public TabletEvent(int x, int y, int b, int p) {
      this.x=x;
      this.y=y;
      this.button=b;
      this.pressure=p;
   }
   
   //[ getters
   public int getX() {
      return x;
   }
   public int getY() {
      return y;
   }
   public int getButton() {
      return button;
   }
   public int getPressure() {
      return pressure;
   }
   public String toString() {
      return String.format("(%d, %d, %d, %d)", x, y, button, pressure);
   }
}
