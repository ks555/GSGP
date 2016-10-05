package core;


import java.util.ArrayList;

import core.MIndividual;


public class EsgsgpRun extends GsgpRun {

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
//				System.out.println("Initialized Individual " + i + ":" + j + " ID: " + populations.get(j).getIndividual(i).getId()
//						+ "\n" + "Training Error: " + populations.get(j).getIndividual(i).trainingError 
//						+ "\n" + "Unseen Error: " + populations.get(j).getIndividual(i).unseenError);					
			}
			mindividual.evaluate();
			mpopulation.addIndividual(mindividual);			
//			System.out.println("Mindividual Error Averages for ID: " + mindividual.getId()+"\nTraining Error " 
//			+ mindividual.getTrainingError() + "\nUnseen Error Total " + mindividual.getUnseenError()+"\n");
			
		}
	
		applyDepthLimit = false;
		mutationStep = 1.0;
		//boundedMutation = false;
		boundedMutation = true;
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
					//System.out.println("Offspring first program training error " +newIndividual.getProgram(0).getTrainingError());
					newIndividual.evaluate();
				}
				offspring.addIndividual(newIndividual);
				//System.out.println("offspring training " +newIndividual.getTrainingError()+"\n");
			}

			mpopulation = selectSurvivors(offspring);
			
			updateCurrentMBest();
			printMPopState();
			currentGeneration++;
		}
	}
	
	protected void printMPopState() {
		if (printAtEachGeneration) {
			System.out.println("\nGeneration:\t\t" + currentGeneration);
			System.out.printf("Training error:\t\t%.2f\nUnseen error:\t\t%.2f\n",
					currentMBest.getTrainingError(), currentMBest.getUnseenError());
		}
	}
	
	// tournament selection
	
	protected MIndividual selectMParent() {
		MPopulation tournamentPopulation = new MPopulation();
		
		int tournamentSize = (int) (0.05 * population.getSize());
		
		for (int i = 0; i < tournamentSize; i++) {
			int index = randomGenerator.nextInt((mpopulation.getSize()));
			
			tournamentPopulation.addIndividual(mpopulation.getMIndividual(index));

		}
		return tournamentPopulation.getBestM();
	}	
	
	protected MIndividual applyStandardCrossover(MIndividual mp1, MIndividual mp2) {
		MIndividual offspring = new MIndividual();
		if (buildIndividuals == false) {
			offspring.setSizeOverride(true);
		}
		//for each program in numPrograms
		//choose random program from mp1 and mp2 as 'parents' of the new program.
		//add each program 'offspring' to the final offspring (an MIndividual)
		for(int i=0; i<numPrograms; i++){
			
			int rand = randomGenerator.nextInt(numPrograms);
			Individual p1 =  mp1.getProgram(rand);
			rand =  randomGenerator.nextInt(numPrograms);
			Individual p2 = mp2.getProgram(rand);
			Individual pOffspring;
				
			if (buildIndividuals) {
				pOffspring = buildCrossoverIndividual(p1, p2);
				
				offspring.addProgramAtIndex(pOffspring, i);
			}
			else {
				pOffspring = buildCrossoverSemantics(p1, p2);
				
				offspring.addProgramAtIndex(pOffspring,i);
			}	
			
			
		}	
		//print
//		System.out.println("offspring ID: " +offspring.getId());
//		System.out.println("P1 training error " + mp1.getTrainingError());
//		System.out.println("P2 training error " + mp2.getTrainingError());
		
		
		
		return offspring;
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


	
	// keep the best overall + all the remaining offsprings
	protected MPopulation selectSurvivors(MPopulation newIndividuals) {
		MPopulation survivors = new MPopulation();
		MIndividual bestParent = mpopulation.getBestM();
		MIndividual bestNewIndividual = newIndividuals.getBestM();
		MIndividual bestOverall;
		
		

		// the best overall is in the current population
		if (bestParent.getTrainingError() < bestNewIndividual.getTrainingError()) {
			//System.out.println("Best parent" +bestParent.getTrainingError());
			bestOverall = bestParent;
		}
		// the best overall is in the offspring population
		else {
			bestOverall = bestNewIndividual;
			//System.out.println("Best  new" +bestNewIndividual.getTrainingError());
		}

		survivors.addIndividual(bestOverall);
		//System.out.println("Best overall" +bestOverall.getTrainingError());
		for (int i = 0; i < newIndividuals.getSize(); i++) {
			if (newIndividuals.getMIndividual(i).getId() != bestOverall.getId()) {
				survivors.addIndividual(newIndividuals.getMIndividual(i));
			}
		}
		//System.out.print("Survivors best: " + survivors.getBestM().getTrainingError());
		return survivors;
	}
	

	protected void updateCurrentMBest() {
		currentMBest = mpopulation.getBestM();
	}

	// ##### get's and set's from here on #####
	public MIndividual getCurrentMBest() {
		return currentMBest;
	}
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
