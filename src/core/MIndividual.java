package core;
import core.GpRun;
import java.util.ArrayList;
import java.util.Arrays;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.math.*;

public class MIndividual extends Individual {

	private static final long serialVersionUID = 7L;
	
	protected  int numPrograms=2;

	protected static long nextId;

	protected long id;
	protected double k;
	protected double w;
	protected ArrayList<Individual> programs;
	protected int depth;
	protected double [] ratios;
	protected double [] ratiosK;
	protected double [] ratiosW;
	protected double[][] trainingErrorVectors =  new double[numPrograms][];
	protected double [][] unseenErrorVectors =  new double[numPrograms][];
	protected double trainingTheta,unseenTheta;
	protected double reconTrainingError,reconUnseenError;
	protected double minDistance;

	protected int evaluateIndex;
	protected int maximumDepthAchieved;
	protected int depthCalculationIndex;
	protected int printIndex;
	
	protected MIndividual mp1;
	protected MIndividual mp2;
	protected boolean sizeOverride;
	protected int computedSize;

	public MIndividual() {
		programs = new ArrayList <Individual>();
		id = getNextId();	
	}

	protected static long getNextId() {
		return nextId++;
	}
	
	public void evaluate(Data data) {
		evaluateTrainingTheta(data);
		evaluateUnseenTheta(data);
		double[][] trainingData = data.getTrainingData();
		double[][] unseenData = data.getUnseenData();
		double[] reconstructedTrainingOutput = reconstructTrainingSemantics(trainingData);
		double[] reconstructedUnseenOutput = reconstructUnseenSemantics(unseenData);
		reconTrainingError=calculateRMSE(trainingData, reconstructedTrainingOutput);
		reconUnseenError=calculateRMSE(unseenData, reconstructedUnseenOutput);
	}	
	
	public void evaluateTrainingTheta(Data data) {
		//for each program, add error vector to trainingErrorVectors
		//calc theta using trainingErrorVectors			
		for(int i=0;i<numPrograms;i++){			
			trainingErrorVectors[i]=this.getProgram(i).evaluateErrorVectorOnTrainingData(data);		
		}
		
		evaluateTrainingTheta(trainingErrorVectors);		
	}
	
	public void evaluateUnseenTheta(Data data) {
		//for each program, add error vector to trainingErrorVectors
		//calc theta using trainingErrorVectors			
		for(int i=0;i<numPrograms;i++){
			//calc theta
			unseenErrorVectors[i]=this.getProgram(i).evaluateErrorVectorOnUnseenData(data);

		}
		
		evaluateUnseenTheta(unseenErrorVectors);
		
	}
	
