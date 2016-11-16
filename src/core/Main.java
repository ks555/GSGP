package core;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import utils.Utils;
import java.math.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;;


public class Main {

	public static final String DATA_FILENAME = "Dataset_bioav/";
	//public static final String DATA_FILENAME = "ppb_Ivo";
	public static final boolean SHUFFLE_AND_SPLIT = false;
	//public static final String DATA_FILENAME = "bio";
	public static final int NUMBER_OF_RUNS = 10;
	public static final int NUMBER_OF_GENERATIONS = 500;
	public static int CURRENTRUN;

	public static void main(String[] args) throws IOException {

		// load training and unseen data
		

		// run GP for a given number of runs
		double[][] resultsPerRun = new double[4][NUMBER_OF_RUNS];
		for (int i = 0; i < NUMBER_OF_RUNS; i++) {
			CURRENTRUN=i+1;
			Data data = loadData(DATA_FILENAME);

			
			System.out.printf("\n\t\t##### Run %d #####\n", i + 1);
			//GpRun gp = new GpRun(data);
			//GsgpRun gp = new GsgpRun(data);
			EsgsgpRun gp = new EsgsgpRun(data);
			
			if(CURRENTRUN==1){
				gp.createOutputFile();
				
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
	
	public static Data loadData(String dataFilename) {
		double[][] trainingData, unseenData;

		if (SHUFFLE_AND_SPLIT) {
			double[][] allData = readData(dataFilename + ".txt");
			List<Integer> instances = Utils.shuffleInstances(allData.length);
			int trainingInstances = (int) Math.floor(0.7 * allData.length);
			int unseenInstances = (int) Math.ceil(0.3 * allData.length);

			trainingData = new double[trainingInstances][];
			unseenData = new double[unseenInstances][];

			for (int i = 0; i < trainingInstances; i++) {
				trainingData[i] = allData[instances.get(i)];
			}

			for (int i = 0; i < unseenInstances; i++) {
				unseenData[i] = allData[instances.get(trainingInstances + i)];
			}
		} else {
			trainingData = readData(dataFilename+"train"+Main.CURRENTRUN +".txt");
			unseenData = readData(dataFilename +"test"+Main.CURRENTRUN +".txt");
		}
		return new Data(trainingData, unseenData);
	}

	public static double[][] readData(String filename) {
		double[][] data = null;
		List<String> allLines = new ArrayList<String>();
		try {
			BufferedReader inputBuffer = new BufferedReader(new FileReader(filename));
			String line = inputBuffer.readLine();
			while (line != null) {
				allLines.add(line);
				line = inputBuffer.readLine();
			}
			inputBuffer.close();
		} catch (Exception e) {
			System.out.println(e);
		}

		StringTokenizer tokens = new StringTokenizer(allLines.get(2).trim());
		int numberOfColumns = tokens.countTokens();
		data = new double[allLines.size()-2][numberOfColumns];
		System.out.println("columns " + numberOfColumns );
		System.out.println("rows " + data.length );
		for (int i = 2; i < data.length; i++) {
			tokens = new StringTokenizer(allLines.get(i).trim());
			for (int k = 0; k < numberOfColumns; k++) {
				data[i][k] = Double.parseDouble(tokens.nextToken().trim());
				if (i==2){
					//System.out.println(data[i][k]);
				}
			}
		}
		return data;
	}
}
