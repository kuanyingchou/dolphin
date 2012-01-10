package test;
import gui.VisualEffect;
import api.util.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Formatter;
import java.util.Vector;

import javax.activation.DataHandler;
import javax.sound.midi.*;
import javax.sound.midi.MidiDevice.Info;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;

//import sun.awt.datatransfer.DataTransferer;
//import sun.swing.SwingUtilities2;


public class GraphicalMidiPlayer {
   /*static {
      setLookAndFeel();
   }*/
   public static final String APP_NAME="midiplayer";
   private final static JFrame mainFrame=new JFrame(APP_NAME);
   private final static JSlider progress=new JSlider(0, 0, 0);
   private final static JLabel status=new JLabel();
   private static Sequencer sequencer;
   private static MidiDevice outDevice;
   //private static double lenTick=0;
   private final static JList playList=new JList(new DefaultListModel());
   private final static JSlider volumeSlider=new JSlider(0, 127, 127);
   private static int currentIndex=-1;
   public static final Font monoFont=Font.getFont("Monospaced");
   private static final VisualEffect visualEffect=new VisualEffect();
   
   static {
      try {
         sequencer=MidiSystem.getSequencer(false);
         setOutDevice(MidiSystem.getSynthesizer());
         sequencer.getTransmitter().setReceiver(visualEffect);
         //sequencer.getTransmitter().setReceiver(new DumpReceiver(System.out));
      
         sequencer.addMetaEventListener(new MetaEventListener() {
            public void meta(MetaMessage meta) {
               if(meta.getType()==47) {
                  SwingUtilities.invokeLater(new Runnable() {
                     public void run() {
                        if(repeat) {
                           stop(); //>>>?
                           play();
                        } else {
                           next();
                        }
                     }
                  });
               }
            }
         });
      } catch(MidiUnavailableException e) {
         e.printStackTrace();
      }
      
      progress.setOpaque(false);
      progress.addChangeListener(new ChangeListener() {
         public void stateChanged(ChangeEvent e) {
            if(progress.getValueIsAdjusting()) return;
            final long pos=getProgress();
            if(!sequencer.isRunning()) {
               sequencer.setTickPosition(pos);
            }
         }
      });
      playList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); //>>> multi
      playList.addMouseListener(new MouseAdapter() {
         public void mousePressed(MouseEvent e) {
            if(e.getClickCount()>=2) {
               currentIndex=playList.getSelectedIndex();
               stop();
               play();
            }
         }
      });
      
