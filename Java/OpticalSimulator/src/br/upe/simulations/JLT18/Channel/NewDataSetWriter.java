package br.upe.simulations.JLT18.Channel;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.EncodingUtils;

import br.upe.base.OpticalChannel;
import br.upe.base.OpticalSignal;
import br.upe.signal.factory.Leandro.ITUGridUniformSignal;

public class NewDataSetWriter {
	static FileWriter fw;
	static int totalChannelNumber = 40;
	public boolean isSingleRun = false;
	
	public void write(ACOPCustomObject acopCO, OpticalSignal signal, NondominatedPopulation result, Boolean singleDataset, Boolean saveBestOSNR) {
		try {
			
			String datasetId = "d1";
			
			if(saveBestOSNR) {
				datasetId = "d2";
				result.truncate((int) (result.size()* 0.3), new ComparatorResultOSNR());
			}
			
			String runPrefix = "MultipleRuns";
			
			if (isSingleRun) {
				runPrefix = "SingleRun";
			}
			
			if(singleDataset) {
				//all scenario dataset
				fw = new FileWriter("output/datasets/" + runPrefix + "/" + datasetId + "/dataset.txt", true);
			} else {
				//amp number dataset
				fw = new FileWriter("output/datasets/" + runPrefix + "/" + datasetId + "/dataset-" + acopCO.getNumberOfAmplifiers() + ".txt", true);
			}
			
			String aux = new String(getFrequencies(acopCO, signal) + ",");
			
			for (int i = 0; i < result.size(); i++) {
				Solution solution = result.get(i);
				
				int[] x = EncodingUtils.getInt(solution);
				fw.write(aux);
				
				for (int j = 0; j < solution.getNumberOfVariables() / 2; j++) {
					fw.write(x[j] + "," + x[j + acopCO.getNumberOfAmplifiers()]);
					
					if (j != acopCO.getNumberOfAmplifiers() - 1) {
						fw.write(",");
					}
				}
				
				if (singleDataset) {
					if(solution.getNumberOfVariables() != 14) fw.write(",");
					
					for (int j = 0; j < 7 - solution.getNumberOfVariables() / 2; j++) {
						fw.write("0,0");
						
						if (j != 7 - solution.getNumberOfVariables() / 2 - 1) {
							fw.write(",");
						}
					} 
				}
				
				fw.write("\n");
			}

			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static String getFrequencies(ACOPCustomObject acopCO, OpticalSignal inputSignal) {
		ArrayList<Double> frequencies      =  new ArrayList<Double>();
		ArrayList<Integer> indexes         =  new ArrayList<Integer>();
		ArrayList<OpticalChannel> channels = inputSignal.getChannels();
		
		for(int i = 0; i < totalChannelNumber; i++) {
			frequencies.add((double) 0);
		}
		System.out.println(inputSignal);
		
		int aux = 0;
		
		for (OpticalChannel channel: channels) {
			indexes.add(aux);
			aux++;
		}
		
		int counter = 0;
		
		System.out.println(indexes);
		for(int index: indexes) {
			frequencies.set(index, channels.get(counter).getFrequency());
			counter += 1;
		}
		
		String strFrequencies = frequencies.toString().replace("[", "").replace("]", "").replace(" ", "");
		return strFrequencies;
	}
}
