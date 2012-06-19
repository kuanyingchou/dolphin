//run:
//java -classpath .:../../bin Run test_score_player.js JavaScript

importPackage(Packages.api.model);
importPackage(Packages.api.midi);

var Do=new Note(60);
var Re=new Note(62);
var Mi=new Note(64);

var part=new Part();
part.add(Do);
part.add(Re);
part.add(Mi);

var score=new Score();
score.add(part);

var player=new BasicScorePlayer();
player.play(score);
