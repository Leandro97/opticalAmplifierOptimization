package br.upe.simulations.JLT18.Channel;

import java.io.IOException;
import java.util.ArrayList;

import org.moeaframework.Executor;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.EncodingUtils;

//import com.sun.jmx.snmp.Timestamp;
import br.upe.base.AmplifierType;
import br.upe.base.OpticalSignal;
import br.upe.signal.factory.PowerMaskSignal;
import br.upe.signal.factory.Leandro.ITUGridUniformSignal;
import br.upe.simulations.JLT18.Channel.ACOPCustomObject;
//import br.upe.simulations.JLT18.Channel.ACOPProblem;
import br.upe.MOO.modeling.ACOPProblem;
import br.upe.simulations.JLT18.Channel.NewDataSetWriter;

public class SimpleExecution {
	public static void main(String[] args) {
		ACOPCustomObject acopCO;

		int[] ampNumberList      = {2, 3, 4, 5, 6, 7, 8};
		String[] channelTypeList = {"regular"};
		int[] channelNumberList  = {2, 3, 4, 5, 6, 7, 8, 9, 10, 15, 20, 30, 40};

		NewDataSetWriter dsw = new NewDataSetWriter();

		for (int ampNumber : ampNumberList) {
			for (String channelType : channelTypeList) {
				for (int channelNumber : channelNumberList) {
					System.out.println(ampNumber + " amplifiers." + channelNumber + " channel(s) " + channelType);
					
					OpticalSignal inputSignal;
					acopCO = new ACOPCustomObject(ampNumber, channelType, channelNumber);
					
					PowerMaskSignal signal = new PowerMaskSignal(channelNumber, AmplifierType.EDFA_1, -18, 40);
					inputSignal = signal.createSignal();
					
					int runs = 0;
					int limit = 10;
			        NondominatedPopulation result = null;
			        
			        dsw.isSingleRun = limit == 1;
			        
			        while(runs < limit) {
						while (true) {
							NondominatedPopulation result_temp = new Executor()
									.withProblemClass(ACOPProblem.class, ampNumber, AmplifierType.EDFA_1)
									.withAlgorithm("NSGAII").withMaxEvaluations(25000).run();
			
							if (result_temp.isEmpty()) { continue; }
							
							if(result == null)
			                    result = result_temp;
			                else {
			                	int counter = 0;
			                	
			                    for(Solution solution : result_temp) {
			                    	if (solution.getObjective(1) < 0) { continue; } 
			                    	
			                    	counter++;
			                        result.add(solution);
			                    }
			                    
			                    if (counter == 0) { continue; }
			                }
							break;
						}
						
						runs++;
						
						//last two parameters correspond to: Boolean singleDataset, Boolean saveBestOSNR
						//dsw.write(acopCO, inputSignal, result, true, false); //single dataset, all osnr solutions (r1d1)
						//dsw.write(acopCO, inputSignal, result, true, true); //single dataset, best osnr solutions (r1d2)
						//dsw.write(acopCO, inputSignal, result, false, false); //one dataset/amp, all osnr solutions (r2d1)
						dsw.write(acopCO, inputSignal, result, false, true); //one dataset/amp, best osnr solutions (r2d2)
						
						System.out.println("Scenario finished");
						System.out.println();
					}
				}
			}
		}
		
		System.out.println("Done!");
	}
}
