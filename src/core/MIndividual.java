package core;

import java.util.ArrayList;

import java.math.*;

public class MIndividual extends Individual {

	private static final long serialVersionUID = 7L;
	
	protected  int numPrograms=2;

	protected static long nextId;

	protected long id;
	protected ArrayList<Individual> programs;
	protected int depth;
	protected double trainingErrorAvg,unseenErrorAvg;
	protected double[][] trainingErrorVectors =  new double[numPrograms][];
	protected double [][] unseenErrorVectors =  new double[numPrograms][];
	protected double trainingTheta,unseenTheta;
	protected double reconTrainingError,reconUnseenError;

	protected int evaluateIndex;
	protected int maximumDepthAchieved;
	protected int depthCalculationIndex;
	protected int printIndex;
	

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
		
	}
		
	public void evaluateTrainingError() {
		double temp = 0;
		for(int i=0;i<numPrograms;i++){
			temp+=this.getProgram(i).getTrainingError();			
		}
		trainingErrorAvg=temp/2;

	}
	
	public void evaluateUnseenError() {
		double temp = 0;
		for(int i=0;i<numPrograms;i++){
			temp+=this.getProgram(i).getUnseenError();			
		}
		unseenErrorAvg=temp/2;
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
    
    
    public double calcDistances(Individual newInd, int j){

	        double [] joutputs = newInd.getTrainingDataOutputs();
	        double [] koutputs = this.getProgram(0).getTrainingDataOutputs();
	        double distance=calculateEuclideanDistance(koutputs, joutputs);
	        
	    	for (int k=1;k<j;k++){	
	    		
	    		koutputs = this.getProgram(k).getTrainingDataOutputs();    		
	    		double temp = calculateEuclideanDistance(koutputs, joutputs);
	    		if (temp<distance){
	    			distance=temp;
	    			}
	    		
	    	}
    	
    	
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
        	
	
	return distance;
}

	protected double calculateEuclideanDistance(double[] koutputs, double[] joutputs) {
		double sum = 0.0;
		for (int i = 0; i < koutputs.length; i++) {
			
			sum += Math.pow(koutputs[i] - joutputs[i], 2.0);
		
		}
		
		return Math.sqrt(sum);
	}
//	// ##### get's and set's from here on #####
	


	public double getTrainingError() {

		return trainingErrorAvg;
	}

	public double getUnseenError() {
		
		return unseenErrorAvg;
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

}
