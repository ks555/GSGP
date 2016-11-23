package core;


import java.io.IOException;
import java.util.ArrayList;

import core.MIndividual;

import java.io.File;
import java.io.FileWriter;

public class EsgsgpRun extends GpRun {

	private static final long serialVersionUID = 7L;

	protected double mutationStep;
	protected boolean boundedMutation;
	protected boolean buildIndividuals;
	protected ArrayList<Population> populations;
	protected MPopulation mpopulation;
	protected int numPrograms;
	protected MIndividual currentMBest;
	protected double minDistance;
	protected double minK;
	
	protected File file;
	protected File file2;
	protected File OutputsFile;

	public EsgsgpRun(Data data) {
		super(data);		
		if(Main.CURRENTRUN==1){
			
			printFirstGen();
		}
		
	}

	protected void initialize() {

		populations = new ArrayList <Population>();
		mpopulation = new MPopulation();
		numPrograms = 2;
		minDistance=50;
		minK=1.2;
		file = new File("results/results.txt");
		file2 = new File("results/population.txt");
		OutputsFile = new File("results/outputs.txt");
		boolean flag=false;

		
		for (int i = 0; i <  numPrograms; i++) {
			
			super.initialize();
			
			populations.add(population);
			
		}
		for (int i = 0; i < population.getSize(); i++) {
			
			//build the Mindividuals using the individuals from the initialized populations
			MIndividual mindividual = new MIndividual();
			
			for (int j = 0; j < numPrograms; j++) {
				Individual ind = populations.get(j).getIndividual(i);
				//check if any errors of the program is > maxError . if not, add to Mindividual
				ind.evaluate(data);
				flag=ind.checkMaxError();				
				
				if (j==0){

				}
				else{
					double k = mindividual.calculateK(ind);
					while(Math.abs(k)<1.01&&Math.abs(k)>0.999){
						//if k is too small, replace ind with a new program
						ind = grow(this.getMaximumDepth());
						ind.evaluate(data);
						k=mindividual.calculateK(ind);
						System.out.println(mindividual.calculateK(ind));
					}
					
				}
				
				mindividual.addProgramAtIndex(ind,j);
				
				}
								
			mindividual.evaluate(data);		
			
			//print to txt file vectors of mindividual being added to population
			try{				
				mindividual.printVectors(currentGeneration,OutputsFile);
			}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			}
			mpopulation.addIndividual(mindividual);			
			
		}	
		
		updateCurrentMBest();	
		printMPopState(0);
		