	public void evaluateTrainingTheta(double[][] errorVector) {
		
		double dotProd;
		double normA;
		double normB;
		double[][] normalized=new double[numPrograms][];
		double tempTheta;
		trainingTheta=360;
		
		if (numPrograms == 2){
			dotProd = dot(errorVector[0], errorVector[1]);			
			normA = magnitude(errorVector[0]); 			
			normB = magnitude(errorVector[1]);
			trainingTheta = Math.acos(dotProd/(normA*normB));
		}
		else if (numPrograms == 3){

			normalized[0]=scalar(errorVector[0],1/magnitude(errorVector[0]));
			normalized[1]=scalar(minus(errorVector[1],scalar(normalized[0],dot(normalized[0],errorVector[1]))),1/magnitude(minus(errorVector[0],scalar(normalized[0],dot(normalized[0],errorVector[1])))));
			normalized[2]=scalar(minus(minus(errorVector[2],scalar(normalized[0],dot(normalized[0],errorVector[0]))),scalar(normalized[1],dot(normalized[1],errorVector[2]))),1/magnitude(minus(minus(errorVector[0],scalar(normalized[0],dot(normalized[0],errorVector[2]))),scalar(normalized[1],dot(normalized[1],errorVector[2])))));
			trainingTheta=Math.asin(dot(errorVector[2],normalized[2])/Math.sqrt(dot(errorVector[2],errorVector[2])));
			
			
			//this is for if we are checking all combos of expressions for all planes...
			//if use this, change planeVector to a vector of same length as errorVector and make it a property of the class		

//			double[][] planeVector=new double[errorVector.length-1][];
//			int index=0;
			

//			for(int i=0;i<numPrograms;i++){		
//				index=0;
//				for (int j=0;j<numPrograms;j++){
//					
//					if(j!=i){
//						planeVector[index]=errorVector[j];					
//						index++;
//					}			
//				}
//				normalized[0]=scalar(planeVector[0],1/magnitude(planeVector[0]));
//				normalized[1]=scalar(minus(planeVector[1],scalar(normalized[0],dot(normalized[0],planeVector[1]))),1/magnitude(minus(planeVector[0],scalar(normalized[0],dot(normalized[0],planeVector[1])))));
//				normalized[2]=scalar(minus(minus(errorVector[i],scalar(normalized[0],dot(normalized[0],errorVector[0]))),scalar(normalized[1],dot(normalized[1],errorVector[0]))),1/magnitude(minus(minus(errorVector[0],scalar(normalized[0],dot(normalized[0],planeVector[1]))),scalar(normalized[1],dot(normalized[1],errorVector[0])))));
//				tempTheta=Math.asin(dot(errorVector[i],normalized[2])/Math.sqrt(dot(errorVector[i],errorVector[i])));
//				if(tempTheta<trainingTheta){
//					trainingTheta=tempTheta;
//				}
//			}
		}		
		//equation for angle of w to a plane containing u,v
//		    #V1 = Normalize[v];
//		    #U1 = Normalize[u - (V1.u) V1];
//		    #W1 = Normalize[w - (w.V1) V1 - (w.U1) U1];
//		    #theta3 = asin(dot(w,W1)/sqrt(dot(w,w)))
		
		trainingTheta =Math.toDegrees(trainingTheta);

	}
	
	public void evaluateUnseenTheta(double[][] errorVector) {
		
		double dotProd;
		double normA;
		double normB;
		double[][] normalized=new double[numPrograms][];
		
		if (numPrograms == 2){
			
			dotProd = dot(errorVector[0], errorVector[1]);			
			normA = magnitude(errorVector[0]); 			
			normB = magnitude(errorVector[1]);
			unseenTheta = Math.acos(dotProd/(normA*normB));

		}	
		else if (numPrograms == 3){
			double[][] planeVector=new double[2][];
			int index=0;
			for(int i=0;i<numPrograms;i++){		
				index=0;
				for (int j=0;j<numPrograms;j++){
					
					if(j!=i){
						planeVector[index]=errorVector[j];					
						index++;
					}			
				}
				normalized[0]=scalar(errorVector[i],1/magnitude(errorVector[i]));
				normalized[1]=scalar(minus(planeVector[0],scalar(normalized[0],dot(normalized[0],planeVector[0]))),1/magnitude(minus(planeVector[0],scalar(normalized[0],dot(normalized[0],planeVector[0])))));
				normalized[2]=scalar(minus(minus(planeVector[1],scalar(normalized[0],dot(normalized[0],planeVector[1]))),scalar(normalized[1],dot(normalized[1],planeVector[1]))),1/magnitude(minus(minus(planeVector[1],scalar(normalized[0],dot(normalized[0],planeVector[1]))),scalar(normalized[1],dot(normalized[1],planeVector[1])))));
				unseenTheta=Math.asin(dot(planeVector[1],normalized[2])/Math.sqrt(dot(planeVector[1],planeVector[1])));
			
			
			}
		}		
		
		
		unseenTheta =Math.toDegrees(unseenTheta);
		
	}
		
    public double[] scalar(double[] vectorOne, double scalar) {
        double[] solution =new double[vectorOne.length];
        for (int i = 0; i < vectorOne.length; i++)
            solution[i] = vectorOne[i]*scalar;
        return solution;
      }
      
      public double[] minus(double[] vectorOne, double[] vectorTwo) {
      	 if (vectorOne.length != vectorTwo.length)
               throw new IllegalArgumentException("Dimensions disagree for angle calculation");
          double[] solution =new double[vectorOne.length];
          for (int i = 0; i < vectorOne.length; i++)
              solution[i] = vectorOne[i]-vectorTwo[i];
          return solution;
        }

