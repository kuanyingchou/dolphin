package api.util;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.View;

class SequenceView extends JComponent {
   final Sequence score;
   /*final int MAX_PITCH=128;
   int cellWidth=20;
   int cellHeight=5;
   */
   int currentX=0;
   final boolean[] mutes;
   //final long durationOfOneBeat;
   
   public SequenceView(Sequence s) {
      score=s;
      //setSizeFromScore(s);
      
      System.err.print("Division Type: ");
      final float type=score.getDivisionType();
      if(type==Sequence.PPQ) System.err.println("PPQ");
      else if(type==Sequence.SMPTE_24) System.err.println("SMPTE_24");
      else if(type==Sequence.SMPTE_25) System.err.println("SMPTE_25");
      else if(type==Sequence.SMPTE_30) System.err.println("SMPTE_30");
      else if(type==Sequence.SMPTE_30DROP) System.err.println("SMPTE_30DROP");
      
      addMouseListener(new MouseAdapter() {
         @Override
         public void mousePressed(MouseEvent e) {
            currentX=e.getX();
            repaint();
         }
      });
      adjustSize();
      final Track[] tracks=s.getTracks();
      //final Part[] parts=s.getPartArray();
      mutes=new boolean[tracks.length];
      
      /*for(int i=0; i<parts.length; i++) {
         System.err.println("Part"+i+": "+Util.getInstrumentName(parts[i].getInstrument()));   
      }*/
      /*durationOfOneBeat=
         (long)(1.0/score.getDenominator()*4.0*
         (60000.0/score.getTempo()));*/
   }
   
   /*boolean isPlaying=false;
   Thread runner=null;
   class Runner implements Runnable {
      @Override
      public void run() {
            //(long)(score.getEndTime()*cellWidth/((double)paneWidth/cellWidth));
         //final double valueOfOneBeat=1.0 / score.getDenominator() * 4.0;
         currentX=0;
         int pageX=0;
         int beatCount=0;
         while(currentX <= score.getEndTime()*cellWidth) {
            if(!isPlaying) {
               currentX=0;
               repaint();
               break;
            }
            try {
               Thread.sleep(durationOfOneBeat);
            } catch(InterruptedException e) {}
            beatCount++;
            currentX=beatCount*cellWidth;
            final int parentWidth=getParent().getWidth();
            if(currentX-pageX>parentWidth) {
               scrollRectToVisible(new Rectangle(currentX+parentWidth-1, getHeight()/2, 1, 1));
//System.err.println(currentX);
               pageX+=parentWidth;
            }
            repaint();
         }
         pageX=0;
         currentX=0;
         repaint();
      }
   }
   
   public void play() {
      final Score s=score.copy();
      s.removeAllParts();
      final Part[] parts=score.getPartArray();
      for(int i=0; i<parts.length; i++) {
         if(!mutes[i]) s.add(parts[i]);
      }
      
      Play.midi(s, false);
      isPlaying=true;
      runner=new Thread(new Runner());
      runner.start();
   }
   public void stop() {
      Play.stopMidi();
      isPlaying=false;
   }*/
   public void zoomIn() {
      zoom*=1.25;
      adjustSize();
      scrollToCurrent();
      revalidate();
   }
   public void zoomOut() {
      zoom*=0.75;
      adjustSize();
      scrollToCurrent();
      revalidate();
   }
   
   public static final int NUM_PITCH=128;
   int noteHeight=5;
   static double zoom=0.25/2; //>>> remove staticness
   double tickWidth=0;
   private void adjustSize() {      
      final long length=score.getTickLength();
      final int height=NUM_PITCH*noteHeight;
      final int width=(int)(length*zoom);
      tickWidth=((double)width)/length;
      setPreferredSize(new Dimension(width, height));
//System.err.println("a: "+getPreferredSize());
   }
   
   private Color getColorWithVelocity(Color old, int velocity) {
      int r=old.getRed();
      int g=old.getGreen();
      int b=old.getBlue();
      
      /*float r=old.getRed()*velocity/128.0f;
      float g=old.getGreen()*velocity/128.0f;
      float b=old.getBlue()*velocity/128.0f;*/
      return new Color((int)r, (int)g, (int)b, velocity);  
   }
   
