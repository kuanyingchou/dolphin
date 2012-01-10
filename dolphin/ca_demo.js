importPackage(java.lang);
importPackage(Packages.test);
importPackage(Packages.model);

function getPitch(cA) {
  var p = 0;
  for (i = 0; i < ca.getLength(); i++) {
    p = (2 * p + ca.getCell(i)) % 128;
  }
  return p;
}
var ca=new CA(30, 16, 32); //150, 30, 60
ca.setCell(ca.getLength()/2, 1);
ca.display();
var part=dp.getScore().get(0);
var cumulativePitch = 0;
for (j = 0; j < 128; j++) {
   var pitch = getPitch(ca);

   cumulativePitch = (cumulativePitch + pitch) % 12;

   var n = new Note(60+cumulativePitch%12);
   part.add(n);
   
   ca.step();
   Thread.sleep(100);
}