package core;

import java.io.Serializable;
import java.util.ArrayList;

import programElements.Constant;
import programElements.InputVariable;
import programElements.Operator;


public class MIndividual implements Serializable {

	private static final long serialVersionUID = 7L;
	
	protected  int numPrograms=2;

	protected static long nextId;

	protected long id;
	protected ArrayList<Individual> programs;
	protected int depth;
	protected double[] trainingErrors, unseenErrors;
	protected double trainingErrorSum,unseenErrorSum;
	protected double[][] trainingDataOutputs, unseenDataOutputs;

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
//nc
	protected static long getNextId() {
		return nextId++;
	}
	
	//nc 
	public void evaluate(Data data) {
		evaluateOnTrainingData(data);
		evaluateOnUnseenData(data);
	}
	//nc
	public double[][] evaluateOnTrainingData(Data data) {
		double[][] trainingData = data.getTrainingData();
		double[] trainingErrors = new double[numPrograms];
		if (sizeOverride == false) {
			trainingDataOutputs = evaluate(trainingData);
			
		}
		for (int i = 0; i < numPrograms; i++){
			trainingErrors[i] = calculateRMSE(trainingData, trainingDataOutputs[i]);
		}
		return trainingDataOutputs;
	}
	//for each program, calculate unseenDataOutputs and unseenError
	public double[][] evaluateOnUnseenData(Data data) {
		double[][] unseenData = data.getUnseenData();
		if (sizeOverride == false) {
			
			unseenDataOutputs = evaluate(unseenData);
				
		}
		for (int i = 0; i < numPrograms; i++) {
			unseenErrors[i] = calculateRMSE(unseenData, unseenDataOutputs[i]);
		}
		
		return unseenDataOutputs;
	}
	//
	public double[][] evaluate(double[][] data) {
		
		double[][]outputs = new double[numPrograms][data.length];
		for (int i = 0; i < outputs.length; i++) {
			for(int j = 0; j < data.length; j++){
				evaluateIndex = 0;
				
				outputs[i][j] = evaluateInner(data[j],programs.get(i));
			}

		}
		return outputs;
	}
	protected double evaluateInner(double[] dataInstance, Individual program ) {
		
		
		if (program.getValue(evaluateIndex) instanceof InputVariable) {
			InputVariable inputVariable = (InputVariable) program.getValue(evaluateIndex);
			return inputVariable.getValue(dataInstance);
		} else if (program.getValue(evaluateIndex) instanceof Constant) {
			Constant constant = (Constant) program.getValue(evaluateIndex);
			return constant.getValue();
		} else {
			Operator operator = (Operator) program.getValue(evaluateIndex);
			double[] arguments = new double[operator.getArity()];
			for (int i = 0; i < arguments.length; i++) {
				evaluateIndex++;
				arguments[i] = evaluateInner(dataInstance,program);
			}
			return operator.performOperation(arguments);
		}
	}

