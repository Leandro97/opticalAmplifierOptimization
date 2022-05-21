package br.upe.simulations.JLT18.Channel;

import java.util.Comparator;

import org.moeaframework.core.Solution;

public class ComparatorResultOSNR implements Comparator<Solution> {

	@Override
	public int compare(Solution solution1, Solution solution2) {
		// Descending OSNR order
		double osnr1 = 1/solution1.getObjective(1);
		double osnr2 = 1/solution2.getObjective(1);
		if(osnr1 < osnr2) {
			return 1;
		}
		return -1;
	}

}
