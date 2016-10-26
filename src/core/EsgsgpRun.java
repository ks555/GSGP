package core;


import java.io.IOException;
import java.util.ArrayList;

import core.MIndividual;

import java.io.File;
import java.io.FileWriter;

public class EsgsgpRun extends GsgpRun {

	private static final long serialVersionUID = 7L;

	protected double mutationStep;
	protected boolean boundedMutation;
	protected boolean buildIndividuals;
	protected ArrayList<Population> populations;
	protected MPopulation mpopulation;
	protected int numPrograms;
	protected MIndividual currentMBest;
	protected double minDistance;
	
	File file = new File("results/results.txt");
	File file2 = new File("results/population.txt");
	File OutputsFile = new File("results/mindividuals/outputs.txt");

	public EsgsgpRun(Data data) {
		super(data);		
	}

	protected void initialize() {
		populations = new ArrayList <Population>();
		mpopulation = new MPopulation();
		numPrograms = 2;
		minDistance=0.99;
		
		for (int i = 0; i <  numPrograms; i++) {
			
			super.initialize();
			
			populations.add(population);
			
		}
		for (int i = 0; i < population.getSize(); i++) {
			
			//build the Mindividuals using the individuals from the initialized populations
			MIndividual mindividual = new MIndividual();
			
			for (int j = 0; j < numPrograms; j++) {
				
				if (j==0){
					mindividual.addProgramAtIndex(populations.get(j).getIndividual(i),j);
				}
				else{
					
					//for j > 0, check distance of expression from all expressions previously added to mindividual.
					//parameters are 'candidate' individual and j, the index it is to be added to mindividual at
					//double distance = mindividual.calcDistances(populations.get(j).getIndividual(i), j);
					Individual ind = populations.get(j).getIndividual(i);
					boolean check = mindividual.checkRatios(ind,  j);
					while(check){	
						//if distance is too small, replace ind with a new program
						ind = grow(this.getMaximumDepth());
						ind.evaluate(data);
						//distance=mindividual.calcDistances(ind, j);	
						check = mindividual.checkRatios(ind,  j);
						System.out.println(check);
					}
					mindividual.addProgramAtIndex(ind,j);
				}
			}
			
			//add ratio check here, set min distance to 0, can adjust the above distance check stuff if 
			//we switch to the ratio check
			
			mindividual.evaluate(data);
			mpopulation.addIndividual(mindividual);			
			
		}	
		
		updateCurrentMBest();
		//!!!printing Generation 0 but will be listed as Generation 1 as generation has already been incremented
		printMPopState();
	}

	public void printFirstGen(){
		for (int i = 0; i < mpopulation.getSize(); i++) {
			//print to each mindividual output2, (population.txt)
			try {
				mpopulation.getMIndividual(i).output2(0, file2);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	
	public void evolve(int numberOfGenerations) throws IOException {
		
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
					boolean check = newIndividual.checkRatios();
					//check distances between programs in newIndividual, keep reselecting mp2 and redoing crossover until distances are high enough
					while(check){
						mp2 = selectMParent();
						newIndividual = applyStandardCrossover(mp1, mp2);
						check = newIndividual.checkRatios();
					}				
					newIndividual.setMp2(mp2);
					
				}
				// apply mutation
				else {
					newIndividual = applyStandardMutation(mp1);
					boolean check = newIndividual.checkRatios();
					while(check){
						newIndividual = applyStandardMutation(mp1);
						check = newIndividual.checkRatios();
					}
				}
				newIndividual.setMp1(mp1);
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
						//evaluate each program in the individual
						newIndividual.getProgram(i).evaluate(data);
						
					}
					
					
					newIndividual.evaluate(data);
				}
				//System.out.println("Adding to offspring " +newIndividual.getId()+ " train theta " +  newIndividual.getTrainingTheta());
				offspring.addIndividual(newIndividual);
			}

			mpopulation = selectSurvivors(offspring);
			
