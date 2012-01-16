importPackage(java.lang);
importPackage(Packages.model);

function createScale() {
   var part=dp.getScore().get(0);
   part.add(new Note(60));
   part.add(new Note(62));
   part.add(new Note(64));
   part.add(new Note(65));
   part.add(new Note(67));
   part.add(new Note(69));
   part.add(new Note(71));
   part.add(new Note(72));
}
createScale();
