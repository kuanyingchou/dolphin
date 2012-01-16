package gui;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import api.midi.NoteOffMessage;
import api.midi.NoteOnMessage;
import api.midi.OutDeviceManager;
import api.midi.BasicScorePlayer;
import api.model.Instrument;
import api.util.Util;


//import tablet.TabletEvent;
//import tablet.TabletListener;
import view.ScoreView;


//>>> out device selection, instrument selection, pressure
public class VirtualKeyboard extends JComponent implements Receiver {
   public static final int NUM_CHANNEL=16;
   public static final int NUM_PITCH=128;
   
   public static int whiteKeyWidth=16;
   public static int whiteKeyHeight=80;
   public static int blackKeyWidth=8;
   public static int blackKeyHeight=50;
   
   //private static MidiDevice outDevice;
   //private static Receiver outDeviceReceiver;
   public static ImageIcon blackKeyIcon=Util.getImageIcon("images/blackkey.png");
   public static ImageIcon blackKeyPressedIcon=Util.getImageIcon("images/blackkey_press.png");
   public static ImageIcon whiteKeyIcon=Util.getImageIcon("images/whitekey.png");
   public static ImageIcon whiteKeyPressedIcon=Util.getImageIcon("images/whitekey_press.png");
   
   public static Color lightColor=new Color(255, 198, 0, 100);
   public static Color shadowColor=new Color(0, 0, 0, 50);
   
   private int top=0;
   private int bottom=0;
   private int left=0;
   private int right=0;
   private MainFrame mainFrame;
   
   private boolean isRecording=false;
   public boolean isMouseEnabled=true;
   
   public VirtualKeyboard(MainFrame mf) {
      //setMaximumSize(new Dimension(128, 32));
      mainFrame=mf;
      setPreferredSize(new Dimension(left+whiteKeyWidth*75+right, top+whiteKeyHeight+bottom));
      //MainFrame.deviceManager.addReceiver(this); //loop
      MainFrame.player.addReceiver(this);
      setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      
//      try {
//         outDevice=MidiSystem.getSynthesizer();
//         //outDevice.getTransmitter().setReceiver(this);
//         outDeviceReceiver=outDevice.getReceiver();
//         outDevice.open();
//      } catch (MidiUnavailableException e2) {
//         e2.printStackTrace();
//      }
      
      final MouseAdapter ma=new KeyboardMouseListener();
      addMouseListener(ma);
      addMouseMotionListener(ma); //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
      
      final KeyAdapter ka=new KeyboardKeyAdapter();
      addKeyListener(ka);
      setFocusable(true);
      
      addMouseListener(new MouseAdapter() {
         @Override
         public void mousePressed(MouseEvent e) {
            requestFocusInWindow();
         }
      });
      
      /*
      //tablet feature
      MainFrame.tabletManager.addTabletListener(new TabletListener() {
         int lastIndex=-1;
         private boolean isValidIndex(int i) {
            return i>=0 && i<NUM_PITCH;
         }
         private int getVelocity(int p) {
            int v=(int)(p/1024.0*128); //>>> fix magic numbers
            if(v<0) v=0;
            else if(v>=128) v=128;
            return v;
         }
         
         @Override
         public void tabletMoved(TabletEvent e) {
            if(e.getPressure()<=0) return;
            final Point loc=VirtualKeyboard.this.getLocationOnScreen();
            final int index=getKeyIndex(e.getX()-loc.x, e.getY()-loc.y);
            if(index<0 || index>=keyPressed.length) return;  
            if(isValidIndex(lastIndex) && index!=lastIndex) releaseKey(lastIndex, 100, true);
            if(!keyPressed[index]) pressKey(index, getVelocity(e.getPressure()), true);
            lastIndex=index;
         }

         @Override
         public void tabletPressed(TabletEvent e) {
            System.err.println(e);
            final int p=e.getPressure();
            if(p>0) {
               final int v=getVelocity(p);
               final Point loc=VirtualKeyboard.this.getLocationOnScreen();
               final int index=getKeyIndex(e.getX()-loc.x, e.getY()-loc.y);
               if(index<0 || index>=keyPressed.length) return;
               pressKey(index, v, true);
               lastIndex=index;
            }            
         }

         @Override
         public void tabletReleased(TabletEvent e) {
            for(int i=0; i < keyPressed.length; i++) {
               if(keyPressed[i]) {
                  releaseKey(i, 100, true);
               }
            }
         }
         
      });
      */
   }
   
   
   @Override
   public void close() {
      
   }

