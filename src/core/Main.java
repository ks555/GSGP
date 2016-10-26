package core;

import java.io.IOException;

import utils.Utils;
import java.math.*;;


public class Main {

	public static final String DATA_FILENAME = "ppb";
	public static final int NUMBER_OF_RUNS = 1;
	public static final int NUMBER_OF_GENERATIONS = 5;
	public static int CURRENTRUN;

	public static void main(String[] args) throws IOException {

		// load training and unseen data
		Data data = Utils.loadData(DATA_FILENAME);

		// run GP for a given number of runs
		double[][] resultsPerRun = new double[4][NUMBER_OF_RUNS];
		for (int i = 0; i < NUMBER_OF_RUNS; i++) {
			CURRENTRUN=i+1;

			
			System.out.printf("\n\t\t##### Run %d #####\n", i + 1);
			//GpRun gp = new GpRun(data);
			//GsgpRun gp = new GsgpRun(data);
			EsgsgpRun gp = new EsgsgpRun(data);
			if(CURRENTRUN==1){
				gp.createOutputFile();
				gp.printFirstGen();
			}
			gp.evolve(NUMBER_OF_GENERATIONS);

			if(gp instanceof EsgsgpRun){
				System.out.println("Running esgp");
				
			
			}else{
				Individual bestFound = gp.getCurrentBest();
				resultsPerRun[0][i] = bestFound.getTrainingError();
				resultsPerRun[1][i] = bestFound.getUnseenError();
				resultsPerRun[2][i] = bestFound.getSize();
				resultsPerRun[3][i] = bestFound.getDepth();
				System.out.print("\nBest =>");
				bestFound.print();
				System.out.println();
			}
			System.out.println();
		}

//		 present average results
//		System.out.printf("\n\t\t##### Results #####\n\n");
//		System.out.printf("Average training error:\t\t%.2f\n", Utils.getAverage(resultsPerRun[0]));
//		System.out.printf("Average unseen error:\t\t%.2f\n", Utils.getAverage(resultsPerRun[1]));
//		System.out.printf("Average size:\t\t\t%.2f\n", Utils.getAverage(resultsPerRun[2]));
//		System.out.printf("Average depth:\t\t\t%.2f\n", Utils.getAverage(resultsPerRun[3]));
	}
}
