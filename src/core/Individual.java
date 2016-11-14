package core;

import java.io.File;
import java.io.FileWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

import programElements.Constant;
import programElements.InputVariable;
import programElements.Operator;
import programElements.ProgramElement;
import programElements.Terminal;

public class Individual implements Serializable {

	private static final long serialVersionUID = 7L;

	protected static long nextId;

	protected long id;
	protected ArrayList<ProgramElement> program;
	protected int depth;
	protected double trainingError, unseenError;
	protected double[] trainingDataOutputs, unseenDataOutputs;
	protected double[] trainingErrorVector, unseenErrorVector;

	protected int evaluateIndex;
	protected int maximumDepthAchieved;
	protected int depthCalculationIndex;
	protected int printIndex;

	protected boolean sizeOverride;
	protected int computedSize;
	protected double maxError;
	
	
	public Individual() {
		program = new ArrayList<ProgramElement>();
		id = getNextId();
		maxError=100;
	}

	protected static long getNextId() {
		return nextId++;
	}

	public void evaluate(Data data) {
		evaluateOnTrainingData(data);
		evaluateOnUnseenData(data);
		evaluateErrorVectorOnTrainingData(data);
		evaluateErrorVectorOnUnseenData(data);
	}

	public double[] evaluateOnTrainingData(Data data) {
		double[][] trainingData = data.getTrainingData();
		if (sizeOverride == false) {
			trainingDataOutputs = evaluate(trainingData);
		}
		trainingError = calculateRMSE(trainingData, trainingDataOutputs);
		//System.out.println("trainin output id " +getId()+" " +Arrays.toString(getTrainingDataOutputs()));
		return trainingDataOutputs;
	}

	public double[] evaluateOnUnseenData(Data data) {
		double[][] unseenData = data.getUnseenData();
		if (sizeOverride == false) {
			unseenDataOutputs = evaluate(unseenData);
		}
		unseenError = calculateRMSE(unseenData, unseenDataOutputs);
		return unseenDataOutputs;
	}

	public double[] evaluate(double[][] data) {
		double[] outputs = new double[data.length];
		for (int i = 0; i < outputs.length; i++) {
			evaluateIndex = 0;
			outputs[i] = evaluateInner(data[i]);
		}
		return outputs;
	}
	//Used when build individual is true
	protected double evaluateInner(double[] dataInstance) {
		if (program.get(evaluateIndex) instanceof InputVariable) {
			InputVariable inputVariable = (InputVariable) program.get(evaluateIndex);
			return inputVariable.getValue(dataInstance);
		} else if (program.get(evaluateIndex) instanceof Constant) {
			Constant constant = (Constant) program.get(evaluateIndex);
			return constant.getValue();
		} else {
			Operator operator = (Operator) program.get(evaluateIndex);
			double[] arguments = new double[operator.getArity()];
			for (int i = 0; i < arguments.length; i++) {
				evaluateIndex++;
				arguments[i] = evaluateInner(dataInstance);
			}
			return operator.performOperation(arguments);
		}
	}

	protected double calculateRMSE(double[][] data, double[] outputs) {
		double errorSum = 0.0;
		for (int i = 0; i < data.length; i++) {
			double target = data[i][data[0].length - 1];
			//System.out.println(target);
			errorSum += Math.pow(outputs[i] - target, 2.0);
		}
		return Math.sqrt(errorSum / data.length);
	}

	//ADDED BY KRISTEN
	//Methods for calculating error vectors
	
	//calculates training error vector
	//If size override set to false, output vector of offspring must be calculated
	//otherwise, it has already been set during crossover / mutation based on parent semantics
	public double[] evaluateErrorVectorOnTrainingData(Data data) {
		double[][] trainingData = data.getTrainingData();
		if (sizeOverride == false) {
			trainingDataOutputs = evaluate(trainingData);
		}
		trainingErrorVector = calculateErrorVector(trainingData, trainingDataOutputs);
		return trainingErrorVector;
	}
	//ADDED BY KRISTEN
	//calculates unseen error vector
	//If size override set to false, output vector of offspring must be calculated
	//otherwise, it has already been set during crossover / mutation based on parent semantics
	public double[] evaluateErrorVectorOnUnseenData(Data data) {
		double[][] unseenData = data.getUnseenData();
		if (sizeOverride == false) {
			unseenDataOutputs = evaluate(unseenData);
		}
		unseenErrorVector = calculateErrorVector(unseenData, unseenDataOutputs);
		return unseenErrorVector;
	}
	
	//ADDED BY KRISTEN
	protected double[] calculateErrorVector(double[][] data, double[] outputs) {
		double[] errorVector=new double[data.length];
		for (int i = 0; i < data.length; i++) {
			errorVector[i] = outputs[i]-data[i][data[0].length - 1];			
		}
		return errorVector;
	}
	//ADDED BY KRISTEN
	