   @Override
   protected void paintComponent(Graphics g) {
      super.paintComponent(g);
//System.err.println("p: "+getSize());      
      final int width=getWidth();
      final int height=NUM_PITCH*noteHeight;//getHeight();
      
      g.setColor(Color.white);
      g.fillRect(0, 0, width, height);
      
      g.setColor(Color.lightGray);
      //[ horizontal lines
      for(int i=0; i<height; i+=noteHeight) {
         g.drawLine(0, i, width, i);
      }
      //[ vertical lines
      g.setColor(Color.lightGray);
      for(long runner=0; runner<score.getTickLength(); runner+=score.getResolution()) {
         final int x=(int)(runner*tickWidth);
         g.drawLine(x, 0, x, height);
      }
      
      //final java.util.Map<Long, Integer> tempos=new java.util.HashMap<Long, Integer>();
      
      //System.err.println(score.getDivisionType());
      final Rectangle tempRect=new Rectangle();
      int colorIndex=0;
      final Track[] tracks=score.getTracks();
      for(int i=0; i<tracks.length; i++) {
         g.setColor(Util.colors[(colorIndex++)%Util.colors.length]);
         if(mutes[i]) continue;
         final Track track=tracks[i];
         final long[][] lastTicks=new long[16][128];
         final int[] velocities=new int[128];
         for(int j=0; j < lastTicks.length; j++) {
            for(int k=0; k<lastTicks[j].length; k++) {
               lastTicks[j][k]=-1;   
            }
         }
         for(int j=0; j<track.size(); j++) {
            final MidiEvent event=track.get(j);
            if(event.getMessage() instanceof ShortMessage) {
               final ShortMessage sm=(ShortMessage)event.getMessage();
               final int command=sm.getCommand();
               final int pitch=sm.getData1();
               final int velocity=sm.getData2();
               if(command==0x90 && velocity>0) { //: note-on
                  lastTicks[sm.getChannel()][pitch]=event.getTick();
                  velocities[pitch]=velocity;
               } else if(command==0x80 || (command==0x90 && velocity<=0)) { //: note-off
                  final long lastTick=lastTicks[sm.getChannel()][pitch];
//System.err.println((double)(event.getTick()-lastTick)/score.getResolution());                  
                  if(lastTick<0) {
                     System.err.println("err while drawing");
                     continue;
                  }
                  final int x=(int)(lastTick*tickWidth);
                  final int y=(NUM_PITCH-pitch)*noteHeight;
                  final int w=(int)((event.getTick()-lastTick)*tickWidth);
                  final int h=noteHeight;
                  tempRect.setBounds(x, y, w, h);
                  if(((Graphics2D)g).getClipBounds().intersects(tempRect)) {
                     final Color old=g.getColor();
                     //g.setColor(getColorWithVelocity(old, velocities[pitch]));
                     g.fillRect(x, y, w, h);
                     
                     g.setColor(Color.black);
                     g.drawRect(x, y, w, h);
                     g.setColor(old);   
                  }
                  
                  //System.err.println("draw rect@("+x+", "+y+", "+w+", "+h+")");
               } else {
                  //System.err.println(command);
               }
            } /*else {
               if(event.getMessage() instanceof MetaMessage) {
                  final MetaMessage mm=(MetaMessage) event.getMessage();
                  final byte[] abData = mm.getData();
                  if(mm.getType()==0x51) {
                     int   nTempo = ((abData[0] & 0xFF) << 16)
                                 | ((abData[1] & 0xFF) << 8)
                                 | (abData[2] & 0xFF);           // tempo in microseconds per beat
                     int tTempo=(int)(nTempo/((double)score.getMicrosecondLength()/score.getTickLength()));
                     tempos.put(event.getTick(), tTempo);
                  }
               }
            }*/
              
         }
      }
      //System.err.println(tempos);
      /*if(tempos.size()>0) {
         final Long[] tempoChanges=new Long[tempos.size()];
         tempos.keySet().toArray(tempoChanges);
         Collections.sort(Arrays.asList(tempoChanges));
         int nextIndex=0;
         long runner=0;
         int step=score.getResolution();
         while(runner<score.getTickLength()) {
            if(nextIndex<tempoChanges.length && runner>=tempoChanges[nextIndex]) {
               runner=tempoChanges[nextIndex];
               step=tempos.get(tempoChanges[nextIndex]);
               nextIndex++;
               g.setColor(Color.blue);
            } else {
               g.setColor(Color.green);
            }
            final int x=(int)(runner*tickWidth);
            g.drawLine(x, 0, x, height);
            runner+=step;
         }
      }*/
      g.setColor(Color.black);
      g.drawRect(0, 0, width, height);
      
      final int x=(int)(currentTick*tickWidth);
      g.setColor(Color.red);
      g.drawLine(x, 0, x, height);
   }
   
   private long currentTick=0;
   private int frameStart=0;
   public void setCurrentTick(long tick) {
      currentTick=tick;
      scrollToCurrent();
   }