	public void addProgramAtIndex(Individual program, int index) {
		programs.add(index, program);
	}
	
	public void print() {
		
		if (sizeOverride == true) {
			System.out.println("Best individual ID: " + this.getId());
			System.out.println("Best individual Training Error: " + this.getTrainingError());	
			System.out.println("Best individual Unseen Error: " + this.getUnseenError());
		} else {
//			printIndex = 0;
//			printInner();
		}
	}
	
	   protected void output2(int currentGeneration, File file2)throws IOException{
		   //"CurrentRun,Generation,ID,trainingTheta,UnseenTheta,reconTrainingError,reconUnseenError,LowestDistance"
		      FileWriter writer = new FileWriter(file2,true); 
		      
		      if (currentGeneration==0||mp1==null&&mp2==null){
			      // Writes the content to the file
			      writer.write("\n"+Main.CURRENTRUN+","+currentGeneration+","+getId()+","+","+
			    		  ","+getProgram(0).getTrainingError()+","+getProgram(0).getUnseenError()+","+getProgram(1).getTrainingError()+","+ getProgram(1).getUnseenError()+
			    		  ","+ getTrainingTheta()+","+getUnseenTheta()+","+getReconTrainingError()+
			    		  ","+getReconUnseenError()+","+minDistance);
			      writer.flush();
			      writer.close();	  
		      }
		      else if(mp2==null){
		    	 
			      // Writes the content to the file
			      writer.write("\n"+Main.CURRENTRUN+","+currentGeneration+","+getId()+","+mp1.getId()+","+
			    		  ","+getProgram(0).getTrainingError()+","+getProgram(0).getUnseenError()+","+getProgram(1).getTrainingError()+","+ getProgram(1).getUnseenError()+
			    		  ","+ getTrainingTheta()+","+getUnseenTheta()+","+getReconTrainingError()+
			    		  ","+getReconUnseenError()+","+minDistance);
			      writer.flush();
			      writer.close();	
		      }
		      else{
			      // Writes the content to the file
			      writer.write("\n"+Main.CURRENTRUN+","+currentGeneration+","+getId()+","+mp1.getId()+","+mp2.getId()+
			    		  ","+getProgram(0).getTrainingError()+","+getProgram(0).getUnseenError()+","+getProgram(1).getTrainingError()+","+ getProgram(1).getUnseenError()+
			    		  ","+getTrainingTheta()+","+getUnseenTheta()+","+getReconTrainingError()+
			    		  ","+getReconUnseenError()+","+minDistance);
			      writer.flush();
			      writer.close();	
		      }
		   
	}
	// Writes the training errors and training output of each individual to  file
	public void printVectors(int currentGeneration,File file) throws IOException{
		//System.out.println(outputsFile.getName());
		
		for(int i=0;i<numPrograms;i++){
			FileWriter writerOut = new FileWriter(file,true);
			writerOut.write("\n"+Main.CURRENTRUN+currentGeneration+","+getId()+
				","+i+","+ Arrays.toString(trainingErrorVectors[i]).replace("[","").replace("]", "")+","+Arrays.toString(getProgram(i).getTrainingDataOutputs()).replace("[","").replace("]", ""));
	      writerOut.flush();
	      writerOut.close();
		}
	     

		
	}

    
    //checks the distances of all the expressions in a MIndividual from each other, returns the min distance found.
    public double calcDistances(){

        double [] ioutputs = this.getProgram(0).getTrainingDataOutputs();
        double [] joutputs = this.getProgram(1).getTrainingDataOutputs();
        double distance=calculateEuclideanDistance(ioutputs, joutputs);
        
        for(int i=2; i<numPrograms; i++){
        	  for(int j=i + 1; j<numPrograms; j++){
        		  ioutputs = this.getProgram(i).getTrainingDataOutputs();
        		  joutputs = this.getProgram(j).getTrainingDataOutputs();
  	    		double temp = calculateEuclideanDistance(ioutputs, joutputs);
  	    		if (temp<distance){
  	    			distance=temp;
  	    			}
        	  }
        	}        	
        minDistance =  distance;
        
		return distance;
	}
    //overloaded method
    //checks distances of a new expression from the expressions already within the MIndividual. Returns the minimum distance
   public double calcDistances(Individual newInd, int j){

	        double [] joutputs = newInd.getTrainingDataOutputs();
	        double [] koutputs = this.getProgram(0).getTrainingDataOutputs();
	        double distance = calculateEuclideanDistance(koutputs, joutputs);
	        
	    	for (int k=1;k<j;k++){	
	    		
	    		koutputs = this.getProgram(k).getTrainingDataOutputs();    		
	    		double temp = calculateEuclideanDistance(koutputs, joutputs);
	    		if (temp<distance){
	    			distance=temp;
	    			}	    		
	    	}  
	    minDistance = distance;    	
   	return distance;
   }
   
