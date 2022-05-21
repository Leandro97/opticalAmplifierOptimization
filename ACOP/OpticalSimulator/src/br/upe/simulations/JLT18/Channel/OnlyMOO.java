package br.upe.simulations.JLT18.Channel;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.moeaframework.Executor;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;

import br.upe.base.ACOPHeuristic;
import br.upe.base.Amplifier;
import br.upe.base.AmplifierType;
import br.upe.base.ObjectiveFunction;
import br.upe.base.OpticalSignal;
//import br.upe.simulations.JLT18.Channel.ACOPProblem;
import br.upe.MOO.modeling.ACOPProblem;
import br.upe.simulations.JLT18.Channel.Regressor;
import br.upe.initializations.UniformInitializationVOA;
import br.upe.metrics.BeckerNoiseFigureMetric;
import br.upe.metrics.GNLIMetric;
import br.upe.objfunctions.linerInterpolation.LinearInterpolationFunction;
import br.upe.objfunctions.maskOSNR.MaskOSNRFunction;
import br.upe.objfunctions.rn.util.NormalizationUtility;
import br.upe.objfunctions.rn.util.NormalizationUtilityFactory;
import br.upe.selection.MaxGainSelection;
import br.upe.signal.factory.PowerMaskSignal;
import br.upe.simulations.JLT18.Channel.ACOPCustomObject;
import br.upe.simulations.simsetups.SimSetAMPVOA;
import br.upe.simulations.simsetups.SimulationSetup;

public class OnlyMOO {
	public static ArrayList<ArrayList<Double>> osnrResults   = new ArrayList<ArrayList<Double>>();
	public static ArrayList<ArrayList<Double>> rippleResults = new ArrayList<ArrayList<Double>>();
	public static FileWriter fw;

	public static ACOPHeuristic heuristic;
	public static AmplifierType type = AmplifierType.EDFA_1;

	public static NormalizationUtility nu = NormalizationUtilityFactory.getInstance().fabricate(type);
	public static ObjectiveFunction functionAux = new LinearInterpolationFunction();
	
	public static ACOPCustomObject acopCO;	
	public static int[] ampTypeList        = {1};
	public static int[] ampNumberList      = {2, 3, 4, 5, 6, 7, 8};
	public static String[] channelTypeList = {"regular"};
	public static int[] channelNumberList  = {10, 20, 40};
	
	public static float mooTime = 0;
	
	public static void resetResults() {
		osnrResults   = new ArrayList<ArrayList<Double>>();
		rippleResults = new ArrayList<ArrayList<Double>>();
		
		for(int i = 0; i < 6; i++) {	
			osnrResults.add(new ArrayList<Double>());
			rippleResults.add(new ArrayList<Double>());
		}
	}
	
	/*
	  Before executing this class, in terminal, enter python fold and then run:
	  FLASK_APP=linkRegressor.py flask run
	*/
	public static void main(String[] args) throws IOException {
		System.out.println("-- LI --");
		for (int ampNumber : ampNumberList) {
			for (int ampType : ampTypeList) {
				for (int channelNumber : channelNumberList) {
					resetResults();
					for (String channelType : channelTypeList) {
						for (int i = 0; i < 1; i ++) {
							runSimulation(ampNumber, channelType, channelNumber, ampType);
						}
					}
					
					fw = new FileWriter("output/optimizedPareto/osnr_" + ampNumber + "amp_" + channelNumber +"channel", false);
					fw.write(osnrResults.toString());
					fw.close();

					fw = new FileWriter("output/optimizedPareto/ripple_" + ampNumber + "amp_" + channelNumber + "channel", false);
					fw.write(rippleResults.toString());
					fw.close();		
				}
			}
		}
		
		System.out.println("Moo mean time " + mooTime);
		GraphPlotter.plot("plotParetoBaselineV1");
	}

