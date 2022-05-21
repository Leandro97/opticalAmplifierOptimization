package br.upe.simulations.JLT18.Channel;

import java.io.FileWriter;
import java.io.IOException;

import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.EncodingUtils;

public class DataSetWriter {
	static FileWriter fw;

	public void write(ACOPCustomObject acopCO, NondominatedPopulation result) {
		try {
			fw = new FileWriter("dataset.txt", true);

			String aux = new String(acopCO.getNumberOfAmplifiers() + "," + acopCO.getInputPowerCh() + ","
					+ acopCO.getLinkLength() + ",");

			for (int i = 1; i < result.size(); i++) {
				Solution solution = result.get(i);
				int[] x = EncodingUtils.getInt(solution);
				
				for (int j = 0; j < solution.getNumberOfVariables() / 2; j++) {
					fw.write(aux);
					fw.write(j + "," + x[j] + "," + x[j + acopCO.getNumberOfAmplifiers()] + "\n");
				}
			}

			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
