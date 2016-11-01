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
	protected ArrayList<Individual> programs;
	protected int depth;
	protected double [] ratios;
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
		double[] reconstructedTrainingOutput = reconstructTrainingSemantics();
		double[] reconstructedUnseenOutput = reconstructUnseenSemantics();
		double[][] trainingData = data.getTrainingData();
		double[][] unseenData = data.getUnseenData();
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
		
		if (numPrograms == 2){
			dotProd = dot(errorVector[0], errorVector[1]);			
			normA = magnitude(errorVector[0]); 			
			normB = magnitude(errorVector[1]);
		}
		
		else{
			dotProd=0;
			normA=1;
			normB=1;
		}		

		trainingTheta = Math.acos(dotProd/(normA*normB));
		trainingTheta =Math.toDegrees(trainingTheta);

	}
	
	public void evaluateUnseenTheta(double[][] errorVector) {
		
		double dotProd;
		double normA;
		double normB;
		
		if (numPrograms == 2){
			
			dotProd = dot(errorVector[0], errorVector[1]);			
			normA = magnitude(errorVector[0]); 			
			normB = magnitude(errorVector[1]);

		}	
		else{
			dotProd=0;
			normA=1;
			normB=1;
		}
		
		unseenTheta = Math.acos(dotProd/(normA*normB));
		unseenTheta =Math.toDegrees(unseenTheta);
		
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
		    	 System.out.println(mp1.getId());
			      // Writes the content to the file
			      writer.write("\n"+Main.CURRENTRUN+","+currentGeneration+","+getId()+","+getId()+","+
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
		
		//!!WHY IS THIS NOT overwriting THE FIRST GENERATION??
		///!!!PUT THIS PRINTING IN A METHOD!
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
        minDistance =  1-1/(1+distance);
        
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
	    minDistance = 1-1/(1+distance);    	
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
		double[] programOneSemantics = getProgram(0).getTrainingDataOutputs();
		double[] programTwoSemantics = getProgram(1).getTrainingDataOutputs();
		calcRatios(programOneSemantics, programTwoSemantics);
	    boolean check=false;
		for (int i=0;i<ratios.length;i++){
			
			if (Math.abs(ratios[i])>2){
				
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
		double[] programOneSemantics = getProgram(0).getTrainingDataOutputs();
		//WHEN MORE THAN TWO EXPRESSIONS, THIS WILL BE DIFFERENT - GET OUTPUTS OF EACH EXPRESSION (UP TO INDEX J-1), PLUS OUT PUT OF IND
		double[] programTwoSemantics = ind.getTrainingDataOutputs();
		calcRatios(programOneSemantics, programTwoSemantics);
	    boolean check=false;
		for (int i=0;i<ratios.length;i++){
			
			if (Math.abs(ratios[i])>2){
				
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
	
	public double[] reconstructTrainingSemantics(){
		//!!!change these functions to deal with N expressions	
		double[] programOneSemantics = getProgram(0).getTrainingDataOutputs();
		double[] programTwoSemantics = getProgram(1).getTrainingDataOutputs();
		double[] reconstructedTrainingSemantics = new double[programOneSemantics.length];
		
		double k = calculateK();
		for (int i = 0; i < programOneSemantics.length; i++) {
			reconstructedTrainingSemantics[i] =  1/(1-k)*programOneSemantics[i]-k/(1-k)*programTwoSemantics[i];
		}		
		
		return reconstructedTrainingSemantics;
	}	
	
	public double[] reconstructUnseenSemantics(){
		//!!!change these functions to deal with N expressions
		double[] reconstructedUnseenSemantics = new double[unseenErrorVectors[0].length];	
		double[] programOneSemantics = getProgram(0).getUnseenDataOutputs();;
		double[] programTwoSemantics = getProgram(1).getUnseenDataOutputs();
		
		double k = calculateK();
		for (int i = 0; i < programOneSemantics.length; i++) {
			reconstructedUnseenSemantics[i] = 1/(1-k)*programOneSemantics[i]-1/(1-k)*programTwoSemantics[i];
		}			
		return reconstructedUnseenSemantics;
	}	
	
	public double calculateK(){
		double k;
		//get median
		if (ratios.length % 2 == 0)
		    k = ((double)ratios[ratios.length/2] + (double)ratios[ratios.length/2 - 1])/2;
		else
		    k = (double) ratios[ratios.length/2];
		return k;
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