	private static void runSimulation(int ampNumber, String channelType, int channelNumber, int ampType) throws IOException {	
		if (ampType == 1) {
			type = AmplifierType.EDFA_1;
		} else {
			type = AmplifierType.EDFA_2;
		}
			
		System.out.println(ampNumber + " amplifiers. " + channelNumber + " channel(s) " + channelType + ". EDFA" + ampType);
		acopCO = new ACOPCustomObject(ampNumber, channelType, channelNumber);
		
		PowerMaskSignal signal = new PowerMaskSignal(channelNumber, type, -18, 40);
		OpticalSignal inputSignal = signal.createSignal();
		
		System.out.println("****** MOO Begin ******");
		long start = System.currentTimeMillis();
		
		while (true) {
			NondominatedPopulation result = new Executor()
					.withProblemClass(ACOPProblem.class, ampNumber, type)
					.withAlgorithm("NSGAII").withMaxEvaluations(25000).run();

			if (result.size() <= 1)
				continue;

			int counter = 0;
			
			for (Solution solution : result) {
				if (solution.getObjective(1) < 0) { continue; }
				
				counter++;
				rippleResults.get(0).add(solution.getObjective(0));
				osnrResults.get(0).add(1.0 / solution.getObjective(1));
				
				System.out.format("%.4f\t%.4f\t|\t", solution.getObjective(0), (1.0 / solution.getObjective(1)));
				System.out.println();
			}
			
			if (counter < 5) { 
				rippleResults.get(0).clear();
				osnrResults.get(0).clear();
				continue;
			}
			
			break;
		}
		
		long elapsedTimeMillis = System.currentTimeMillis() - start;
		System.out.println("Elapsed time: " + elapsedTimeMillis / 1000F);
		mooTime += elapsedTimeMillis / (1000F * channelNumberList.length * ampNumberList.length);
		
		System.out.println("****** MOO End ******");
		
		System.out.println("****** Simulation Setup ******");
		
		SimulationSetup simSet = new SimSetAMPVOA(channelNumber, acopCO.getInputPowerCh(), 9.0f, ampNumber, 18);
		float[] linLosses      = simSet.getLINK_LOSSES();
		int numberAmplifiers   = simSet.getNumberOfAmplifiers();

		ObjectiveFunction function = new MaskOSNRFunction(functionAux, linLosses[0], false);
		BeckerNoiseFigureMetric nfMetric = new BeckerNoiseFigureMetric(linLosses);

		GNLIMetric gnliMetric = new GNLIMetric(28e9, 100e9, channelNumber, acopCO.getInputPowerCh(), acopCO.getLinkLength()*1000);

		// Definindo ganho m�ximo
		float maxPout = simSet.getMaxOutputPower();
		// System.out.println(maxPout);
		
		System.out.println("****** BayesianRidge Begin ******");
		
		heuristic = new Regressor("BayesianRidge", numberAmplifiers, linLosses, inputSignal, signal.getChannelIndexes(), functionAux);
		heuristic.setInitialization(new UniformInitializationVOA(type));
		heuristic.setSelectionOp(new MaxGainSelection());
		heuristic.setVoaMaxAttenuation(simSet.getVOA_MAX_ATT());
		heuristic.setRoadmAttenuation(simSet.getROADM_ATT());
		heuristic.setMaxOutputPower(maxPout);
		
		Amplifier[] amplifiers  = heuristic.execute();
		OpticalSignal endSignal = heuristic.getMonitors()[numberAmplifiers - 1].getOutputSignal();
		
		saveResults(1, amplifiers, endSignal, nfMetric, gnliMetric, heuristic.calculateTilt(endSignal),
				heuristic.calculateOSNR(endSignal));
		
		System.out.println("****** RandomForestRegressor Begin ******");
		
		heuristic = new Regressor("RandomForestRegressor", numberAmplifiers, linLosses, inputSignal, signal.getChannelIndexes(), functionAux);
		heuristic.setInitialization(new UniformInitializationVOA(type));
		heuristic.setSelectionOp(new MaxGainSelection());
		heuristic.setVoaMaxAttenuation(simSet.getVOA_MAX_ATT());
		heuristic.setRoadmAttenuation(simSet.getROADM_ATT());
		heuristic.setMaxOutputPower(maxPout);
		
		amplifiers  = heuristic.execute();
		endSignal = heuristic.getMonitors()[numberAmplifiers - 1].getOutputSignal();
		
		saveResults(2, amplifiers, endSignal, nfMetric, gnliMetric, heuristic.calculateTilt(endSignal),
				heuristic.calculateOSNR(endSignal));
		
		System.out.println("****** DecisionTreeRegressor Begin ******");
		
		heuristic = new Regressor("DecisionTreeRegressor", numberAmplifiers, linLosses, inputSignal, signal.getChannelIndexes(), functionAux);
		heuristic.setInitialization(new UniformInitializationVOA(type));
		heuristic.setSelectionOp(new MaxGainSelection());
		heuristic.setVoaMaxAttenuation(simSet.getVOA_MAX_ATT());
		heuristic.setRoadmAttenuation(simSet.getROADM_ATT());
		heuristic.setMaxOutputPower(maxPout);
		
		amplifiers  = heuristic.execute();
		endSignal = heuristic.getMonitors()[numberAmplifiers - 1].getOutputSignal();
		
		saveResults(3, amplifiers, endSignal, nfMetric, gnliMetric, heuristic.calculateTilt(endSignal),
				heuristic.calculateOSNR(endSignal));
		
		System.out.println("****** LassoCV Begin ******");
		
		heuristic = new Regressor("LassoCV", numberAmplifiers, linLosses, inputSignal, signal.getChannelIndexes(), functionAux);
		heuristic.setInitialization(new UniformInitializationVOA(type));
		heuristic.setSelectionOp(new MaxGainSelection());
		heuristic.setVoaMaxAttenuation(simSet.getVOA_MAX_ATT());
		heuristic.setRoadmAttenuation(simSet.getROADM_ATT());
		heuristic.setMaxOutputPower(maxPout);
		
		amplifiers  = heuristic.execute();
		endSignal = heuristic.getMonitors()[numberAmplifiers - 1].getOutputSignal();
		
		saveResults(4, amplifiers, endSignal, nfMetric, gnliMetric, heuristic.calculateTilt(endSignal),
				heuristic.calculateOSNR(endSignal));
		
		System.out.println("****** SimpleMeanRegressor Begin ******");
		
		heuristic = new Regressor("smr", numberAmplifiers, linLosses, inputSignal, signal.getChannelIndexes(), functionAux);
		heuristic.setInitialization(new UniformInitializationVOA(type));
		heuristic.setSelectionOp(new MaxGainSelection());
		heuristic.setVoaMaxAttenuation(simSet.getVOA_MAX_ATT());
		heuristic.setRoadmAttenuation(simSet.getROADM_ATT());
		heuristic.setMaxOutputPower(maxPout);
		
		amplifiers  = heuristic.execute();
		endSignal = heuristic.getMonitors()[numberAmplifiers - 1].getOutputSignal();
		
		saveResults(5, amplifiers, endSignal, nfMetric, gnliMetric, heuristic.calculateTilt(endSignal),
				heuristic.calculateOSNR(endSignal));
		
		System.out.println("****** Simulation end ******");
		System.out.println("Scenario finished");
		System.out.println();
		
		fw = new FileWriter("output/optimizedPareto/osnr_" + ampNumber + "amp_" + channelNumber +"channel", false);
	}
	
