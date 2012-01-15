package api.midi;

import javax.sound.midi.Receiver;

import api.model.Path;
import api.model.Score;

public interface ScorePlayer {

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

   public abstract long getTickPosition();

   public abstract long getMicrosecondPosition();

   public abstract void setTickPosition(long pos);

   public abstract long getTickLength();

   public abstract void setTempoFactor(float tf);

   public abstract void addReceiver(Receiver rec);

   public abstract long getMicrosecondLength();

}