			updateCurrentMBest();
			printMPopState();
			output();
			currentGeneration++;
		}
	}
	
	protected void printMPopState() {
		if (printAtEachGeneration) {
			System.out.println("\nGeneration:\t\t" + currentGeneration);
			System.out.printf("Training Theta:\t\t%.2f\nUnseen Theta:\t\t%.2f\nReconstructed Training Error:"
					+ "\t\t%.2f\nReconstructed Unseen Error:\t\t%.2f\nTraining Error Program 1:\t\t%.2f\nTraining Error Program 2:\t\t%.2f\n",
					currentMBest.getTrainingTheta(), currentMBest.getUnseenTheta(), currentMBest.getReconTrainingError(),
					currentMBest.getReconUnseenError(),currentMBest.getProgram(0).getTrainingError(),currentMBest.getProgram(1).getTrainingError());
			
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
		//System.out.println("Tournament get best");
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
		if (bestParent.getTrainingTheta() < bestNewIndividual.getTrainingTheta()) {
			
			bestOverall = bestParent;
		}
		// the best overall is in the offspring population
		else {
			bestOverall = bestNewIndividual;
			//System.out.println("Best  new" +bestNewIndividual.getTrainingError());
		}

		survivors.addIndividual(bestOverall);
		try {
			bestOverall.output2(currentGeneration, file2);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for (int i = 0; i < newIndividuals.getSize(); i++) {
			if (newIndividuals.getMIndividual(i).getId() != bestOverall.getId()) {
				survivors.addIndividual(newIndividuals.getMIndividual(i));
				//print to new individual to output2, population
				try {
					newIndividuals.getMIndividual(i).output2(currentGeneration, file2);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}

		return survivors;
	}
	

	protected void updateCurrentMBest() {
		
		currentMBest = mpopulation.getBestM();
	}


   protected void createOutputFile()throws IOException{
	      
	      // creates the file
	      file.createNewFile();
	      file2.createNewFile();
	      OutputsFile.createNewFile();
	   // creates a FileWriter Object
	      FileWriter writer = new FileWriter(file); 
	      // Writes the content to the file
	      writer.write("CurrentRun,Generation,NumGens,NumRuns,popSize,trainingTheta,UnseenTheta,reconTrainError,reconTrainingError,reconUnseenError"); 
	      writer.flush();
	      writer.close();
	   // creates a FileWriter Object
	      FileWriter writer2 = new FileWriter(file2); 
	      // Writes the content to the file
	      writer2.write("CurrentRun,Generation,ID,p1,p2,P1Training,P1Unseen,P2Training,P2Unseen,trainingTheta,UnseenTheta,reconTrainingError,reconUnseenError,LowestDistance"); 
	      writer2.flush();
	      writer2.close();
	   // creates a FileWriter Object
	      FileWriter writer3 = new FileWriter(OutputsFile); 
	      // Writes the content to the file
	      writer3.write("CurrentRun,ID,ExpressionNum,TrainingErrorVector0,"); 
	      for(int i=1;i<data.getTrainingData().length;i++){
	    	  writer3.write("TrainingErrorVector"+i+",");
	      }
	      writer3.write("TrainingOutputVector,");
	      for(int i=1;i<data.getTrainingData().length;i++){
	    	  writer3.write("TrainingOutputVector"+i+",");
	      }
	      writer3.flush();
	      writer3.close();
	   }	
	

   protected void output()throws IOException{

	      FileWriter writer = new FileWriter(file,true); 
	      // Writes the content to the file
	      writer.write("\n"+Main.CURRENTRUN+","+currentGeneration+","+Main.NUMBER_OF_GENERATIONS+","+Main.NUMBER_OF_RUNS+
	    		  ","+populationSize+currentMBest.getTrainingTheta()+","+currentMBest.getUnseenTheta()+","+currentMBest.getReconTrainingError()+
	    		  ","+currentMBest.getReconUnseenError()+
	    		  ","+currentBest.getDepth() + ","+","+applyDepthLimit+","+maximumDepth+
	    		  ","+crossoverProbability); 
	      writer.flush();
	      writer.close();	      
	   
   }
   

	
	
	// ##### get's and set's from here on #####
	public MIndividual getCurrentMBest() {
		return currentMBest;
	}
	public double getMutationStep() {
		return mutationStep;
	}
	public File getOutputsFileName() {
		return OutputsFile;
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
