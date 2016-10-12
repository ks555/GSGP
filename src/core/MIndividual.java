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
	protected int minDistance;

	protected boolean sizeOverride;
	protected int computedSize;

	public MIndividual() {
		programs = new ArrayList <Individual>();
		id = getNextId();
		minDistance=100;
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
			//calc theta?
			
			trainingErrorVectors[i]=this.getProgram(i).evaluateErrorVectorOnTrainingData(data);
		}
		
		evaluateTrainingTheta(trainingErrorVectors);
		
//		if(trainingTheta<=0.07){
////			for(int i = 0;i<programs.get(0).getTrainingDataOutputs().length;i++){
////				System.out.println("First ind output " + i +": "+ programs.get(0).getTrainingDataOutputs()[i]);
////				System.out.println("Second ind: " + programs.get(1).getTrainingDataOutputs()[i]);
////							
////			}
//			
//			System.out.println("Id " + programs.get(0).getId() + " and ID " + programs.get(1).getId() + " are the same");
//		}
//		
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
		double dotProd=0;
		double normA=0;
		double normB=0;
		
		if (numPrograms == 2){
			dotProd = dot(errorVector[0], errorVector[1]);
			
			normA = magnitude(errorVector[0]); 			
			normB = magnitude(errorVector[1]);
//			
//			System.out.println("Dot product "+dotProd);
//			System.out.println("Norm A "+normA);
//			System.out.println("Norm B "+normB);
//			System.out.println("temp "+dotProd/(normA*normB)+"\n");
		}	
		

		trainingTheta = Math.acos(dotProd/(normA*normB));
	}
	
	public void evaluateUnseenTheta(double[][] errorVector) {

		
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

//    // return the Euclidean distance between this and that
//    public double distanceTo(Vector that) {
//        if (this.length() != that.length())
//            throw new IllegalArgumentException("Dimensions disagree");
//        return this.minus(that).magnitude();
//    }
//
//    // return this + that
//    public Vector plus(Vector that) {
//        if (this.length() != that.length())
//            throw new IllegalArgumentException("Dimensions disagree");
//        Vector c = new Vector(n);
//        for (int i = 0; i < n; i++)
//            c.data[i] = this.data[i] + that.data[i];
//        return c;
//    }	
    public boolean calcDistances(Individual newInd, int i){
        double [] ioutputs = newInd.getTrainingDataOutputs();
        boolean flag = true;
    	for (int j=i-1;j<numPrograms;j++){		
    		double [] joutputs = this.getProgram(j).getTrainingDataOutputs();
    		if (calculateEuclideanDistance(joutputs, ioutputs)<minDistance){
    			flag=false;
    		}
    	}
    	return flag;
    }

	protected double calculateEuclideanDistance(double[] joutputs, double[] ioutputs) {
		double sum = 0.0;
		for (int i = 0; i < joutputs.length; i++) {
			
			sum += Math.pow(joutputs[i] - ioutputs[i], 2.0);
		}
		return Math.sqrt(sum / joutputs.length);
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