      setProgressTime(0);
   }
   public static SequenceViewer sv=new SequenceViewer(null);
      
   public static void open(final File file) {
      final DefaultListModel pListModel=(DefaultListModel)playList.getModel();
      pListModel.addElement(file);
   }
   
   private static void setProgressSlider(long p) {
      progress.setValue((int)p);
   }
   private static long getProgress() {
      return progress.getValue();
   }
   public static final long SECOND_MICRO_SECOND=1000000L;
   public static final long MINUTE_MICRO_SECOND=SECOND_MICRO_SECOND*60;
   public static final long HOUR_MICRO_SECOND=MINUTE_MICRO_SECOND*60;
   
   public static void setProgressTime(long microPos) {
      final long hour=microPos/HOUR_MICRO_SECOND;
      microPos-=hour*HOUR_MICRO_SECOND;
      final long min=microPos/MINUTE_MICRO_SECOND;
      microPos-=min*MINUTE_MICRO_SECOND;
      final long sec=microPos/SECOND_MICRO_SECOND;
      
      status.setText(String.format("%02d:%02d:%02d", hour, min, sec));
   }
   private static void play(File file) {
      mainFrame.setTitle(file.getName()+" - "+APP_NAME);
      
      if(sequencer.isOpen()) {
         if(sequencer.isRunning()) {
            sequencer.stop();
         }
      } else {
         try {
            sequencer.open();
         } catch(MidiUnavailableException e) {
            e.printStackTrace();
         }
      }
      if(!outDevice.isOpen()) {
         try {
            setOutDevice(outDevice);
         } catch(MidiUnavailableException e) {
            e.printStackTrace();
         }
      }
      Sequence sequence=null;
      try {
         sequence=MidiSystem.getSequence(file);
         sequencer.setSequence(sequence);
      } catch(InvalidMidiDataException e) {
         JOptionPane.showMessageDialog(mainFrame, "Invalid Midi File: "+file.getPath());
         e.printStackTrace();
         stop();
         return;
      } catch(IOException e) {
         JOptionPane.showMessageDialog(mainFrame, "Can't Access File: "+file.getPath());
         e.printStackTrace();
         stop();
         return;
      }
      progress.setMaximum((int)sequencer.getTickLength()); //>>> cast
      
      if(sequencer.getSequence()==null) return;
      if(sequencer.isRunning()) return;    
      sequencer.setTickPosition(getProgress());
      
      sv.setScore(sequence);
      sv.setCurrentTick(getProgress());
      
      sequencer.start();   
      
      new Thread(new Runnable() {
         public void run() {
            while(sequencer.isRunning()) {
               final long nextTickPos=sequencer.getTickPosition();
               final long nextMsPos=sequencer.getMicrosecondPosition();
               SwingUtilities.invokeLater(new Runnable() {
                  public void run() {
                     setProgressSlider(nextTickPos);
                     setProgressTime(nextMsPos);
                     sv.setCurrentTick(nextTickPos);
                  }
               });

               try {
                  Thread.sleep(100);
               } catch(InterruptedException e) {}
            }
         }
      }).start();
   }

   public static void play(){
      final int size=playList.getModel().getSize();
      if(size<=0 || currentIndex>=size) {
         currentIndex=-1;
         return;
      }
      if(currentIndex<0) currentIndex=0;
      final File file=(File)playList.getModel().getElementAt(currentIndex);
      play(file);
      playList.repaint();
   }
   
   public static void pause() {
      if(!sequencer.isRunning()) return;
      setProgressSlider(sequencer.getTickPosition());
      sequencer.stop();
      playList.repaint();
   }
   public static void stop() {
      setProgressSlider(0); //>>>
      setProgressTime(0);      
      progress.setMaximum(0);
      if(sequencer.isRunning()) {
         System.err.println("stopping sequencer...");   
         sequencer.stop();
         System.err.println("sequencer stopped");      
      }
      playList.repaint();
   }
   public static void next() {
      stop();
      if(currentIndex<playList.getModel().getSize()-1) {
         currentIndex++;
         play();
      } else {
         currentIndex=-1;
      }
   }
   public static void previous() {
      stop();
      if(currentIndex>0) {
         currentIndex--;
         play();
      }
   }
   private static boolean repeat=false;
   public static void toggleRepeat() {
      repeat=!repeat;
   }
   public static void setOutDevice(MidiDevice device) throws MidiUnavailableException {
      /*if(outDevice==device) {
         return;
      }*/
      try {
         if(device==sequencer || device instanceof Sequencer) {
            throw new MidiUnavailableException();
         }
         device.open();
         sequencer.getTransmitter().setReceiver(device.getReceiver());
         if(outDevice!=null && outDevice.isOpen()) {
            outDevice.close();
         }
         outDevice=device;
         
         changeVolumeV2(volumeSlider.getValue());
      } catch(MidiUnavailableException e) {
         //e.printStackTrace();
         //device.close();
         if(outDevice!=null) outDevice.open();
         throw e;
      }
      
   }
   
   
   private static Vector<Info> getOutDeviceInfos() throws MidiUnavailableException {
      final Vector<Info> outInfos=new Vector<Info>();
      final Info[] deviceInfos=MidiSystem.getMidiDeviceInfo();
      for(int i=0; i < deviceInfos.length; i++) {
         if(MidiSystem.getMidiDevice(deviceInfos[i]).getMaxReceivers()!=0) {
            outInfos.add(deviceInfos[i]);
         }
      }
      return outInfos;
   }
   /*public void changeVolumeV1() {
      final MidiChannel[] channels=synthesizer.getChannels();

      // gain is a value between 0 and 1 (loudest)
      final double gain=volumeSlider.getValue() / 100.0;
      System.err.println(gain);
      for(int i=0; i < channels.length; i++) {
         channels[i].controlChange(7, (int) (gain * 127.0));
      }
   }*/

   public static void changeVolumeV2(int volume) { //: 0~127
      Receiver receiver=null;
      try {
         receiver=outDevice.getReceiver();
      } catch(MidiUnavailableException e) {
         e.printStackTrace();
      }
      final ShortMessage volumeMessage=new ShortMessage();
      for(int i=0; i < 16; i++) {
         try {
            volumeMessage.setMessage(ShortMessage.CONTROL_CHANGE, i, 7,
                  volume);
         } catch(InvalidMidiDataException e1) {
            e1.printStackTrace();
         }
         receiver.send(volumeMessage, -1);
      }

   }

   public static void main(String[] args) {
      //System.err.println("Start Playback...");
      //play("liz_et4.mid");
      final JButton playButton=new JButton(new ImageIcon("Play24.gif"));
      final JButton pauseButton=new JButton(new ImageIcon("Pause24.gif"));
      final JButton stopButton=new JButton(new ImageIcon("Stop24.gif"));
      final JButton nextButton=new JButton(new ImageIcon("FastForward24.gif"));
      final JButton previousButton=new JButton(new ImageIcon("Rewind24.gif"));
      final JToggleButton repeatButton=new JToggleButton(new ImageIcon("Refresh24.gif"));
      //final JButton toStartButton=new JButton(new ImageIcon("Rewind24.gif"));
      //final JButton toEndButton=new JButton(new ImageIcon("FastForward24.gif"));
      
      playButton.setToolTipText("Play");
      pauseButton.setToolTipText("Pause");
      stopButton.setToolTipText("Stop");
      nextButton.setToolTipText("Next");
      previousButton.setToolTipText("Previous");
      repeatButton.setToolTipText("Repeat");
      
      playButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            play();
         }
      });
      pauseButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            pause(); 
         }
      });
      stopButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            stop();
            currentIndex=-1;
         }
      });   
      nextButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            next();  
         }
      }); 
      previousButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            previous();  
         }
      }); 
      repeatButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            toggleRepeat(); 
            repeatButton.setSelected(repeat);
         }
      }); 
      
      
      
      Vector<Info> outInfos=null;
      try {
         outInfos=getOutDeviceInfos();
      } catch(MidiUnavailableException e2) {
         e2.printStackTrace();
      }
      final JComboBox deviceList=new JComboBox(outInfos);
      deviceList.setSelectedItem(outDevice.getDeviceInfo());  
      deviceList.addItemListener(new ItemListener() {
         public void itemStateChanged(ItemEvent e) {
            if(e.getStateChange()!=ItemEvent.SELECTED) return;
            try {
               final MidiDevice device=
                  MidiSystem.getMidiDevice((Info)deviceList.getSelectedItem());
               //System.err.println("s: "+device.getDeviceInfo());
               setOutDevice(device);
            } catch(MidiUnavailableException e1) {
               //System.err.println(outDevice.getDeviceInfo());
               JOptionPane.showMessageDialog(mainFrame, "Can't use this device");
               deviceList.setSelectedItem(outDevice.getDeviceInfo());
               return;
            }
         }
      });
      
      playList.setFont(monoFont);
      playList.setDragEnabled(true);
      playList.setCellRenderer(new DefaultListCellRenderer() {
         public Component getListCellRendererComponent(JList list,
               Object value, int index, boolean isSelected, boolean cellHasFocus) {          
            final File file=(File)value;
            final String label=(index+1)+". "+file.getName();
            final Component res=super.getListCellRendererComponent(
                  list, label, index, isSelected, cellHasFocus);
            if(outDevice.isOpen() && index==currentIndex) {
               res.setForeground(Color.red);
            }
            return res;
         }
         
      });
      playList.setTransferHandler(new TransferHandler() {
         public boolean canImport(TransferHandler.TransferSupport info) {
            return true;
         }
         private String getStringFromInputStreamReader(InputStreamReader reader) {
            final BufferedReader in = new BufferedReader(
                  reader);
            final StringBuffer buffer = new StringBuffer();
            String line;
            try {
               while ((line = in.readLine()) != null) {
                 buffer.append(line).append("\n");
               }
            } catch(IOException e) {
               e.printStackTrace();
            }
            return buffer.toString();
         }
         private java.util.List<File> getFilesFromUriStr(String paths) {
            final String[] pathArr=paths.split("\n");
            final java.util.List<File> fileData=new java.util.ArrayList<File>();

            for(String s : pathArr) {
               try {
                  final File f=new File(new URI(s.trim()));
                  if(f.exists()) fileData.add(f);
               } catch(URISyntaxException e) {
                  continue; //: failed to create URI from s
               }
            }

            return fileData;
         }
         @SuppressWarnings("unchecked")
         public boolean importData(TransferHandler.TransferSupport info) {
            if(!info.isDrop()) {
               return false;
            }
            final JList list=(JList) info.getComponent();
            final DefaultListModel listModel=(DefaultListModel) list.getModel();
            final JList.DropLocation dl=(JList.DropLocation) info.getDropLocation();
            final boolean insert=dl.isInsert();
            int index=dl.getIndex();

            final Transferable t=info.getTransferable();
            java.util.List<File> fileData=null;

            try {
               if(info.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                  fileData=(java.util.List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
                  
                //[ for linux support   
               } else if(info.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                  final String paths=(String) t.getTransferData(DataFlavor.stringFlavor);
                  fileData=getFilesFromUriStr(paths);
               } else {
                  if(t.getTransferDataFlavors().length<=0) return false;
                  final DataFlavor firstDataFlavor=t.getTransferDataFlavors()[0];
                  final Object data=t.getTransferData(firstDataFlavor);
                  if(data instanceof InputStream) {
                     final String uriStr=getStringFromInputStreamReader(
                           new InputStreamReader((InputStream)data));
                     fileData=getFilesFromUriStr(uriStr);
                  } else if(data instanceof InputStreamReader) {
                     final String uriStr=getStringFromInputStreamReader((InputStreamReader)data);
                     fileData=getFilesFromUriStr(uriStr);
                  } else if(data instanceof String) {
                     //System.err.println(data);
                     fileData=getFilesFromUriStr((String)data);
                  } else {
                     //>>>
                     System.err.println(data.getClass());
                     System.err.println(data);
                     for(DataFlavor df: t.getTransferDataFlavors()) {
                        System.err.println(df.getHumanPresentableName());
                     }   
                  }
               }
            } catch(UnsupportedFlavorException e1) {
               e1.printStackTrace();
            } catch(IOException e1) {
               e1.printStackTrace();
            }

            try {
               for(int i=0; i < fileData.size(); i++) {
                  final File file=fileData.get(i);
                  if(insert) {
                     listModel.add(index++, file);
                  } else {
                     if(index < 0) {
                        listModel.addElement(fileData.get(i));
                     } else if(index < listModel.getSize()) {
                        listModel.set(index++, file);
                     } else {
                        listModel.add(index++, file);
                     }
                  }
               }
            } catch(Exception e) {
               e.printStackTrace();
            }
            return true;
         }
      });
      
      //volumeSlider.setOrientation(SwingConstants.VERTICAL);
      volumeSlider.setToolTipText("Volume");
      volumeSlider.setOpaque(false);
      //volumeSlider.setMaximumSize(new Dimension(50, 30));
      volumeSlider.addChangeListener(new ChangeListener() {
         public void stateChanged(ChangeEvent e) {
            changeVolumeV2(volumeSlider.getValue());
         }
      });
      
      final JToolBar controls=new JToolBar("Controls");
      //controls.setFloatable(false);
      //controls.setLayout(new FlowLayout(FlowLayout.LEADING));
      controls.add(playButton);
      controls.add(pauseButton);
      controls.add(stopButton);
      controls.add(previousButton);
      controls.add(nextButton);
      controls.add(repeatButton);
      //controls.add(new JLabel(new ImageIcon("Volume24.gif")));
      controls.add(volumeSlider);
      
      //controls.add(visualEffect);
      /*final JFrame veFrame=new JFrame();
      veFrame.add(visualEffect);
      veFrame.pack();
      veFrame.setVisible(true);*/
      
      final JButton addButton=new JButton(new ImageIcon("Open24.gif"));
      final JButton removeButton=new JButton(new ImageIcon("Delete24.gif"));
      final JButton moveUpButton=new JButton(new ImageIcon("Up24.gif"));
      final JButton moveDownButton=new JButton(new ImageIcon("Down24.gif"));
      
      addButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            final JFileChooser jfc=new JFileChooser();
            jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            jfc.setFileFilter(
               new FileNameExtensionFilter("Midi Files(*.mid)", "mid"));
            final int ret=jfc.showOpenDialog(mainFrame);
            if(ret==JFileChooser.APPROVE_OPTION) {
               open(jfc.getSelectedFile());
            }
         }
         
      });
      removeButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            //final int[] indices=playList.getSelectedIndices();
            final int index=playList.getSelectedIndex();
            if(index<playList.getModel().getSize() && index>=0) {
               final DefaultListModel model=(DefaultListModel) playList.getModel();
               model.remove(index);
               if(index==currentIndex) currentIndex=-1;
            }
         }
      });
      moveUpButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            final int index=playList.getSelectedIndex();
            if(index<playList.getModel().getSize() && index>0) {
               final DefaultListModel model=(DefaultListModel) playList.getModel();
               final Object obj=model.remove(index);
               model.insertElementAt(obj, index-1);
               if(currentIndex==index) currentIndex--;
               else if(currentIndex==index-1) currentIndex++;
            }
         }
      });
      moveDownButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            final int index=playList.getSelectedIndex();
            if(index<playList.getModel().getSize()-1 && index>=0) {
               final DefaultListModel model=(DefaultListModel) playList.getModel();
               final Object obj=model.remove(index);
               model.insertElementAt(obj, index+1);
               if(currentIndex==index) currentIndex++;
               else if(currentIndex==index+1) currentIndex--;
            }
         }
      });
      
      /*addButton.setPreferredSize(new Dimension(24, 24));
      removeButton.setPreferredSize(new Dimension(24, 24));
      moveUpButton.setPreferredSize(new Dimension(24, 24));
      moveDownButton.setPreferredSize(new Dimension(24, 24));*/
      
      addButton.setToolTipText("Add File To List");
      removeButton.setToolTipText("Remove From List");
      moveUpButton.setToolTipText("Move Up");
      moveDownButton.setToolTipText("Move Down");
      
      final JToolBar progressPane=new JToolBar();
      progressPane.setFloatable(false);
      //progressPane.setBorder(BorderFactory.createLineBorder(Color.black));
      //progressPane.setLayout(new BorderLayout());
      progressPane.add(progress, BorderLayout.CENTER);
      progressPane.add(status, BorderLayout.EAST);
      
      final JToolBar devicePane=new JToolBar();
      //devicePane.setLayout(new BorderLayout());
      devicePane.add(new JLabel("Output Device: "), BorderLayout.WEST);
      devicePane.add(deviceList, BorderLayout.CENTER);
      //devicePane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
      
      final JToolBar playListControls=new JToolBar();
      playListControls.setFloatable(false);
      playListControls.add(addButton);
      playListControls.add(removeButton);
      playListControls.add(moveUpButton);
      playListControls.add(moveDownButton);
      playListControls.add(new JLabel(" "));
      playListControls.add(visualEffect);
      playListControls.add(new JLabel(" "));
      
      final JPanel playPane=new JPanel();
      playPane.setLayout(new BorderLayout());
      playPane.add(new JScrollPane(playList), BorderLayout.CENTER);
      playPane.add(playListControls, BorderLayout.NORTH);
      
      final JPanel display=new JPanel();
      display.setLayout(new BorderLayout());
      display.add(devicePane, BorderLayout.NORTH);
      display.add(playPane, BorderLayout.CENTER);
      display.add(progressPane, BorderLayout.SOUTH);
      
      final Container content=mainFrame.getContentPane();
      content.setLayout(new BorderLayout());
      //content.setLayout(new BoxLayout(content, BoxLayout.PAGE_AXIS));
      progress.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
      content.add(display, BorderLayout.CENTER);
      content.add(controls, BorderLayout.SOUTH);
      //status.setBorder(BorderFactory.createLoweredBevelBorder());
      
      /*for(Component c: content.getComponents()) {
         ((JComponent)c).setAlignmentX(Component.LEFT_ALIGNMENT);
      }*/
      mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      mainFrame.pack();
      mainFrame.addWindowListener(new WindowAdapter() {
         public void windowClosed(WindowEvent e) {
            System.err.println("closed");
         }
         public void windowClosing(WindowEvent e) {
            System.err.println("closing");
            if(sequencer.isOpen()) sequencer.close();
            if(outDevice.isOpen()) outDevice.close();
         }
      });
      mainFrame.setVisible(true);
      
      open(new File("liz_et4.mid"));
   }

   //[ old =================================================================================
   public static void play2(final File midiFile) throws Exception {
      final Sequence sequence=MidiSystem.getSequence(midiFile);
      final Sequencer sequencer=MidiSystem.getSequencer(false);
      sequencer.open();
      sequencer.setSequence(sequence);

      final Synthesizer synthesizer=MidiSystem.getSynthesizer();
      synthesizer.open();
      final Transmitter seqTransmitter=sequencer.getTransmitter();
      final Receiver synthReceiver=synthesizer.getReceiver();
      seqTransmitter.setReceiver(synthReceiver);

      sequencer.start();
   }
   public static void play1(final File midiFile) throws Exception {
      final Sequence sequence=MidiSystem.getSequence(midiFile);
      final Sequencer sequencer=MidiSystem.getSequencer();
      sequencer.open();
      sequencer.setSequence(sequence);
      
      sequencer.start();
   }

}