   @Override
   public void send(MidiMessage message, long timeStamp) {
      if(message instanceof ShortMessage) {
         final ShortMessage sm=(ShortMessage)message;
         if(sm.getCommand()==0x90 && sm.getData2()!=0) { //note on
            SwingUtilities.invokeLater(new Runnable() {
               public void run() {
                  pressKey(sm.getData1(), sm.getData2(), false);
               }
            });
            //decreaseAll();
            repaint();
         } else if(sm.getCommand()==0x80 || (sm.getCommand()==0x90 && sm.getData2()==0)) { //note off
            SwingUtilities.invokeLater(new Runnable() {
               public void run() {
                  releaseKey(sm.getData1(), sm.getData2(), false);
               }
            });
         }
      }
      
   }
   
   private final boolean[] keyPressed=new boolean[NUM_PITCH];
   private final boolean[] displayKeyPressed=new boolean[NUM_PITCH];
   
   private Instrument instrument;
   {
      setInstrument(Instrument.getInstance(0));
   }
   private int channel=15;
   public void setInstrument(Instrument inst) {
      instrument=inst;
      if(instrument.isPercussion()) {
         channel=9;
      } else {
         channel=15;   
         //] use last channel, hope it will not conflict with playing score   
         final ShortMessage pc=new ShortMessage();
         try {
            pc.setMessage(0xC0, channel, instrument.getValue(), 0);
         } catch(InvalidMidiDataException e1) {
            e1.printStackTrace();
         }
         OutDeviceManager.instance.send(pc, 0); //>>> may effect playing score
      }
      
      
   }
   public Instrument getInstrument() { return instrument; }
   public synchronized boolean isRecording() { return isRecording; }
   public void setRecording(boolean r) { isRecording=r; }
   
   public void pressKey(int pitch, int velocity, boolean sing) {
      if(!sing) {
         displayKeyPressed[pitch]=true;
      } else {
         keyPressed[pitch]=true;
         //System.err.println("press "+Util.getPitchName(pitch)+"("+pitch+")");
         
         
         //[ note on
         final ShortMessage sm=new NoteOnMessage(channel, pitch, velocity);
         
         //outDeviceReceiver.send(sm, 0);
         final ViewPane vf=((ViewPane)mainFrame.desktop.getSelectedComponent());
         if(vf==null) return;
         final ScoreView sheet=vf.scoreView;

         OutDeviceManager.instance.send(sm, 0);
         if(isRecording) sheet.send(sm, 0);
      }
      repaint();
   }
   public void releaseKey(int pitch, int velocity, boolean sing) {
      if(!sing) {
         displayKeyPressed[pitch]=false;
      } else {
         keyPressed[pitch]=false;
         //System.err.println("release "+Util.getPitchName(pitch)+"("+pitch+")");
         
         final ShortMessage sm=new NoteOffMessage(channel, pitch, velocity);
         
         //outDeviceReceiver.send(sm, 0);
         final ViewPane vf=((ViewPane)mainFrame.desktop.getSelectedComponent());
         if(vf==null) return;
         final ScoreView sheet=vf.scoreView;
         
         OutDeviceManager.instance.send(sm, 0);
         if(isRecording) sheet.send(sm, 0);
      }
      repaint();
   }
   
   private final int[] steps= {2, 2, 1, 2, 2, 2, 1};
   public int getKeyIndex(int ex, int ey) {
      ex-=left;
      ey-=top;
      int stepIndex=0;
      int x=0;
      
      //[ black key
      
      for(int p=0; p<keyPressed.length;) {
         final int s=steps[stepIndex++%steps.length];
         if(s==2) { //[ draw black key
            if(p+1>=keyPressed.length) break;
            final Rectangle r=new Rectangle(x+whiteKeyWidth-blackKeyWidth/2, 0, blackKeyWidth, blackKeyHeight);
            if(r.contains(ex, ey)) return p+1;
         }
         p+=s;
         x+=whiteKeyWidth;
      }
      
      //[ white key
      stepIndex=0;
      x=0;
      for(int p=0; p<keyPressed.length;) {
         final Rectangle r=new Rectangle(x, 0, whiteKeyWidth, whiteKeyHeight);
         if(r.contains(ex, ey)) return p;
         final int s=steps[stepIndex++%steps.length];
         p+=s;
         x+=whiteKeyWidth;
      }
      return -1;
   }
   @Override
   protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      //[ draw background
      g.setColor(Color.darkGray);
      g.fillRect(0, 0, getWidth(), getHeight());
      