   private void scrollToCurrent() {
      final Rectangle visibleRect=getVisibleRect();
      if(currentTick*tickWidth-frameStart>visibleRect.getWidth()) {
         scrollRectToVisible(new Rectangle(
               frameStart+(int)visibleRect.getWidth(), getHeight()/2, 
               (int)visibleRect.getWidth(), getHeight()));
//System.err.println(currentX);
         frameStart+=visibleRect.getWidth();
      }
      repaint();      
   }
   
}
public class SequenceViewer extends JFrame {
   SequenceView sv=null;
   //ScoreParts scoreParts=new ScoreParts();
   ScoreParts scoreParts=null; 
   
   public SequenceViewer(Sequence score) {
      setLayout(new BorderLayout());
      
      final JMenu fileMenu=new JMenu("File");
      final JMenuItem openItem=new JMenuItem("Open...");
      openItem.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            final JFileChooser jfc=new JFileChooser(Util.curDir);
            jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            jfc.setFileFilter(
               new FileNameExtensionFilter("Midi Files", "mid"));
            final int ret=jfc.showOpenDialog(SequenceViewer.this);
            if(ret==JFileChooser.APPROVE_OPTION) {
               Sequence score=null;
               try {
                  score=MidiSystem.getSequence(jfc.getSelectedFile());
               } catch(InvalidMidiDataException e1) {
                  e1.printStackTrace();
               } catch(IOException e1) {
                  e1.printStackTrace();
               }
               setScore(score);
            }                  
         }
      
      });
      
      /*final JMenuItem playItem=new JMenuItem("Play");
      playItem.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            sv.play();
         }
      });
      final JMenuItem stopItem=new JMenuItem("Stop");
      stopItem.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            sv.stop();
         }
      });*/
      final JMenuItem zoomInItem=new JMenuItem("Zoom In");
      zoomInItem.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            sv.zoomIn();
            repaint();
         }
      }); 
      final JMenuItem zoomOutItem=new JMenuItem("Zoom Out");
      zoomOutItem.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            sv.zoomOut();
            repaint();
         }
      }); 
      /*final JMenuItem printItem=new JMenuItem("Print");
      printItem.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            sv.print();
         }
      });*/ 
      
      final JMenuBar menuBar=new JMenuBar();
      fileMenu.add(openItem);
      //fileMenu.add(playItem);
      //fileMenu.add(stopItem);
      fileMenu.add(zoomInItem);
      fileMenu.add(zoomOutItem);
      //fileMenu.add(printItem);
      menuBar.add(fileMenu);
      setJMenuBar(menuBar);
   
      setScore(score);
      setSize(400, 300);
      //pack();
      setVisible(true);
      
      //scoreParts.setVisible(true);
      //Play.midi(score);
   } 
   public void setScore(Sequence score) {
//System.err.println("setScore:"+score);      
      if(score==null) return;
      //if(sv!=null && score.equals(sv.score)) return; //: no use, defensive copy?
      sv=new SequenceView(score);
      
      this.getContentPane().removeAll();
      
      final JScrollPane sPane=new JScrollPane(sv);
      add(sPane, BorderLayout.CENTER);
      sPane.revalidate();
      
      scoreParts=new ScoreParts();
      add(new JScrollPane(scoreParts), BorderLayout.WEST);
      scoreParts.setScoreView(sv);
      scoreParts.revalidate();
      
      this.validate();
   }
   public void setCurrentTick(long tick) {
      sv.setCurrentTick(tick);
   }
   public static void main(String[] args) {
      //final Score test=new Score();
      //Read.midi(test, "C:/Documents and Settings/ken/桌面/eagles-hotelcalifornia.mid"); 
      final SequenceViewer sv=new SequenceViewer(null);
      //jm.util.View.show(test);
      //if(sv.sv!=null) new ScoreParts(sv.sv);
   }

}
class ScoreParts extends JPanel {
   public ScoreParts() {
      //setScoreView(sv);
      //setSize(200, 200);
   }
   public void setScoreView(final SequenceView sv) {
      if(sv.score==null) return;
      //final JPanel innerPane=new JPanel();
      this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
      final Track[] tracks=sv.score.getTracks();
      int colorIndex=0;
      for(int i=0; i < tracks.length; i++) {
         //final int inst=parts[i].getInstrument();
         //final String instName=Util.getInstrumentName(inst);
         final JCheckBox partBox=
            new JCheckBox("Track#"+i, true);
         partBox.setForeground(Util.colors[(colorIndex++)%Util.colors.length]);
         final int currentIndex=i;
         partBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               sv.mutes[currentIndex]=!partBox.isSelected();
               sv.repaint();
            }
         });
         this.add(partBox);
      }
      //add(new JScrollPane(thsi));
   }
}