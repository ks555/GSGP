package core;

import java.io.Serializable;
import java.util.ArrayList;



public class MIndividual implements Serializable {

	private static final long serialVersionUID = 7L;
	
	protected  int numPrograms=2;

	protected static long nextId;

	protected long id;
	protected ArrayList<Individual> programs;
	protected int depth;
	protected double trainingErrorAvg,unseenErrorAvg;
	

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
	
	public void evaluate() {
		evaluateTrainingError();
		evaluateUnseenError();
	}
	
	public void evaluateTrainingError() {
		int temp = 0;
		for(int i=0;i<numPrograms;i++){
			temp+=this.getProgram(i).getTrainingError();			
		}
		trainingErrorAvg=temp/2;
	}
	
	public void evaluateUnseenError() {
		int temp = 0;
		for(int i=0;i<numPrograms;i++){
			temp+=this.getProgram(i).getTrainingError();			
		}
		unseenErrorAvg=temp/2;
	}

	public void addProgramAtIndex(Individual program, int index) {
		programs.add(index, program);
}
	

//	// ##### get's and set's from here on #####
	


	public double getTrainingError() {

		return trainingErrorAvg;
	}

	public double getUnseenError() {
		
		return unseenErrorAvg;
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
	
}