      g.translate(left, top);
      
      int stepIndex=0;
      int x=0;
      //[ draw white key
      for(int p=0; p<keyPressed.length;) {
         if(keyPressed[p]) {
            if(whiteKeyPressedIcon==null) {
               g.setColor(Color.orange);               
               g.fillRect(x, 0, whiteKeyWidth, whiteKeyHeight);
            } else {
               g.drawImage(whiteKeyPressedIcon.getImage(), 
                     x, 0, whiteKeyWidth, whiteKeyHeight, null);   
            }
            
         } else {
            if(whiteKeyIcon==null) {
               g.setColor(Color.white);
               g.fill3DRect(x, 0, whiteKeyWidth, whiteKeyHeight, true);
            } else {
               g.drawImage(whiteKeyIcon.getImage(), 
                  x, 0, whiteKeyWidth, whiteKeyHeight, null);
            }
         }
         if(displayKeyPressed[p]) {
            g.setColor(lightColor);
            g.fillRect(x, 0, whiteKeyWidth, whiteKeyHeight);
         }
         //g.fill3DRect(x, 0, whiteKeyWidth, whiteKeyHeight, true);
         //g.drawImage(whiteKeyIcon.getImage(), 
         //      x, 0, whiteKeyWidth, whiteKeyHeight, null);
         if(p==60) {
            g.setColor(Color.gray);
            g.drawString("c", x+5, whiteKeyHeight-5);
         }
         final int s=steps[stepIndex++%steps.length];
         p+=s;
         x+=whiteKeyWidth;
      }
      
      //[ draw black key
      stepIndex=0;
      x=0;
      for(int p=0; p<keyPressed.length;) {
         final int s=steps[stepIndex++%steps.length];
         if(s==2) { //[ draw black key
            if(p+1>=keyPressed.length) break;
            //g.setColor(Color.black);
            //g.fillRect(x+whiteKeyWidth-blackKeyWidth/2-1, 0, blackKeyWidth+2, blackKeyHeight+1);
            if(keyPressed[p+1]) {
               if(blackKeyPressedIcon==null) {
                  g.setColor(Color.orange);
                  g.fillRect(x+whiteKeyWidth-blackKeyWidth/2, 0, blackKeyWidth, blackKeyHeight);   
               } else {
                  g.drawImage(blackKeyPressedIcon.getImage(), 
                        x+whiteKeyWidth-blackKeyWidth/2, 0, blackKeyWidth, blackKeyHeight, null);   
               }
            } else {
//[ very slow under linux
//g.setColor(shadowColor);
//g.fillRoundRect(x+whiteKeyWidth-blackKeyWidth/2+2, 0, blackKeyWidth, blackKeyHeight+2, 4, 4);
               //g.setColor(Color.black);
               if(blackKeyIcon==null) {
                  g.setColor(Color.black);
                  g.fillRect(x+whiteKeyWidth-blackKeyWidth/2, 0, blackKeyWidth, blackKeyHeight);
               } else {
                  g.drawImage(blackKeyIcon.getImage(), 
                        x+whiteKeyWidth-blackKeyWidth/2, 0, blackKeyWidth, blackKeyHeight, null);                  
               }
            }
            if(displayKeyPressed[p+1]) {
               g.setColor(lightColor);
               g.fillRect(x+whiteKeyWidth-blackKeyWidth/2, 0, blackKeyWidth, blackKeyHeight);
            }
            //g.fill3DRect(x+whiteKeyWidth-blackKeyWidth/2, 0, blackKeyWidth, blackKeyHeight, true);
            //g.drawImage(blackKeyIcon.getImage(), 
            //      x+whiteKeyWidth-blackKeyWidth/2, 0, blackKeyWidth, blackKeyHeight, null);
         }
         p+=s;
         x+=whiteKeyWidth;
      }
      g.translate(-left, -top);
   }