   //calcs ratios between outputs of the two expressions.
   public void calcRatios(double[]oneSemantics,double[]twoSemantics){

		double[] ratiosTemp=new double[oneSemantics.length];
			
		for (int i=0;i<oneSemantics.length;i++){
			ratiosTemp[i]=oneSemantics[i]/twoSemantics[i];
		}
		Arrays.sort(ratiosTemp);
		ratios=ratiosTemp;
	}
   
   //checks ratios between outputs of the two expressions.
   public boolean checkRatios(){
		double[] programOneSemantics = getProgram(0).getTrainingErrorVector();
		double[] programTwoSemantics = getProgram(1).getTrainingErrorVector();
		calcRatios(programOneSemantics, programTwoSemantics);
	    boolean check=false;
		for (int i=0;i<ratios.length;i++){
			
			if (Math.abs(ratios[i])>=1.2||Math.abs(ratios[i])<=0.98){
				
			}
			else{
				check=true;
			}
		}
		
		return check;
	} 
   //checks ratios between outputs of the two expressions.
   //overloaded method, checks ratio of new expression to current expressions in MIndividual
   public boolean checkRatios(Individual ind, int j){
		double[] programOneSemantics = getProgram(0).getTrainingErrorVector();
		//WHEN MORE THAN TWO EXPRESSIONS, THIS WILL BE DIFFERENT - GET OUTPUTS OF EACH EXPRESSION (UP TO INDEX J-1), PLUS OUT PUT OF IND
		double[] programTwoSemantics = ind.getTrainingErrorVector();
		calcRatios(programOneSemantics, programTwoSemantics);
	    boolean check=false;
		for (int i=0;i<ratios.length;i++){
			
			if (Math.abs(ratios[i])>=1.2||Math.abs(ratios[i])<=0.98){
				
			}
			else{
				check=true;
			}
		}
		
		return check;
	} 
	protected double calculateEuclideanDistance(double[] koutputs, double[] joutputs) {
		double sum = 0.0;
		for (int i = 0; i < koutputs.length; i++) {
			
			sum += Math.pow(koutputs[i] - joutputs[i], 2.0);
		
		}
		
		return Math.sqrt(sum);
	}
	//method for calculating k before adding an expression to the mindividual	
	public double calculateK(Individual ind){
		
		//!!! for two expressions only
		double[] programOneSemantics = getProgram(0).getTrainingErrorVector();
		double[] programTwoSemantics = ind.getTrainingErrorVector();
		calcRatios(programOneSemantics, programTwoSemantics);
		
		//get median
		if (ratios.length % 2 == 0)
		   k = ((double)ratios[ratios.length/2] + (double)ratios[ratios.length/2 - 1])/2;
			
		else
		    k = (double) ratios[ratios.length/2];
		
		return k;
	}
	
	
	public double calculateK(double[][] trainingData){
		
		if (numPrograms==2){
			
			double[] programOneSemantics = getProgram(0).getTrainingErrorVector();
			double[] programTwoSemantics = getProgram(1).getTrainingErrorVector();
			calcRatios(programOneSemantics, programTwoSemantics);
			//get median
			if (ratios.length % 2 == 0)
			   k = ((double)ratios[ratios.length/2] + (double)ratios[ratios.length/2 - 1])/2;
				//k = (double)ratios[ratios.length/2];
			else
			    k = (double) ratios[ratios.length/2];
		}
		else{
			//!!! for three expressions only
			double[] programOneSemantics = getProgram(0).getTrainingErrorVector();
			double[] programTwoSemantics = getProgram(1).getTrainingErrorVector();
			double[] programThreeSemantics = getProgram(2).getTrainingErrorVector();
			calculateK(programOneSemantics, programTwoSemantics,programThreeSemantics,trainingData);
			//get median
			if (ratiosK.length % 2 == 0)
			   k = ((double)ratiosK[ratiosK.length/2] + (double)ratiosK[ratiosK.length/2 - 1])/2;
				//k = (double)ratios[ratios.length/2];
			else
			    k = (double) ratiosK[ratiosK.length/2];
		}
		
		return k;
	}
	