	private static void saveResults(int index, Amplifier[] amplifiers, OpticalSignal endSignal, BeckerNoiseFigureMetric nfMetric,
			GNLIMetric gnliMetric, double tilt, double OSNR) {
		gnliMetric.evaluate(amplifiers);
		System.out.println("NF\tTilt_OSNR\tO_NLI\tO_ASE");
		System.out.printf("%2.3f\t%2.3f\t%2.3f\t%2.3f", nfMetric.evaluate(amplifiers), gnliMetric.getTiltOSNR_NLI(),
				gnliMetric.worstOSNR_NLI(), gnliMetric.worstOSNR_ASE());
		System.out.println();

		for (int i = 0; i < amplifiers.length; i++) {
			System.out.println(amplifiers[i]);
		}
		System.out.println();

		if(gnliMetric.getTiltOSNR_NLI() > 0 && gnliMetric.worstOSNR_NLI() > 0) {
			rippleResults.get(index).add(gnliMetric.getTiltOSNR_NLI());
			osnrResults.get(index).add(gnliMetric.worstOSNR_NLI());
		} 

		if (osnrResults.get(index).isEmpty()) {
			osnrResults.get(index).add((double) 24);
		}
		
		if (rippleResults.get(index).isEmpty()) {
			rippleResults.get(index).add((double) 1);
		}
	}

}
