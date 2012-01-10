importPackage(java.lang);
importPackage(Packages.test);
var ca=new CA(30, 128, 128, 2, 2); //150, 30, 60
ca.setCell(ca.getLength()/2, 1);
ca.display();

for(i=0; i<128; i++) {
   ca.step();
   Thread.sleep(100);
}
