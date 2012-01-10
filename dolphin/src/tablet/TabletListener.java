package tablet;

public interface TabletListener {
   public void tabletMoved(TabletEvent e);
   public void tabletPressed(TabletEvent e);
   public void tabletReleased(TabletEvent e);
}
