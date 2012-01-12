package api.model;

import api.util.Util;

public class Path {
   public int partIndex=0;
   public int noteIndex=0;
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
      noteIndex=n;
      //length=len;
   }
   public void setBy(Path that) {
      this.partIndex=that.partIndex;
      this.noteIndex=that.noteIndex;
   }
   public String toString() {
      return Util.getObjectInfo(this);
   }
}