	public double[] reconstructTrainingSemantics(double[][] trainingData){
		//!!!change these functions to deal with N expressions	
		double[] reconstructedTrainingSemantics = new double[trainingErrorVectors[0].length];
		
		
		if (numPrograms==2){
			double[] programOneSemantics = getProgram(0).getTrainingDataOutputs();
			double[] programTwoSemantics = getProgram(1).getTrainingDataOutputs();			
			k = calculateK(trainingData);
			
			for (int i = 0; i < programOneSemantics.length; i++) {
				reconstructedTrainingSemantics[i] =  1/(1-k)*programOneSemantics[i]-k/(1-k)*programTwoSemantics[i];
			}
		}
		else{
			//calc k and w using median from each index
			//create array variable to hold k, one for w
			//use function to get medians
			//reconstruct
			//add to unseen too...how does this work with unseen?\
			double[] programOneSemantics = getProgram(0).getTrainingDataOutputs();
			double[] programTwoSemantics = getProgram(1).getTrainingDataOutputs();
			double[] programThreeSemantics = getProgram(2).getTrainingDataOutputs();
			w = calculateW(programOneSemantics,programTwoSemantics,programThreeSemantics,trainingData);
			k = calculateK(programOneSemantics,programTwoSemantics,programThreeSemantics,trainingData);
			for (int i = 0; i < programOneSemantics.length; i++) {
				reconstructedTrainingSemantics[i] =  1/((1-k)*(1-w))*programOneSemantics[i]-w/((1-k)*(1-w))*programTwoSemantics[i]
						-k/(1-k)*programThreeSemantics[i];
			}
						
			
		}
		return reconstructedTrainingSemantics;
	}	
	
	public double[] reconstructUnseenSemantics(double[][] unseenData){
		
		//!!!change these functions to deal with N expressions
		double[] reconstructedUnseenSemantics = new double[unseenErrorVectors[0].length];
		
		if (numPrograms==2){	
			double[] programOneSemantics = getProgram(0).getUnseenDataOutputs();;
			double[] programTwoSemantics = getProgram(1).getUnseenDataOutputs();
			k = calculateK(unseenData);
			for (int i = 0; i < programOneSemantics.length; i++) {
				reconstructedUnseenSemantics[i] = 1/(1-k)*programOneSemantics[i]-k/(1-k)*programTwoSemantics[i];
			}	
		}
		else{
			double[] programOneSemantics = getProgram(0).getUnseenDataOutputs();
			double[] programTwoSemantics = getProgram(1).getUnseenDataOutputs();
			double[] programThreeSemantics = getProgram(2).getUnseenDataOutputs();
			w = calculateW(programOneSemantics,programTwoSemantics,programThreeSemantics,unseenData);
			k = calculateK(programOneSemantics,programTwoSemantics,programThreeSemantics,unseenData);
			for (int i = 0; i < programOneSemantics.length; i++) {
				reconstructedUnseenSemantics[i] =  1/((1-k)*(1-w))*programOneSemantics[i]-w/((1-k)*(1-w))*programTwoSemantics[i]
						-k/(1-k)*programThreeSemantics[i];
			}
		}
		return reconstructedUnseenSemantics;
	}	

