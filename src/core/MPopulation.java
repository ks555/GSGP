package core;


import java.util.ArrayList;

public class MPopulation extends Population {

	private static final long serialVersionUID = 7L;

	protected ArrayList<MIndividual> mindividuals;

	public MPopulation() {
		mindividuals = new ArrayList<MIndividual>();		
		
	}

	public MIndividual getBestM() {
		
		return mindividuals.get(getBestIndex());
	}

	public int getBestIndex() {
		int bestIndex = 0;
		double bestTrainingError = mindividuals.get(bestIndex).getTrainingError();
		for (int i = 1; i < mindividuals.size(); i++) {
			if (mindividuals.get(i).getTrainingError() < bestTrainingError) {
				bestTrainingError = mindividuals.get(i).getTrainingError();
				bestIndex = i;
			}
		}
		return bestIndex;
	}

	public void addIndividual(MIndividual mindividual) {
		mindividuals.add(mindividual);
	}
//
//	public void removeIndividual(int index) {
//		individuals.remove(index);
//	}
//
	public int getSize() {
		return mindividuals.size();
	}

	public MIndividual getMIndividual(int index) {
		return mindividuals.get(index);
	}
}