	protected double calculateRMSE(double[][] data, double[] outputs) {
		double errorSum = 0.0;
		for (int i = 0; i < data.length; i++) {
			double target = data[i][data[0].length - 1];
			errorSum += Math.pow(outputs[i] - target, 2.0);
		}
		return Math.sqrt(errorSum / data.length);
	}

//	public Individual deepCopy() {
//		Individual newIndividual = new Individual();
//		for (int i = 0; i < program.size(); i++) {
//			newIndividual.program.add(program.get(i));
//		}
//		newIndividual.setDepth(depth);
//		return newIndividual;
//	}
//
//	// The resulting copy is: [0, exclusionZoneStart[ + ]exclusionZoneEnd, N-1]
//	public Individual selectiveDeepCopy(int exclusionZoneStartIndex, int exclusionZoneEndIndex) {
//		Individual newIndividual = new Individual();
//		for (int i = 0; i < exclusionZoneStartIndex; i++) {
//			newIndividual.program.add(program.get(i));
//		}
//		for (int i = exclusionZoneEndIndex + 1; i < program.size(); i++) {
//			newIndividual.program.add(program.get(i));
//		}
//		return newIndividual;
//	}
//
//	public void calculateDepth() {
//		maximumDepthAchieved = 0;
//		depthCalculationIndex = 0;
//		calculateDepth(0);
//		depth = maximumDepthAchieved;
//	}
//
//	protected void calculateDepth(int currentDepth) {
//		if (program.get(depthCalculationIndex) instanceof Operator) {
//			Operator currentOperator = (Operator) program.get(depthCalculationIndex);
//			for (int i = 0; i < currentOperator.getArity(); i++) {
//				depthCalculationIndex++;
//				calculateDepth(currentDepth + 1);
//			}
//		} else {
//			if (currentDepth > maximumDepthAchieved) {
//				maximumDepthAchieved = currentDepth;
//			}
//		}
//	}
//
//	public int countElementsToEnd(int startingIndex) {
//		if (program.get(startingIndex) instanceof Terminal) {
//			return 1;
//		} else {
//			Operator operator = (Operator) program.get(startingIndex);
//			int numberOfElements = 1;
//			for (int i = 0; i < operator.getArity(); i++) {
//				numberOfElements += countElementsToEnd(startingIndex + numberOfElements);
//			}
//			return numberOfElements;
//		}
//	}
//
//	public void addProgramElement(ProgramElement programElement) {
//		program.add(programElement);
//	}
//
//	public void addProgramElementAtIndex(ProgramElement programElement, int index) {
//		program.add(index, programElement);
//	}
	
	public void addProgramAtIndex(Individual program, int index) {
		programs.add(index, program);
}
	
//
//	public void removeProgramElementAtIndex(int index) {
//		program.remove(index);
//	}
//
//	public ProgramElement getProgramElementAtIndex(int index) {
//		return program.get(index);
//	}
//
//	public void setProgramElementAtIndex(ProgramElement programElement, int index) {
//		program.set(index, programElement);
//	}
//
//	public void print() {
//		if (sizeOverride == true) {
//			System.out.println(" [Individual not constructed]");
//		} else {
//			printIndex = 0;
//			printInner();
//		}
//	}
//
//	protected void printInner() {
//		if (program.get(printIndex) instanceof Terminal) {
//			System.out.print(" " + program.get(printIndex));
//		} else {
//			System.out.print(" (");
//			System.out.print(program.get(printIndex));
//			Operator currentOperator = (Operator) program.get(printIndex);
//			for (int i = 0; i < currentOperator.getArity(); i++) {
//				printIndex++;
//				printInner();
//			}
//			System.out.print(")");
//		}
//	}
//
//	// ##### get's and set's from here on #####
	


	public double getTrainingError() {
		trainingErrorSum = 0;
		for(int i=0;i<numPrograms;i++){
			trainingErrorSum+=this.getProgram(i).getTrainingError();			
		}
		return trainingErrorSum/numPrograms;
	}

	public double getUnseenError() {
		
		for(int i=0;i<numPrograms;i++){
			unseenErrorSum+=this.getProgram(i).getUnseenError();
			
		}
		return unseenErrorSum/numPrograms;
	}

	public double[][] getTrainingDataOutputs() {
		return trainingDataOutputs;
	}

//	public double[] getUnseenDataOutputs() {
//		return unseenDataOutputs;
//	}
//
		public long getId() {
			return id;
		}
	
//	public int getSize() {
//		if (sizeOverride) {
//			return computedSize;
//		} else {
//			return program.size();
//		}
//	}
//	public int getDepth() {
//		return depth;
//	}
	public  int getNumPrograms() {
		return numPrograms;
	}
//
	public Individual getProgram(int index) {
		Individual program = programs.get(index);
		return program;
	}
	
//nc
//	public void setSizeOverride(boolean sizeOverride) {
//		this.sizeOverride = sizeOverride;
//	}
//
//	public void setComputedSize(int computedSize) {
//		this.computedSize = computedSize;
//	}

	public void setDepth(int depth) {
		this.depth = depth;
	}


//	public void setTrainingDataOutputs(double[][] trainingDataOutputs) {
//		this.trainingDataOutputs = trainingDataOutputs;
//	}
//
//	public void setUnseenDataOutputs(double[] unseenDataOutputs) {
//		this.unseenDataOutputs = unseenDataOutputs;
//	}
}
