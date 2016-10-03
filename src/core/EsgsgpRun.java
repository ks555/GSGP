package core;

import programElements.Addition;
import programElements.Constant;
import programElements.LogisticFunction;
import programElements.Multiplication;
import programElements.Subtraction;
import utils.Utils;

import java.util.ArrayList;

import core.MIndividual;


public class EsgsgpRun extends GpRun {

	private static final long serialVersionUID = 7L;

	protected double mutationStep;
	protected boolean boundedMutation;
	protected boolean buildIndividuals;
	protected ArrayList<Population> populations;
	protected MPopulation mpopulation;
	protected int numPrograms;
	protected MIndividual currentMBest;

	public EsgsgpRun(Data data) {
		super(data);
		
	}


	protected void initialize() {
		populations = new ArrayList <Population>();
		mpopulation = new MPopulation();
		numPrograms = 2;
		
		for (int i = 0; i <  2; i++) {
			
			super.initialize();
			
			populations.add(population);
			
		}
		for (int i = 0; i < population.getSize(); i++) {
			
			//build the Mindividuals using the individuals from the initialized populations
			MIndividual mindividual = new MIndividual();
			
			for (int j = 0; j < numPrograms; j++) {
				mindividual.addProgramAtIndex(populations.get(j).getIndividual(i),j);			
							
			}
			mpopulation.addIndividual(mindividual);		
			
		}
//		///!!!check this
//		for (int i = 0; i < population.getSize(); i++) {
//			mpopulation.getMIndividual(i).evaluate(data);
//		}
//
//		updateCurrentBest();
//		
		applyDepthLimit = false;
		mutationStep = 1.0;
		boundedMutation = false;
		//boundedMutation = true;
		//buildIndividuals = true;
		buildIndividuals = false;
		
		
	}

	public void evolve(int numberOfGenerations) {

		// evolve for a given number of generations
		while (currentGeneration <= numberOfGenerations) {
			MPopulation offspring = new MPopulation();

			// generate a new offspring population
			while (offspring.getSize() < population.getSize()) {
				MIndividual mp1, newIndividual;
				mp1 = selectMParent();
//				System.out.println(mp1.getProgram(0).getTrainingError());
//				System.out.println(mp1.getProgram(1).getTrainingError());
//				System.out.println(mp1.getTrainingError());
				// apply crossover to parents selected by tournament selection
				if (randomGenerator.nextDouble() < crossoverProbability) {
					MIndividual mp2 = selectMParent();
					newIndividual = applyStandardCrossover(mp1, mp2);
				}
				// apply mutation
				else {
					newIndividual = applyStandardMutation(mp1);
				}

				/*
				 * add the new individual to the offspring population if its
				 * depth is not higher than the maximum (applicable only if the
				 * depth limit is enabled)
				 */
				boolean flag = false;
				for (int i=0; i<numPrograms; i++){
					if(newIndividual.getProgram(i).getDepth()>maximumDepth){
						flag=true;
					}
				}
				if (applyDepthLimit && flag) {
					newIndividual = mp1;
				} else {
					for (int i=0; i<numPrograms; i++){
						newIndividual.getProgram(i).evaluate(data);
					}
				}
				offspring.addIndividual(newIndividual);
			}

			population = selectSurvivors(offspring);
			updateCurrentMBest();
			printState();
			currentGeneration++;
		}
	}
	
	// tournament selection
	
	protected MIndividual selectMParent() {
		MPopulation tournamentPopulation = new MPopulation();
		
		int tournamentSize = (int) (0.05 * population.getSize());
		
		for (int i = 0; i < tournamentSize; i++) {
			int index = randomGenerator.nextInt(population.getSize());
			//System.out.println(index);
			tournamentPopulation.addIndividual(mpopulation.getMIndividual(index));

		}
		return tournamentPopulation.getBestM();
	}	
	
	protected MIndividual applyStandardCrossover(MIndividual mp1, MIndividual mp2) {
		MIndividual offspring = new MIndividual();
		
		//choose random program from each individual as 'parents' of each program.
		//Repeat of numPrograms of MIndividual
		//add each 'offspring' to the final offspring (an MIndividual)
		for(int i=0; i<numPrograms; i++){
			
			int rand = randomGenerator.nextInt(numPrograms);
			Individual p1 =  mp1.getProgram(rand);
			rand =  randomGenerator.nextInt(numPrograms);
			Individual p2 = mp2.getProgram(rand);
				
			if (buildIndividuals) {
				offspring.addProgramAtIndex(buildCrossoverIndividual(p1, p2), i);
			}
			else {
				offspring.addProgramAtIndex(buildCrossoverSemantics(p1, p2),i);
			}					
		}	
		return offspring;
	}

	
	protected Individual buildCrossoverIndividual(Individual p1, Individual p2) {

		Individual offspring = new Individual();

		offspring.addProgramElement(new Addition());
		offspring.addProgramElement(new Multiplication());

		// copy first parent to offspring
		for (int i = 0; i < p1.getSize(); i++) {
			offspring.addProgramElement(p1.getProgramElementAtIndex(i));
		}

		// create a random tree
		int maximumInitialDepth = 6;
		Individual randomTree = grow(maximumInitialDepth);

		offspring.addProgramElement(new LogisticFunction());
		// copy random tree to offspring
		for (int i = 0; i < randomTree.getSize(); i++) {
			offspring.addProgramElement(randomTree.getProgramElementAtIndex(i));
		}

		offspring.addProgramElement(new Multiplication());
		offspring.addProgramElement(new Subtraction());
		offspring.addProgramElement(new Constant(1.0));

		offspring.addProgramElement(new LogisticFunction());
		// copy random tree to offspring
		for (int i = 0; i < randomTree.getSize(); i++) {
			offspring.addProgramElement(randomTree.getProgramElementAtIndex(i));
		}

		// copy second parent to offspring
		for (int i = 0; i < p2.getSize(); i++) {
			offspring.addProgramElement(p2.getProgramElementAtIndex(i));
		}

		offspring.calculateDepth();
		return offspring;
	}