//   public static void main(String[] args) {
//      SwingUtilities.invokeLater(new Runnable() {
//         @Override
//         public void run() {
//            final JFrame jf=new JFrame("Keyboard");
//            jf.add(new VisualKeyboard());
//            jf.pack();
//            jf.setVisible(true);
//         }
//      });
//   }
   
   //>>> key pressure by wheel
   class KeyboardMouseListener extends MouseAdapter {
      int lastIndex=-1;
      
      private boolean isValidIndex(int i) {
         return i>=0 && i<NUM_PITCH;
      }
      
      @Override
      public void mouseDragged(MouseEvent e) {
         if(!isMouseEnabled) return;
         final int index=getKeyIndex(e.getX(), e.getY());
         if(index<0 || index>=keyPressed.length) return;  
         if(isValidIndex(lastIndex) && index!=lastIndex) releaseKey(lastIndex, 100, true);
         if(!keyPressed[index]) pressKey(index, 100, true);
         lastIndex=index;
         //repaint();
      }
      @Override
      public void mousePressed(MouseEvent e) {
         if(!isMouseEnabled) return;
         final int index=getKeyIndex(e.getX(), e.getY());
         if(index<0 || index>=keyPressed.length) return;
         pressKey(index, 100, true);
         repaint();
         lastIndex=index;
      }
      @Override
      public void mouseReleased(MouseEvent e) {
         if(!isMouseEnabled) return;
//            final int index=getKeyIndex(e.getX(), e.getY());
//            if(index<0) return;
//            releaseKey(index);
         for(int i=0; i < keyPressed.length; i++) {
            if(keyPressed[i]) {
               releaseKey(i, 100, true);
            }
         }
         //repaint();
      }
   }
   
   class KeyboardKeyAdapter extends KeyAdapter { //>>> asdf scheme
      int offset=60;
      private int getIndex(char keyChar) {
         int index=-1;
//         switch(keyChar) {
//         case 'a':        index=0; break; /*Do*/
//            case 'w':     index=1; break;
//         case 's':        index=2; break;
//            case 'e':     index=3; break;
//         case 'd':        index=4; break;
//         case 'f':        index=5; break;
//            case 't':     index=6; break;
//            case 'u':     index=6; break;
//         case 'j':        index=7; break;
//            case 'i':     index=8; break;
//         case 'k':        index=9; break;
//            case 'o':     index=10; break;
//         case 'l':        index=11; break;
//         case ';':        index=12; break; /*Do*/
//         
//         }
         //[ another mapping
         switch(keyChar) {
         case 'a':        index=0; break;
            case 'w':     index=1; break;
         case 's':        index=2; break;
            case 'e':     index=3; break;
         case 'd':        index=4; break;
         case 'f':        index=5; break;
            case 't':     index=6; break;
         case 'g':        index=7; break;
            case 'y':     index=8; break;
         case 'h':        index=9; break;
            case 'u':     index=10; break;
         case 'j':        index=11; break;
         case 'k':        index=12; break;
            case 'o':     index=13; break;
         case 'l':        index=14; break;
            case 'p':     index=15; break;
         case ';':        index=16; break;
         }
         
         
         //final int index=e.getKeyChar()-49+offset;
         if(index<0) return -1;
         return index+=offset;
      }
      @Override
      public void keyPressed(KeyEvent e) {
         if(e.getKeyChar()=='+') {
            offset+=12;
            if(offset>=NUM_PITCH) offset=12*(NUM_PITCH/12);
         } else if(e.getKeyChar()=='-') {
            offset-=12;
            if(offset<0) offset=0;
         }
         //if(!Character.isDigit(e.getKeyChar())) return;
         int index=getIndex(e.getKeyChar());
         if(index<0 || index>=keyPressed.length) return;
         if(keyPressed[index]) return; 
         pressKey(index, 100, true);
         //repaint();
      }
      @Override
      public void keyReleased(KeyEvent e) {
         //if(!Character.isDigit(e.getKeyChar())) return;
         final int index=getIndex(e.getKeyChar()); //e.getKeyChar()-49+offset;
         if(index<0 || index>=keyPressed.length) return;
         releaseKey(index, 100, true);
         //repaint();
      }
   }
}

