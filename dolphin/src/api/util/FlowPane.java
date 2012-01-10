package api.util;

import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;

public class FlowPane extends JPanel {
   public FlowPane() {
      this(FlowLayout.LEADING, 5, 5);
   }
   public FlowPane(JComponent ... childs) {
      this();
      for(int i=0; i < childs.length; i++) {
         add(childs[i]);
      }
   }
   public FlowPane(String title, JComponent ... childs) {
      this(childs);
      setBorder(BorderFactory.createTitledBorder(title));
   }
   public FlowPane(int align, int hgap, int vgap) {
      setLayout(new FlowLayout(align, hgap, vgap));
   }
}