	protected Individual buildCrossoverSemantics(Individual p1, Individual p2) {

		Individual offspring = new Individual();

		// create a random tree and evaluate it
		int maximumInitialDepth = 6;
		Individual randomTree = grow(maximumInitialDepth);
		randomTree.evaluate(data);

		// build training data semantics
		double[] parent1TrainingSemantics = p1.getTrainingDataOutputs();
		double[] parent2TrainingSemantics = p2.getTrainingDataOutputs();
		double[] randomTreeTrainingSemantics = randomTree.getTrainingDataOutputs();
		double[] offspringTrainingSemantics = buildCrossoverOffspringSemantics(parent1TrainingSemantics,
				parent2TrainingSemantics, randomTreeTrainingSemantics);
		offspring.setTrainingDataOutputs(offspringTrainingSemantics);

		// build unseen data semantics
		double[] parent1UnseenSemantics = p1.getUnseenDataOutputs();
		double[] parent2UnseenSemantics = p2.getUnseenDataOutputs();
		double[] randomTreeUnseenSemantics = randomTree.getUnseenDataOutputs();
		double[] offspringUnseenSemantics = buildCrossoverOffspringSemantics(parent1UnseenSemantics,
				parent2UnseenSemantics, randomTreeUnseenSemantics);
		offspring.setUnseenDataOutputs(offspringUnseenSemantics);

		// calculate size and depth
		offspring.setSizeOverride(true);
		offspring.setComputedSize(calculateCrossoverOffspringSize(p1, p2, randomTree));
		offspring.setDepth(calculateCrossoverOffspringDepth(p1, p2, randomTree));

		return offspring;
	}

	protected double[] buildCrossoverOffspringSemantics(double[] parent1Semantics, double[] parent2Semantics,
			double[] randomTreeSemantics) {
		double[] offspringSemantics = new double[parent1Semantics.length];
		for (int i = 0; i < offspringSemantics.length; i++) {
			double randomTreeValue = Utils.logisticFunction(randomTreeSemantics[i]);
			offspringSemantics[i] = (parent1Semantics[i] * randomTreeValue)
					+ ((1.0 - randomTreeValue) * parent2Semantics[i]);
		}
		return offspringSemantics;
	}

	protected int calculateCrossoverOffspringSize(Individual p1, Individual p2, Individual randomTree) {
		return p1.getSize() + p2.getSize() + randomTree.getSize() * 2 + 5;
	}

	protected int calculateCrossoverOffspringDepth(Individual p1, Individual p2, Individual randomTree) {
		int largestParentDepth = Math.max(p1.getDepth(), p2.getDepth());
		// "+ 1" because of the bounding function
		return Math.max(largestParentDepth + 2, randomTree.getDepth() + 3 + 1);
	}

	protected MIndividual applyStandardMutation(MIndividual mp) {
		MIndividual offspring = new MIndividual();
		for(int i=0; i<mp.getNumPrograms(); i++){
			if (buildIndividuals) {
				offspring.addProgramAtIndex(buildMutationIndividual(mp.getProgram(i)),i);
			} else {
				offspring.addProgramAtIndex(buildMutationSemantics(mp.getProgram(i)),i);
			}
		}
		
		return offspring;
	}

	protected Individual buildMutationIndividual(Individual p) {

		Individual offspring = new Individual();
		offspring.addProgramElement(new Addition());

		// copy parent to offspring
		for (int i = 0; i < p.getSize(); i++) {
			offspring.addProgramElement(p.getProgramElementAtIndex(i));
		}

		offspring.addProgramElement(new Multiplication());
		offspring.addProgramElement(new Constant(mutationStep));
		offspring.addProgramElement(new Subtraction());

		// create 2 random trees
		int maximumInitialDepth = 6;
		Individual randomTree1 = grow(maximumInitialDepth);
		Individual randomTree2 = grow(maximumInitialDepth);

		if (boundedMutation) {
			offspring.addProgramElement(new LogisticFunction());
		}
		// copy random tree 1 to offspring
		for (int i = 0; i < randomTree1.getSize(); i++) {
			offspring.addProgramElement(randomTree1.getProgramElementAtIndex(i));
		}

		if (boundedMutation) {
			offspring.addProgramElement(new LogisticFunction());
		}
		// copy random tree 2 to offspring
		for (int i = 0; i < randomTree2.getSize(); i++) {
			offspring.addProgramElement(randomTree2.getProgramElementAtIndex(i));
		}

		offspring.calculateDepth();
		return offspring;
	}