	//
	public double calculateK(double[] p1s,double[] p2s,double[] p3s,double[][] trainingData){
		
		int sum=0;
		for (int i=0;i<p1s.length-1;i++){
			for(int j=i+1;j<p1s.length;j++){
				sum++;
			}
		}
		ratiosK=new double[sum];
		int index = 0;
		for (int i=0;i<p1s.length-1;i++){
			for(int j=i+1;j<p1s.length;j++){
				double t1 = trainingData[i][trainingData[0].length - 1];
				double t2 = trainingData[j][trainingData[0].length - 1];
				
				ratiosK[index]=(p1s[i]*p2s[j]-p1s[j]*p2s[i]-p1s[i]*t2+p1s[j]*t1+p2s[i]*t2-p2s[j]*t1)/
						(p1s[i]*p3s[j]-p1s[j]*p3s[i]-p2s[i]*p3s[j]+p2s[j]*p3s[i]-p1s[i]*t2+p1s[j]*t1+p2s[i]*t2-p2s[j]*t1);
				index++;
			}			
		}
		Arrays.sort(ratiosK);
		if (ratiosK.length % 2 == 0)
			   k = ((double)ratiosK[ratiosK.length/2] + (double)ratiosK[ratiosK.length/2 - 1])/2;
				//k = (double)ratios[ratios.length/2];
			else
			    k = (double) ratiosK[ratiosK.length/2];
		
		return k;
	}
	
	public double calculateW(double[] p1s,double[] p2s,double[] p3s,double[][] trainingData){
		double w;
		int sum=0;
		for (int i=0;i<p1s.length-1;i++){
			for(int j=i+1;j<p1s.length;j++){
				sum++;
			}
		}
		ratiosW=new double[sum];
		int index = 0;
		for (int i=0;i<p1s.length-1;i++){
			for(int j=i+1;j<p1s.length;j++){
				
					double t1 = trainingData[i][trainingData[0].length - 1];
					double t2 = trainingData[j][trainingData[0].length - 1];
					
					ratiosW[index]=(p1s[i]*p3s[j]-p1s[j]*p3s[i]-p1s[i]*t2+p1s[j]*t1+p3s[i]*t2-p3s[j]*t1)/
							(p2s[i]*p3s[j]-p2s[j]*p3s[i]-p2s[i]*t2+p2s[j]*t1+p3s[i]*t2-p3s[j]*t1);
					index++;
			}			
		}
		Arrays.sort(ratiosW);
		if (ratiosW.length % 2 == 0)
			   w = ((double)ratiosW[ratiosW.length/2] + (double)ratiosW[ratiosW.length/2 - 1])/2;
			else
			    w = (double) ratiosW[ratiosW.length/2];
		return w;
	}
	protected double calculateRMSE(double[][] data, double[] outputs) {
		double errorSum = 0.0;
		for (int i = 0; i < outputs.length; i++) {
			double target = data[i][data[0].length - 1];
			errorSum += Math.pow(outputs[i] - target, 2.0);
		}
		return Math.sqrt(errorSum / data.length);
	}
	
    // return the inner product of vectors a and b
    public double dot(double[] vectorOne, double[] vectorTwo) {
//        if (vectorOne.length != vectorTwo.length)
//            throw new IllegalArgumentException("Dimensions disagree for angle calculation");
        double sum = 0.0;
        for (int i = 0; i < vectorOne.length; i++)
            sum = sum + (vectorOne[i] * vectorTwo[i]);
        return sum;
    }
    
    // return the Euclidean norm of this Vector
    public double magnitude(double[] vector) {
        return Math.sqrt(dot(vector,vector));
    }
    
  //  public String createStringFromArray()
//	// ##### get's and set's from here on #####
	
	public double getK(){
		
		return k;
	}
	
	public double getTrainingTheta() {
		
		return trainingTheta;
	}
	
	public double getUnseenTheta() {
		
		return unseenTheta;
	}

	public long getId() {
			return id;
		}
	

	public  int getNumPrograms() {
		return numPrograms;
	}

	public Individual getProgram(int index) {
		Individual program = programs.get(index);
		return program;
	}
	
	public double getReconTrainingError(){
		return reconTrainingError;
	}
	
	public double getReconUnseenError(){
		return reconUnseenError;
	}
	
	public void setSizeOverride(boolean sizeOverride) {
		this.sizeOverride = sizeOverride;
	}

	public void setMp1(MIndividual p1) {
		this.mp1 = p1;
	}
	public void setMp2(MIndividual p2) {
		this.mp2 = p2;
	}
}
