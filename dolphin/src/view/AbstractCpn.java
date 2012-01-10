package view;
import java.awt.Graphics2D;



public abstract class AbstractCpn implements Cpn {
   protected int x, y, w, h;
   
   @Override
   public int h() {
      return h;
   }
   @Override
   public int w() {
      return w;
   }
   @Override
   public int x() {
      return x;
   }
   @Override
   public int y() {
      return y;
   }

   @Override
   public void x(int x) {
      this.x=x;
   }
   @Override
   public void y(int y) {
      this.y=y;
   }
   @Override
   public void w(int w) {
      this.w=w;
   }
   @Override
   public void h(int h) {
      this.h=h;
   }
   
   @Override
   public void setLocation(int x, int y) {
      this.x=x;
      this.y=y;
   }
   
   @Override
   public void move(int dx, int dy) {
      this.x+=dx;
      this.y+=dy;
   }
   
   @Override
   public void setSize(int w, int h) {
      this.w=w;
      this.h=h;
   }
   
   @Override
   public void setBounds(int x, int y, int w, int h) {
      this.x=x;
      this.y=y;
      this.w=w;
      this.h=h;
   }
   
   @Override
   public boolean contains(int x, int y) {
      return x>=this.x && x<this.x+this.w &&
             y>=this.y && y<this.y+this.h;
   }
}