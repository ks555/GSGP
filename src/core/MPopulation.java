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
		double bestTrainingTheta = mindividuals.get(bestIndex).getTrainingTheta();
		for (int i = 1; i < mindividuals.size(); i++) {
			if (mindividuals.get(i).getTrainingTheta() < bestTrainingTheta) {
				bestTrainingTheta = mindividuals.get(i).getTrainingTheta();
				//System.out.println("get best: id " + mindividuals.get(i).getId() + " training theta " + mindividuals.get(i).getTrainingTheta() );
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
