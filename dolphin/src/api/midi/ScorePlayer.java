package api.midi;

import java.util.ArrayList;
import java.util.List;

import javax.sound.midi.Receiver;

import api.model.Path;
import api.model.Score;

public abstract class ScorePlayer {

   //usage:
   //play: playing
   //pause: paused, not playing
   //play: playing
   //stop: not paused, not playing, stopped
   public abstract boolean isPlaying();

   public abstract boolean isStopped();

   //public void setScoreView(ScoreView s) { sv=s; }

   public abstract void play(Score s);

   public abstract Score getScore();

   public abstract void play(Score score, Path startPath);

   public abstract void pause();

   public abstract void stop();

   //public abstract long getTickPosition();

   public abstract long getMicrosecondPosition();
   
   public abstract void setMicrosecondPosition(long pos);

   //public abstract void setTickPosition(long pos);

   //public abstract long getTickLength();

   public abstract void setTempoFactor(float tf);

   public abstract void addReceiver(Receiver rec);

   public abstract long getMicrosecondLength();

   public abstract void setVolume(int partIndex, int volume);
   
   public abstract void setPan(int part, int pan);
   
   public abstract void reset();
   
   private final List<PlayerListener> playerListeners=new ArrayList<PlayerListener>();
   public void addPlayerListeners(PlayerListener lis) {
      playerListeners.add(lis);
   }
   public void clearPlayerListeners() {
      playerListeners.clear();
   }
   public void fireNotePlayered(Path path) {
      for (int i = 0; i < playerListeners.size(); i++) {
         playerListeners.get(i).notePlayed(path);
      }
   }
   public static interface PlayerListener {
      public void notePlayed(Path path);
   }
}