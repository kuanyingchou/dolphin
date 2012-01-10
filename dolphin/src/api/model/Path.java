package api.model;

import api.util.Util;

public class Path {
   public int partIndex=0;
   public int index=0;
   //public int length=0;    
   public Path() {}
   public Path(int p, int i) {
      setBy(p, i);
   }
   public Path(Path that) {
      setBy(that);
   }
   public void setBy(int p, int n/*, int len*/) {
      partIndex=p;
      index=n;
      //length=len;
   }
   public void setBy(Path that) {
      this.partIndex=that.partIndex;
      this.index=that.index;
   }
   public String toString() {
      return Util.getProperties(this);
   }
}