	protected boolean checkMaxError() {
		boolean flag = false;
		
		for (int i = 0; i < trainingErrorVector.length; i++) {
			if(Math.abs(trainingErrorVector[i])>maxError){
				flag=true;
			};			
		}
		return flag;
	}
	
	public Individual deepCopy() {
		Individual newIndividual = new Individual();
		for (int i = 0; i < program.size(); i++) {
			newIndividual.program.add(program.get(i));
		}
		newIndividual.setDepth(depth);
		return newIndividual;
	}

	// The resulting copy is: [0, exclusionZoneStart[ + ]exclusionZoneEnd, N-1]
	public Individual selectiveDeepCopy(int exclusionZoneStartIndex, int exclusionZoneEndIndex) {
		Individual newIndividual = new Individual();
		for (int i = 0; i < exclusionZoneStartIndex; i++) {
			newIndividual.program.add(program.get(i));
		}
		for (int i = exclusionZoneEndIndex + 1; i < program.size(); i++) {
			newIndividual.program.add(program.get(i));
		}
		return newIndividual;
	}

	public void calculateDepth() {
		maximumDepthAchieved = 0;
		depthCalculationIndex = 0;
		calculateDepth(0);
		depth = maximumDepthAchieved;
	}

	protected void calculateDepth(int currentDepth) {
		if (program.get(depthCalculationIndex) instanceof Operator) {
			Operator currentOperator = (Operator) program.get(depthCalculationIndex);
			for (int i = 0; i < currentOperator.getArity(); i++) {
				depthCalculationIndex++;
				calculateDepth(currentDepth + 1);
			}
		} else {
			if (currentDepth > maximumDepthAchieved) {
				maximumDepthAchieved = currentDepth;
			}
		}
	}

	public int countElementsToEnd(int startingIndex) {
		if (program.get(startingIndex) instanceof Terminal) {
			return 1;
		} else {
			Operator operator = (Operator) program.get(startingIndex);
			int numberOfElements = 1;
			for (int i = 0; i < operator.getArity(); i++) {
				numberOfElements += countElementsToEnd(startingIndex + numberOfElements);
			}
			return numberOfElements;
		}
	}

	public void addProgramElement(ProgramElement programElement) {
		program.add(programElement);
	}

	public void addProgramElementAtIndex(ProgramElement programElement, int index) {
		program.add(index, programElement);
	}

	public void removeProgramElementAtIndex(int index) {
		program.remove(index);
	}

	public ProgramElement getProgramElementAtIndex(int index) {
		return program.get(index);
	}

	public void setProgramElementAtIndex(ProgramElement programElement, int index) {
		program.set(index, programElement);
	}

	public void print() {
		
		if (sizeOverride == true) {
			System.out.println(" [Individual not constructed]");
		} else {
			printIndex = 0;
			printInner();
		}
	}

	protected void printInner() {
		if (program.get(printIndex) instanceof Terminal) {
			System.out.print(" " + program.get(printIndex));
		} else {
			System.out.print(" (");
			System.out.print(program.get(printIndex));
			Operator currentOperator = (Operator) program.get(printIndex);
			for (int i = 0; i < currentOperator.getArity(); i++) {
				printIndex++;
				printInner();
			}
			System.out.print(")");
		}
	}

	// ##### get's and set's from here on #####

	public double getTrainingError() {
		return trainingError;
	}

	public double getUnseenError() {
		return unseenError;
	}

	public double[] getTrainingDataOutputs() {
		return trainingDataOutputs;
	}

	public double[] getUnseenDataOutputs() {
		return unseenDataOutputs;
	}

	public double[] getTrainingErrorVector() {
		return trainingErrorVector;
	}

	public double[] getUnseenErrorVector() {
		return unseenErrorVector;
	}
	public long getId() {
		return id;
	}

	public int getSize() {
		if (sizeOverride) {
			return computedSize;
		} else {
			return program.size();
		}
	}

	public int getDepth() {
		return depth;
	}
	
	//!!!!!ADDED BY KS
	public ProgramElement getValue(int i){
		return program.get(i);
	}

	public ArrayList<ProgramElement> getProgram() {
		return program;
	}

	public void setSizeOverride(boolean sizeOverride) {
		this.sizeOverride = sizeOverride;
	}

	public void setComputedSize(int computedSize) {
		this.computedSize = computedSize;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}

	public void setTrainingDataOutputs(double[] trainingDataOutputs) {
		this.trainingDataOutputs = trainingDataOutputs;
	}

	public void setUnseenDataOutputs(double[] unseenDataOutputs) {
		this.unseenDataOutputs = unseenDataOutputs;
	}
}