	protected Individual buildMutationSemantics(Individual p) {

		Individual offspring = new Individual();

		// create 2 random trees and evaluate them
		int maximumInitialDepth = 6;
		Individual randomTree1 = grow(maximumInitialDepth);
		Individual randomTree2 = grow(maximumInitialDepth);
		randomTree1.evaluate(data);
		randomTree2.evaluate(data);

		// build training data semantics
		double[] parentTrainingSemantics = p.getTrainingDataOutputs();
		double[] randomTree1TrainingSemantics = randomTree1.getTrainingDataOutputs();
		double[] randomTree2TrainingSemantics = randomTree2.getTrainingDataOutputs();
		double[] offspringTrainingSemantics = buildMutationOffspringSemantics(parentTrainingSemantics,
				randomTree1TrainingSemantics, randomTree2TrainingSemantics);
		offspring.setTrainingDataOutputs(offspringTrainingSemantics);

		// build unseen data semantics
		double[] parentUnseenSemantics = p.getUnseenDataOutputs();
		double[] randomTree1UnseenSemantics = randomTree1.getUnseenDataOutputs();
		double[] randomTree2UnseenSemantics = randomTree2.getUnseenDataOutputs();
		double[] offspringUnseenSemantics = buildMutationOffspringSemantics(parentUnseenSemantics,
				randomTree1UnseenSemantics, randomTree2UnseenSemantics);
		offspring.setUnseenDataOutputs(offspringUnseenSemantics);

		// calculate size and depth
		offspring.setSizeOverride(true);
		offspring.setComputedSize(calculateMutationOffspringSize(p, randomTree1, randomTree2));
		offspring.setDepth(calculateMutationOffspringDepth(p, randomTree1, randomTree2));

		return offspring;
	}

	protected double[] buildMutationOffspringSemantics(double[] parentSemantics, double[] randomTree1Semantics,
			double[] randomTree2Semantics) {
		double[] offspringSemantics = new double[parentSemantics.length];
		for (int i = 0; i < offspringSemantics.length; i++) {
			double value1 = randomTree1Semantics[i];
			double value2 = randomTree2Semantics[i];
			if (boundedMutation) {
				value1 = Utils.logisticFunction(value1);
				value2 = Utils.logisticFunction(value2);
			}
			offspringSemantics[i] = parentSemantics[i] + (mutationStep * (value1 - value2));
		}
		return offspringSemantics;
	}

	protected int calculateMutationOffspringSize(Individual parent, Individual randomTree1, Individual randomTree2) {
		return parent.getSize() + randomTree1.getSize() + randomTree2.getSize() + 4;
	}

	protected int calculateMutationOffspringDepth(Individual parent, Individual randomTree1, Individual randomTree2) {
		int largestRandomTreeDepth = Math.max(randomTree1.getDepth(), randomTree2.getDepth());
		return Math.max(largestRandomTreeDepth + 3, parent.getDepth() + 1);
	}	
	
	// keep the best overall + all the remaining offsprings
	protected MPopulation selectSurvivors(MPopulation newIndividuals) {
		MPopulation survivors = new MPopulation();
		MIndividual bestParent = mpopulation.getBestM();
		MIndividual bestNewIndividual = newIndividuals.getBestM();
		MIndividual bestOverall;
		// the best overall is in the current population
		if (bestParent.getTrainingError() < bestNewIndividual.getTrainingError()) {
			bestOverall = bestParent;
		}
		// the best overall is in the offspring population
		else {
			bestOverall = bestNewIndividual;
		}

		survivors.addIndividual(bestOverall);
		for (int i = 0; i < newIndividuals.getSize(); i++) {
			if (newIndividuals.getMIndividual(i).getId() != bestOverall.getId()) {
				survivors.addIndividual(newIndividuals.getMIndividual(i));
			}
		}
		return survivors;
	}
	

	protected void updateCurrentMBest() {
		currentMBest = mpopulation.getBestM();
	}

	// ##### get's and set's from here on #####

	public double getMutationStep() {
		return mutationStep;
	}

	public void setMutationStep(double mutationStep) {
		this.mutationStep = mutationStep;
	}

	public void setBoundedMutation(boolean boundedMutation) {
		this.boundedMutation = boundedMutation;
	}

	public void setBuildIndividuals(boolean buildIndividuals) {
		this.buildIndividuals = buildIndividuals;
	}
}
