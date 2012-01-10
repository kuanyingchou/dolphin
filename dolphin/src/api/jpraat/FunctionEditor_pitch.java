package api.jpraat;

public class FunctionEditor_pitch {
   public Pitch data;
   boolean show=true;
   /* Pitch settings: *///>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
   //double floor=75, ceiling=500; //>>> default
   //double floor=200, ceiling=600; //>>> c4~c5
   //double floor=100, ceiling=500; //female
   public double floor=75, ceiling=300; //male
   //double floor=75, ceiling=600; //>>> general
   
   // enum kPitch_unit unit;
   // enum kTimeSoundAnalysisEditor_pitch_drawingMethod drawingMethod;
   // /* Advanced pitch settings: */
   // double viewFrom, viewTo;
   int method=1;
   public int veryAccurate=0;
   int maximumNumberOfCandidates=15;
   double silenceThreshold=0.03;
   double voicingThreshold=0.45;
   double octaveCost=0.01;
   double octaveJumpCost=0.35;
   double voicedUnvoicedCost=0.14;
   // struct { bool speckle; } picture;
};
