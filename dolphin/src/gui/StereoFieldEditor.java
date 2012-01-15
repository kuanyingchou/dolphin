package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Window;
import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.ShortMessage;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;

import api.midi.OutDeviceManager;
import api.midi.BasicScorePlayer;
import api.model.*;
import view.*;

class PartIcon extends AbstractCpn {
   private final Part part;
   public static final int WIDTH=20;
   public static final int HEIGHT=20;
   
   public PartIcon(Part p) {
      part=p;
      setSize(WIDTH, HEIGHT);
   } 
   public int getVolume() { return getPart().getVolume(); }
   public int getPan() { return getPart().getPan(); }
   public Part getPart() {
      return part;
   }
   
   @Override
   public void adjust(Graphics2D g) {
      
   }
   @Override
   public void draw(Graphics2D g) {
      g.setColor(Color.black);
      g.drawRect(x(), y(), w(), h());
      g.drawLine(x(), y(), x()+w(), y()+h());
      g.drawLine(x()+w(), y(), x(), y()+h());
      g.drawString(getPart().getInstrument().getName(), this.x(), this.y());
      //g.drawString(""+part.getPan(), this.x(), this.y()+12);
      //g.drawString(""+part.getVolume(), this.x(), this.y()+24);
   }
   
}

class StereoField extends JComponent implements ScoreChangeListener {
   private final Score score;
   final List<PartIcon> icons=new ArrayList<PartIcon>();
   
   public StereoField(Score s) {
      score=s;
      setPreferredSize(new Dimension(400, 300));
      
      for(int i=0; i < score.partCount(); i++) {
         final Part p=score.getPart(i);
         final PartIcon icon=new PartIcon(p);
         icons.add(icon);
      }
      final PartIconDragger cmd=new PartIconDragger(this);
      addMouseListener(cmd);
      addMouseMotionListener(cmd);
      this.addComponentListener(new ComponentAdapter() {
         @Override
         public void componentResized(ComponentEvent e) {
            updateIcons();
         }
      });
   }
   
   //[ conversions
   private Point getPositionByValuePan(int volume, int pan) {
      return new Point( 
            Math.round((float)pan/127*getWidth()),
            Math.round((float)volume/127*getHeight())
      ); ///>>>try another mapping
   }
   public int getPanByX(int x) {
      return Math.round((float)x/(getWidth()-PartIcon.WIDTH)*127); 
   }
   public int getVolumeByY(int y) {
      return Math.round((float)y/(getHeight()-PartIcon.WIDTH)*127);
   }
   
   public void updateIcons() {
      for(int i=0; i < icons.size(); i++) {
         final PartIcon icon=icons.get(i);
         final Point pLoc=getPositionByValuePan(icon.getVolume(), icon.getPan());
         icon.setLocation(pLoc.x-icon.w()/2, pLoc.y-icon.h()/2);
      }
   }
   
   @Override
   protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      final Graphics2D g2=(Graphics2D)g;
      g2.setColor(Color.white);
      g2.fillRect(0, 0, getWidth()-1, getHeight()-1);
      for(PartIcon icon : icons) {
         icon.draw(g2);
         //g2.drawString("p:"+getPanByX(icon.x())+", v:"+getVolumeByY(icon.y()), icon.x()+50, icon.y()+50);
      }
      g2.setColor(Color.black);
      g2.drawRect(0, 0, getWidth()-1, getHeight()-1);
   }
   @Override
   public void scoreChanged(ScoreChange e) {
      //>>> real-time?
   }

   
}
class PartIconDragger extends MouseAdapter {
   private PartIcon holdedIcon=null;
   private int lastX, lastY;
   private final StereoField sf;
   public PartIconDragger(StereoField sf) {
      this.sf=sf;
   }
   @Override
   public void mouseMoved(MouseEvent e) {
      for(int i=sf.icons.size()-1; i>=0 ; i--) {
         if(sf.icons.get(i).contains(e.getX(), e.getY())) {
            sf.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            return;
         }
      }
      sf.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
   }
   @Override
   public void mousePressed(MouseEvent e) {
      for(int i=sf.icons.size()-1; i>=0 ; i--) {
         if(sf.icons.get(i).contains(e.getX(), e.getY())) {
            //System.err.println("press "+i);
            holdedIcon=sf.icons.get(i);
            lastX=e.getX(); lastY=e.getY();
            return;
         }
      }
      sf.repaint();
   }
   @Override
   public void mouseDragged(MouseEvent e) {
      if(holdedIcon!=null) {
         final int dx=e.getX()-lastX;
         final int dy=e.getY()-lastY;
         holdedIcon.move(dx, dy);
         
         //[ bounds check
         if(holdedIcon.x()<0) {
            holdedIcon.x(0);
         } else if(holdedIcon.x()+holdedIcon.w()>sf.getWidth()) {
            holdedIcon.x(sf.getWidth()-holdedIcon.w());
         }
         if(holdedIcon.y()<0) {
            holdedIcon.y(0);
         } else if(holdedIcon.y()+holdedIcon.h()>sf.getHeight()) {
            holdedIcon.y(sf.getHeight()-holdedIcon.h());
         }
         
         //[ send msg to player
         if(BasicScorePlayer.getInstance().isPlaying() && 
               BasicScorePlayer.getInstance().getScore()==holdedIcon.getPart().getScore()) {
            //[ volume
            final ShortMessage cc=new ShortMessage();
            try {
               cc.setMessage(0xb0, holdedIcon.getPart().channelBinding, 0x07, 
                     sf.getVolumeByY(holdedIcon.y()));
               OutDeviceManager.instance.send(cc, 0);
            } catch(InvalidMidiDataException e1) {
               e1.printStackTrace();
            }
            //[ pan
            final ShortMessage cv=new ShortMessage();
            try {
               cv.setMessage(0xb0, holdedIcon.getPart().channelBinding, 0x0A, 
                     sf.getPanByX(holdedIcon.x()));
               OutDeviceManager.instance.send(cv, 0);
            } catch(InvalidMidiDataException e1) {
               e1.printStackTrace();
            }
         }
      }
      lastX=e.getX(); lastY=e.getY();
      sf.repaint();
   }
   @Override
   public void mouseReleased(MouseEvent e) {
      holdedIcon=null;
      sf.repaint();
   }
}
public class StereoFieldEditor extends JDialog {
   
   public StereoFieldEditor(Window owner, final Score score) {
      super(owner, ModalityType.DOCUMENT_MODAL);
      setTitle("Stereo Field Editor");
      
      //[ center
      final StereoField sf=new StereoField(score);
      final JPanel center=new JPanel();
      center.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
      center.add(sf);
      add(center, BorderLayout.CENTER);
      
      //[ page end
      final JButton cancelButton=new JButton("Cancel");
      cancelButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            dispose();
         }
      });
      final JButton okButton=new JButton("OK");
      okButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            score.setComboMode(true);
            for(PartIcon icon: sf.icons) {
               icon.getPart().setVolume(sf.getVolumeByY(icon.y()));
               icon.getPart().setPan(sf.getPanByX(icon.x()));
            }
            score.setComboMode(false);
            dispose();
         }
      });
      final JPanel endPane=new JPanel();
      endPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
      endPane.add(cancelButton);
      endPane.add(okButton);
      add(endPane, BorderLayout.SOUTH);
      
      pack();
      setVisible(true);
   }
}
