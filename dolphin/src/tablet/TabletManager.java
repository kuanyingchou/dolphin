package tablet;

import java.awt.Dimension;
import java.awt.Toolkit;

import cello.tablet.*; //: needs jtablet-sdk-v0.9.5

public class TabletManager {
   
   private volatile boolean isOpened=false;
   
   private final java.util.List<TabletListener> listeners
      =new java.util.ArrayList<TabletListener>();
   public void addTabletListener(TabletListener lis) {
      listeners.add(lis);
   }
   private static final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize(); //>>>
   
   public TabletManager() {
      
   }
   
   public void open() {
      new Thread(new Runnable() {
         public void run() {
            JTablet tablet=null;
            try {
               tablet=new JTablet();
            } catch(JTabletException e) {
               e.printStackTrace();
            }
            JTabletCursor cursor=null;
            TabletEvent lastEvent=new TabletEvent(0, 0, 0, 0);
            isOpened=true;
            while(isOpened) {
               try {
                  tablet.poll();
               } catch(JTabletException ex) {
                  ex.printStackTrace();
               }
               if(tablet.hasCursor()/*cursor != tablet.getCursor()*/) {
                  //System.err.print(cursor == tablet.getCursor());
                  cursor=tablet.getCursor();
                  //System.err.println("hi?");
                  final int b=cursor.getData(JTabletCursor.DATA_BUTTONS);
                  final int x=cursor.getData(JTabletCursor.DATA_X);
                  final int y=screen.height-cursor.getData(JTabletCursor.DATA_Y);
                  final int p=cursor.getPressure();
                  final TabletEvent event=new TabletEvent(x, y, b, p);
                  if(x!=lastEvent.getX() || y!=lastEvent.getY()) {
                     for(TabletListener lis: listeners) {
                        lis.tabletMoved(event);
                     }
                  }
                  if(b!=lastEvent.getButton()) {
                     if(b==0) {
                        for(TabletListener lis: listeners) {
                           lis.tabletReleased(event);
                        }   
                     } else {
                        for(TabletListener lis: listeners) {
                           lis.tabletPressed(event);
                        }   
                     }
                  }
                  
                  //System.err.printf("in loop, (%d, %d, %d)%n", x, y, p);
                  lastEvent=event;
               }

               try {
                  Thread.yield();
                  Thread.sleep(10);
               } catch(Exception e) {}
            }      
         }
      }).start();
      
   }
   public void close() {
      isOpened=false;
   }
   
   public static void main(String[] args) {
      final TabletManager tm=new TabletManager();
      tm.addTabletListener(new TabletListener() {

         @Override
         public void tabletMoved(TabletEvent e) {
            System.err.println(e);
         }

         @Override
         public void tabletPressed(TabletEvent e) {
            System.err.println(e);
         }

         @Override
         public void tabletReleased(TabletEvent e) {
            System.err.println(e);
         }
         
      });
      tm.open();
   }
}
