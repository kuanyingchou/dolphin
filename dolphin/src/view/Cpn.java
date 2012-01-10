package view;
import java.awt.Graphics2D;

//[ gui component which is lighter than swing.
public interface Cpn {
   public int x();
   public int y();
   public int w();
   public int h();
   public void x(int x);
   public void y(int y);
   public void w(int w);
   public void h(int h);
   public void setSize(int w, int h);
   public void setLocation(int x, int y);
   public void move(int dx, int dy);
   public void setBounds(int x, int y, int w, int h);
   public boolean contains(int x, int y);
   //[ to be called before draw >>> the graphics may not be the graphics at draw time 
   public void adjust(Graphics2D g); 
   public void draw(Graphics2D g);
}
