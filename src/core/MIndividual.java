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
			
			//for each program, print to outputs run, gen, i, EVT, EVU, OVT, OVU
			//!!!this is messy (file name in two places, doesnt print great) just for testing right now
			File outputs = new File("results/mindividuals/outputs.txt");
			
	        // Writes the content to the file
			//!!WHY IS THIS NOT WRITING THE FIRST GENERATION?? EVEN THOUGH THE TRY CLAUSE IS ENTERED???
	        try {
	    	  FileWriter writer = new FileWriter(outputs,true); 
	    	  //System.out.println("trainin output id " +getId()+" program " +i+" " +Arrays.toString(getProgram(i).getTrainingDataOutputs()));
			  writer.write("\n"+Main.CURRENTRUN+";"+getId()+
					  ";"+i+";"+ Arrays.toString(trainingErrorVectors[i])+";"+Arrays.toString(getProgram(i).getTrainingDataOutputs()));
		      writer.flush();
		      writer.close();	
		     
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		
		}
		
		evaluateTrainingTheta(trainingErrorVectors);		
	}
	
	public void evaluateUnseenTheta(Data data) {
		//for each program, add error vector to trainingErrorVectors
		//calc theta using trainingErrorVectors			
		for(int i=0;i<numPrograms;i++){
			//calc theta
			unseenErrorVectors[i]=this.getProgram(i).evaluateErrorVectorOnUnseenData(data);
			
			
			//for each program, print to outputs run, gen, i, EVT, EVU, OVT, OVU
			File outputs = new File("results/mindividuals/outputs.txt");
	      
//	        // Writes the content to the file
//	        try {
//	    	  FileWriter writer = new FileWriter(outputs,true); 
//			  writer.write(","+Arrays.toString(unseenErrorVectors[i])+","+Arrays.toString(this.getProgram(i).getUnseenDataOutputs()));
//		      writer.flush();
//		      writer.close();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}

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
		      
		      if (currentGeneration==0){
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
	    System.out.println(minDistance);
    	return distance;
    }
    
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
        System.out.println(minDistance);
		return distance;
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
		double k = getK(programOneSemantics,programTwoSemantics);
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
		double k = this.getK(programOneSemantics,programTwoSemantics);
		for (int i = 0; i < programOneSemantics.length; i++) {
			reconstructedUnseenSemantics[i] = 1/(1-k)*programOneSemantics[i]-1/(1-k)*programTwoSemantics[i];
		}			
		return reconstructedUnseenSemantics;
	}	
	
	public double getK(double[]oneSemantics,double[]twoSemantics){
		double[] ratios=new double[oneSemantics.length];
		double k;
		Arrays.sort(ratios);
		for (int i=0;i<oneSemantics.length;i++){
			ratios[i]=oneSemantics[i]/twoSemantics[i];
		}
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
