import api.model.*;
import api.midi.*;

class TestScorePlayer {
  public static void main(String[] args) {
    final Note noteDo=new Note(60);
    final Note noteRe=new Note(62);
    final Note noteMi=new Note(64);

    final Part part=new Part();
    part.add(noteDo);
    part.add(noteRe);
    part.add(noteMi);

    final Score score=new Score();
    score.add(part);

    final ScorePlayer player=new BasicScorePlayer();
    player.play(score);
  }
}