		try {
			output(0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void printFirstGen(){
		for (int i = 0; i < mpopulation.getSize(); i++) {
			//print to each mindividual output2, (population.txt)
			try {
				mpopulation.getMIndividual(i).output2(0, file2);
				output();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	
	public void evolve(int numberOfGenerations) throws IOException {
		//first tournament selection - selectLowestError for num inds in population
		//set this to population
		//
		
		
		// evolve for a given number of generations
		while (currentGeneration <= numberOfGenerations) {
			MPopulation offspring = new MPopulation();

			// generate a new offspring population
			while (offspring.getSize() < population.getSize()) {
				MIndividual mp1, newIndividual;
				//mp1 = nestedSelectMParent();
				mp1 = selectMParent();
				
				// apply crossover to parents selected by tournament selection
				if (randomGenerator.nextDouble() < crossoverProbability) {
					//MIndividual mp2 = nestedSelectMParent();
					MIndividual mp2 = selectMParent();
					newIndividual = applyStandardCrossover(mp1, mp2);
					boolean flag=false;
					//check errors of each program
					for(int i=0; i<newIndividual.numPrograms;i++){
						if(newIndividual.getProgram(i).checkMaxError()){
							flag=true;
						}
					}
					//check distances of each program
					double distance=newIndividual.calcDistances();
					double k = newIndividual.calculateK();
					while(Math.abs(k)<1.01&&Math.abs(k)>0.999){						
						//mp1 = nestedSelectMParent();
						//mp2 = nestedSelectMParent();
						mp1 = selectMParent();
						mp2 = selectMParent();
						newIndividual = applyStandardCrossover(mp1, mp2);
						flag=false;
						for(int i=0; i<newIndividual.numPrograms;i++){
							if(newIndividual.getProgram(i).checkMaxError()){
								flag=true;
							}
						}
						distance=newIndividual.calcDistances();
						k = newIndividual.calculateK();
					}
					
			
					newIndividual.setMp1(mp1);					
					newIndividual.setMp2(mp2);
					
				}
				// apply mutation
				else {
					
					newIndividual = applyStandardMutation(mp1);
					newIndividual.setMp1(mp1);
					boolean flag=false;
					//check errors of each program
					for(int i=0; i<newIndividual.numPrograms;i++){
						if(newIndividual.getProgram(i).checkMaxError()){
							flag=true;
						}
					}
					double distance = newIndividual.calcDistances();
					double k = newIndividual.calculateK();
					while(Math.abs(k)<1.01&&Math.abs(k)>0.999){
						//mp1 = nestedSelectMParent();	
						mp1 = selectMParent();
						flag=false;
						newIndividual = applyStandardMutation(mp1);
						for(int i=0; i<newIndividual.numPrograms;i++){
							if(newIndividual.getProgram(i).checkMaxError()){
								flag=true;
							}
						}
						distance = newIndividual.calcDistances();
						k = newIndividual.calculateK();
					}

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
						//evaluate each program in the individual
						newIndividual.getProgram(i).evaluate(data);
						
					}					
					
					newIndividual.evaluate(data);
					newIndividual.printVectors(currentGeneration,OutputsFile);
				}
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
			System.out.printf("Training Theta (deg):\t\t%.2f\nUnseen Theta (deg):\t\t%.2f\nReconstructed Training Error:"
					+ "\t\t%.2f\nReconstructed Unseen Error:\t\t%.2f\nTraining Error Program 1:\t\t%.2f\nTraining Error Program 2:\t\t%.2f\n"
					+ "Id:\t\t%d\nK:\t\t%.2f\n",
					currentMBest.getTrainingTheta(), currentMBest.getUnseenTheta(), currentMBest.getReconTrainingError(),
					currentMBest.getReconUnseenError(),currentMBest.getProgram(0).getTrainingError(),currentMBest.getProgram(1).getTrainingError(),
					currentMBest.getId(),currentMBest.getK());
			
		}
	}
	//overloaded for printing Generation 0 - currentGeneration is already incremented when print is called the first time
	protected void printMPopState(int generation) {
		if (printAtEachGeneration) {
			System.out.println("\nGeneration:\t\t0");
			System.out.printf("Training Theta:\t\t%.2f\nUnseen Theta:\t\t%.2f\nReconstructed Training Error:"
					+ "\t\t%.2f\nReconstructed Unseen Error:\t\t%.2f\nTraining Error Program 1:\t\t%.2f\nTraining Error Program 2:\t\t%.2f\n",
					currentMBest.getTrainingTheta(), currentMBest.getUnseenTheta(), currentMBest.getReconTrainingError(),
					currentMBest.getReconUnseenError(),currentMBest.getProgram(0).getTrainingError(),currentMBest.getProgram(1).getTrainingError());
			
		}
	}
	
//	protected MIndividual nestedSelectMParent() {		
//		
//		MPopulation tournamentPopulationThree = new MPopulation();
//		
//		int tournamentSize = (int) (0.05 * population.getSize());
//		
//		//from tournamentPopulation, select lowest error
//		//repeat N times where N = tournament size, this is tournamentPopulation2
//		for (int k=0;k<tournamentSize;k++){
//			MPopulation tournamentPopulationTwo = new MPopulation();
//			for (int j=0;j<tournamentSize;j++){
//				MPopulation tournamentPopulation = new MPopulation();
//				for (int i = 0; i < tournamentSize; i++) {
//					int index = randomGenerator.nextInt((mpopulation.getSize()));
//					
//					tournamentPopulation.addIndividual(mpopulation.getMIndividual(index));
//
//				}
//				
//				tournamentPopulationTwo.addIndividual(tournamentPopulation.getHighestDistance());
//				
//			}	
//			tournamentPopulationThree.addIndividual(tournamentPopulationTwo.getLowestErrorM());
//			
//		}
//		//from tournamentPopulation3 select lowest Theta - this is the selected parent
//		return tournamentPopulationThree.getBestM();
//	}
//	
//protected MIndividual nestedSelectMParent() {		
//		
//		MPopulation tournamentPopulationTwo = new MPopulation();
//		
//		int tournamentSize = (int) (0.05 * population.getSize());
//		
//		//from tournamentPopulation, select lowest error
//		//repeat N times where N = tournament size, this is tournamentPopulation2
//
//			for (int j=0;j<tournamentSize;j++){
//				MPopulation tournamentPopulation = new MPopulation();
//				for (int i = 0; i < tournamentSize; i++) {
//					int index = randomGenerator.nextInt((mpopulation.getSize()));
//					
//					tournamentPopulation.addIndividual(mpopulation.getMIndividual(index));
//					//System.out.println("Add ind "+ mpopulation.getMIndividual(index).getId()+ " with distance "+mpopulation.getMIndividual(index).calcDistances());
//
//				}
//				
//				tournamentPopulationTwo.addIndividual(tournamentPopulation.getHighestDistance());
//				//System.out.println("Distance tournament won by "+ tournamentPopulation.getHighestDistance().getId()+ " with distance "+tournamentPopulation.getHighestDistance().calcDistances());
//				
//			}	
//
//		//from tournamentPopulation3 select lowest Theta - this is the selected parent
//		return tournamentPopulationTwo.getBestM();
//	}
//	protected MIndividual nestedSelectMParent() {		
//	
//	MPopulation tournamentPopulationTwo = new MPopulation();
//	
//	int tournamentSize = (int) (0.05 * population.getSize());
//	
//	//from tournamentPopulation, select lowest error
//	//repeat N times where N = tournament size, this is tournamentPopulation2
//
//		for (int j=0;j<tournamentSize;j++){
//			MPopulation tournamentPopulation = new MPopulation();
//			for (int i = 0; i < tournamentSize; i++) {
//				int index = randomGenerator.nextInt((mpopulation.getSize()));
//				
//				tournamentPopulation.addIndividual(mpopulation.getMIndividual(index));
//				double sum = 0;
//				for(int k=0;k<mpopulation.getMIndividual(index).numPrograms;k++){
//					sum+=mpopulation.getMIndividual(index).getProgram(k).getTrainingError();
//				}
//				double avg =sum/mpopulation.getMIndividual(index).numPrograms;
//				//System.out.println("Add ind "+ mpopulation.getMIndividual(index).getId()+ " with error "+avg);
//
//			}
//
//			tournamentPopulationTwo.addIndividual(tournamentPopulation.getLowestErrorM());
//			//System.out.println("Error tournament won by "+ tournamentPopulation.getLowestErrorM().getId());
//			
//		}	
//	//from tournamentPopulation3 select lowest Theta - this is the selected parent
//	return tournamentPopulationTwo.getBestM();
//}
	 //tournament selection
	
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
				
//			if (buildIndividuals) {
//				pOffspring = buildCrossoverIndividual(p1, p2);
//				offspring.addProgramAtIndex(pOffspring, i);
//			}
//			else {
//				pOffspring = buildCrossoverSemantics(p1, p2);
//				offspring.addProgramAtIndex(pOffspring,i);
//			}	
			pOffspring=applyStandardCrossover(p1,p1);
			offspring.addProgramAtIndex(pOffspring,i);
			pOffspring.evaluate(data);
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
			Individual pOffspring = new Individual();
//			if (buildIndividuals) {
//				pOffspring=buildMutationIndividual(mp.getProgram(i));
//				offspring.addProgramAtIndex(pOffspring,i);
//				
//			} else {				
//				pOffspring=buildMutationSemantics(mp.getProgram(i));
//				offspring.addProgramAtIndex(pOffspring,i);
//				
//			}
			pOffspring=applyStandardMutation(mp.getProgram(i));
			offspring.addProgramAtIndex(pOffspring,i);
			pOffspring.evaluate(data);
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
	      writer.write("CurrentRun,Generation,NumGens,NumRuns,popSize,trainingTheta,UnseenTheta,"
	      		+ "reconTrainError,reconUnseenError,depth,depthLimitOn,maxDepth,crossoverProb,k"); 
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
	    		  ","+populationSize+","+currentMBest.getTrainingTheta()+","+currentMBest.getUnseenTheta()+","+currentMBest.getReconTrainingError()+
	    		  ","+currentMBest.getReconUnseenError()+
	    		  ","+currentMBest.getDepth() + ","+","+applyDepthLimit+","+maximumDepth+
	    		  ","+crossoverProbability+","+currentMBest.getK()); 
	      writer.flush();
	      writer.close();	      
	   
   }
   
   protected void output(int generation)throws IOException{

	      FileWriter writer = new FileWriter(file,true); 
	      // Writes the content to the file
	      writer.write("\n"+Main.CURRENTRUN+","+generation+","+Main.NUMBER_OF_GENERATIONS+","+Main.NUMBER_OF_RUNS+
	    		  ","+populationSize+","+currentMBest.getTrainingTheta()+","+currentMBest.getUnseenTheta()+","+currentMBest.getReconTrainingError()+
	    		  ","+currentMBest.getReconUnseenError()+
	    		  ","+currentMBest.getDepth() + ","+","+applyDepthLimit+","+maximumDepth+
	    		  ","+crossoverProbability+","+currentMBest.getK()); 
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
