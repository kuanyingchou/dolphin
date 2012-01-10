package gui;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.TransferHandler;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import api.model.ScoreChange;
import api.model.ScoreChangeListener;
import api.util.Util;

import view.ScoreView;


public class ViewPane extends JPanel/*JInternalFrame*/ /*implements ScoreChangeListener*/ {
   final ScoreView scoreView;
   static int viewID=0;
   final MainFrame mainFrame;
   //private final JLabel statusLabel=new JLabel(""); //>>>
   
   public ViewPane(ScoreView s, MainFrame mf) {
      if(s==null || mf==null) throw new IllegalArgumentException();
      scoreView=s;
      mainFrame=mf;
      //scoreView.score.addScoreChangeListener(this); //>>>
      
//      setTitle("score#"+scoreView.score.id+": view#"+viewID++);
//      setResizable(true);
//      setIconifiable(true);
//      setClosable(true);
//      setFrameIcon(new ImageIcon("sheet.png"));
//      setMaximizable(true);
      setLayout(new BorderLayout());
      final JScrollPane sPane=new JScrollPane(scoreView);
      sPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
      sPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
      
//      final KeyListener[] lis=scoreView.getKeyListeners();
//      for(int i=0; i < lis.length; i++) {
//         System.err.println(lis[i]);
//                                     //.removeKeyListener(lis[i]);
//      }
      add(sPane, BorderLayout.CENTER);
      sPane.setCorner(ScrollPaneConstants.LOWER_RIGHT_CORNER, new Mover(scoreView));
      s.validate();
      
      //add(statusLabel, BorderLayout.SOUTH); //>>>
      
      //[ for drag & drop
      setTransferHandler(new TransferHandler() {
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
            //final JList list=(JList) info.getComponent();
            //final DefaultListModel listModel=(DefaultListModel) list.getModel();
            //final JList.DropLocation dl=(JList.DropLocation) info.getDropLocation();
            final JPanel frame=(JPanel) info.getComponent();
            //final DefaultListModel listModel=(DefaultListModel) frame.getModel();
            final TransferHandler.DropLocation dl=(TransferHandler.DropLocation) info.getDropLocation();
            //final boolean insert=dl.isInsert();
            //int index=dl.getIndex();

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
                  mainFrame.importMidiFromFile(file);
               }
            } catch(Exception e) {
               e.printStackTrace();
            }
            return true;
         }
      });
      
      //setSize(400, 300);
      
//      addInternalFrameListener(new InternalFrameAdapter() {
//         public void internalFrameActivated(InternalFrameEvent e) {
//            //mainFrame.partPane.setModel(scoreView.score);
//            scoreView.requestFocusInWindow();
//         }
//         @Override
//         public void internalFrameDeactivated(InternalFrameEvent e) {
//            //mainFrame.partPane.setModel(null);
//         }
//      });
      //addInternalFrameListener(mainFrame.tabBar);
      
   }
//   @Override
//   public void scoreChanged(ScoreChange e) {
//      statusLabel.setText(e.toString());  
//   }
}
class Mover extends JButton {
   private final ScoreView sheet;
   public Mover(ScoreView s) {
      sheet=s;
      setBorderPainted(false);
      setIcon(Util.getImageIcon("images/Find16.gif"));
      setToolTipText("Drag to Move");
      
      final MouseAdapter ma=new MouseAdapter() {
         int lastX, lastY;
         
         public void mouseEntered(MouseEvent e) {
            setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
         }
         public void mousePressed(MouseEvent e) {
            lastX=e.getX();
            lastY=e.getY();
         }
         public void mouseDragged(MouseEvent e) {
            final Rectangle visibleRect=sheet.getVisibleRect();
            visibleRect.x-=(e.getX()-lastX);
            visibleRect.y-=(e.getY()-lastY);
            sheet.scrollRectToVisible(visibleRect);
            lastX=e.getX();
            lastY=e.getY();
            //System.err.println("hi?");
         }
         public void mouseReleased(MouseEvent e) {
            lastX=lastY=0;
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
         }
      };
      this.addMouseListener(ma);
      this.addMouseMotionListener(ma);
      
   }
   /*@Override
   public void paintComponent(Graphics g) {
      super.paintComponents(g);
      g.setColor(Color.red);
      g.drawRect(0, 0, getWidth(), getHeight());
      final ViewFrame vf=(ViewFrame)mainFrame.desktop.getSelectedFrame();
      if(vf==null) return;
      int sw=vf.sheet.getWidth();
   }*/
   }