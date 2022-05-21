//package br.upe.simulations.JLT18.Channel;
//
//import java.io.FileWriter;
//import java.io.IOException;
//import java.util.ArrayList;
//
//import org.moeaframework.Executor;
//import org.moeaframework.core.NondominatedPopulation;
//import org.moeaframework.core.Solution;
//import org.moeaframework.core.variable.EncodingUtils;
//
//import br.upe.base.ACOPHeuristic;
//import br.upe.base.Amplifier;
//import br.upe.base.AmplifierType;
//import br.upe.base.ObjectiveFunction;
//import br.upe.base.OpticalSignal;
//import br.upe.MOO.modeling.ACOPProblem;
//import br.upe.heuristics.AsHB.AsHBFlex;
//import br.upe.heuristics.lossComp.LossComp;
//import br.upe.heuristics.maxGain.MaxGain;
//import br.upe.heuristics.uiara.AdGC;
//import br.upe.initializations.UniformInitializationVOA;
//import br.upe.metrics.BeckerNoiseFigureMetric;
//import br.upe.metrics.GNLIMetric;
//import br.upe.objfunctions.linerInterpolation.LinearInterpolationFunction;
//import br.upe.objfunctions.maskOSNR.MaskOSNRFunction;
//import br.upe.objfunctions.rn.util.NormalizationUtility;
//import br.upe.objfunctions.rn.util.NormalizationUtilityFactory;
//import br.upe.selection.MaxGainSelection;
//import br.upe.selection.OSNRWeightSelection;
//import br.upe.selection.UiaraWeightSelection;
//import br.upe.signal.factory.PowerMaskSignal;
//import br.upe.simulations.simsetups.SimSetAMPVOA;
//import br.upe.simulations.simsetups.SimulationSetup;
//
//public class Default_Var {
//	public static ArrayList<ArrayList<Double>> osnrResults   =  new ArrayList<ArrayList<Double>>();
//	public static ArrayList<ArrayList<Double>> rippleResults =  new ArrayList<ArrayList<Double>>();
//	public static FileWriter fw;
//
//	public static ACOPHeuristic heuristic;
//	public static AmplifierType type = AmplifierType.EDFA_1;
//
//	public static NormalizationUtility nu = NormalizationUtilityFactory.getInstance().fabricate(type);
//	public static ObjectiveFunction functionAux = new LinearInterpolationFunction(); // NNFunction(nu);
//	
//	public static int[]   ampTypeEntries    = { 1, 2 };
//	public static int[]   ampNumberEntries  = { 2, 3, 4 };
//	public static float[] inputPowerEntries = { -18 };
//	public static float[] linkLengthEntries = { 90 };
//
//	public static void resetResults() {
//		osnrResults   = new ArrayList<ArrayList<Double>>();
//		rippleResults = new ArrayList<ArrayList<Double>>();
//		
//		for(int i = 0; i < 8; i++) {
//			osnrResults.add(new ArrayList<Double>());
//			rippleResults.add(new ArrayList<Double>());
//		}
//	}
//	
//	public static void main(String[] args) throws IOException {
//		System.out.println("-- LI --");
//		for (int ampNumber : ampNumberEntries) {
//			for (float pinSystem : inputPowerEntries) {
//				for (float linkLength : linkLengthEntries) {
//					for (int ampType : ampTypeEntries) {
//						resetResults();
//						runSimulation(ampNumber, pinSystem, linkLength, ampType);
//						
//						fw = new FileWriter("output/osnr_" + ampNumber + "amp_edfa" + ampType, false);
//						fw.write(osnrResults.toString());
//						fw.close();
//	
//						fw = new FileWriter("output/ripple_" + ampNumber + "amp_edfa" + ampType, false);
//						fw.write(rippleResults.toString());
//						fw.close();		
//					}
//				}
//			}
//		}
//		GraphPlotter.plot("plotParetoGrid");
//		
////		fw = new FileWriter("output/osnr", false);
////		fw.write(osnrResults.toString());
////		fw.close();
////
////		fw = new FileWriter("output/ripple", false);
////		fw.write(rippleResults.toString());
////		fw.close();	
////		GraphPlotter.plot("plotPareto");
//	}
//
//	private static void runSimulation(int ampNumber, float pinSystem, float linkLength, int ampType) throws IOException {	
//		if (ampType == 1) {
//			type = AmplifierType.EDFA_1;
//		} else {
//			type = AmplifierType.EDFA_2;
//		}
//		
//		while (true) {
//			NondominatedPopulation result = new Executor()
//					.withProblemClass(ACOPProblem.class, ampNumber, type).withAlgorithm("NSGAII")
//					.withMaxEvaluations(250000).run();
//
//			if (result.size() <= 1)
//				continue;
//			
//			for (Solution solution : result) {
//				rippleResults.get(0).add(solution.getObjective(0));
//				osnrResults.get(0).add(1.0 / solution.getObjective(1));
//				
//				System.out.format("%.4f\t%.4f\t|\t", solution.getObjective(0), (1.0 / solution.getObjective(1)));
//				System.out.println();
//			}
//			
//			break;
//		}
//			
//		int numberCh = 40;
//		System.out.println("ampNumber = " + ampNumber);
//		System.out.println("pinSystem = " + pinSystem);
//		System.out.println("linkLength = " + linkLength);
//
//		SimulationSetup simSet = new SimSetAMPVOA(numberCh, pinSystem, 9.0f, ampNumber, 18);
//		float[] linLosses = simSet.getLINK_LOSSES();
//		int numberAmplifiers = simSet.getNumberOfAmplifiers();
//
//		ObjectiveFunction function = new MaskOSNRFunction(functionAux, linLosses[0], false);
//		BeckerNoiseFigureMetric nfMetric = new BeckerNoiseFigureMetric(linLosses);
//
//		GNLIMetric gnliMetric = new GNLIMetric(28e9, 100e9, numberCh, pinSystem, linkLength*1000);
//
//		// Definindo ganho m�ximo
//		float maxPout = simSet.getMaxOutputPower();
//		// System.out.println(maxPout);
//
//		PowerMaskSignal signal = new PowerMaskSignal(numberCh, type, simSet.getCHANNEL_POWER(), 30);
//		OpticalSignal inputSignal = signal.createSignal();
//
//		heuristic = new MaxGain(numberAmplifiers, linLosses, inputSignal, function);
//		heuristic.setInitialization(new UniformInitializationVOA(type));
//		heuristic.setSelectionOp(new MaxGainSelection());
//		heuristic.setVoaMaxAttenuation(simSet.getVOA_MAX_ATT());
//		heuristic.setRoadmAttenuation(simSet.getROADM_ATT());
//		heuristic.setMaxOutputPower(maxPout);
//		Amplifier[] amplifiers = heuristic.execute();
//		OpticalSignal endSignal = heuristic.getMonitors()[numberAmplifiers - 1].getOutputSignal();
//
//		System.out.println("****** MaxGain ******");
//		printResults(1, amplifiers, endSignal, nfMetric, gnliMetric, heuristic.calculateTilt(endSignal),
//				heuristic.calculateOSNR(endSignal));
//		//MaxGain end
//		
//		double nfWeight = 1;
//		double gfWeight = 0.5;
//
//		heuristic = new AdGC(numberAmplifiers, linLosses, inputSignal, function);
//		heuristic.setInitialization(new UniformInitializationVOA(type));
//		heuristic.setSelectionOp(new UiaraWeightSelection());
//		heuristic.setVoaMaxAttenuation(simSet.getVOA_MAX_ATT());
//		heuristic.setRoadmAttenuation(simSet.getROADM_ATT());
//		heuristic.setMaxOutputPower(maxPout);
//
//		// When the selection uses weight
//		if (heuristic.getSelectionOp() instanceof UiaraWeightSelection) {
//			((UiaraWeightSelection) heuristic.getSelectionOp()).setNFWeight(nfWeight);
//			((UiaraWeightSelection) heuristic.getSelectionOp()).setGFWeight(gfWeight);
//		}
//
//		amplifiers = heuristic.execute();
//		endSignal = heuristic.getMonitors()[numberAmplifiers - 1].getOutputSignal();
//
//		System.out.println("****** AdGC ******");
//		printResults(2, amplifiers, endSignal, nfMetric, gnliMetric, heuristic.calculateTilt(endSignal),
//				heuristic.calculateOSNR(endSignal));
//		//AdGC end
//		
//		heuristic = new AdGC(numberAmplifiers, linLosses, inputSignal, function);
//		heuristic.setInitialization(new UniformInitializationVOA(type));
//		heuristic.setSelectionOp(new OSNRWeightSelection());
//		heuristic.setVoaMaxAttenuation(simSet.getVOA_MAX_ATT());
//		heuristic.setRoadmAttenuation(simSet.getROADM_ATT());
//		heuristic.setMaxOutputPower(maxPout);
//
//		// When the selection uses weight
//		if (heuristic.getSelectionOp() instanceof OSNRWeightSelection) {
//		    ((OSNRWeightSelection) heuristic.getSelectionOp()).setNFWeight(nfWeight);
//		    ((OSNRWeightSelection) heuristic.getSelectionOp()).setGFWeight(gfWeight);
//		}
//
//		amplifiers = heuristic.execute();
//		endSignal = heuristic.getMonitors()[numberAmplifiers - 1].getOutputSignal();
//
//		System.out.println("****** AdGC NLI ******");
//		printResults(3, amplifiers, endSignal, nfMetric, gnliMetric, heuristic.calculateTilt(endSignal),
//				heuristic.calculateOSNR(endSignal));
//		//AdGC NLI end
//		
//		heuristic = new AsHBFlex(numberAmplifiers, linLosses, inputSignal, function);
//		heuristic.setSelectionOp(new UiaraWeightSelection());
//		heuristic.setInitialization(new UniformInitializationVOA(type));
//		heuristic.setVoaMaxAttenuation(simSet.getVOA_MAX_ATT());
//		heuristic.setRoadmAttenuation(simSet.getROADM_ATT());
//		heuristic.setMaxOutputPower(maxPout);
//		((AsHBFlex) heuristic).setMaxIteration(5);
//		// When the selection uses weight
//		if (heuristic.getSelectionOp() instanceof UiaraWeightSelection) {
//			((UiaraWeightSelection) heuristic.getSelectionOp()).setNFWeight(nfWeight);
//			((UiaraWeightSelection) heuristic.getSelectionOp()).setGFWeight(gfWeight);
//		}
//
//		amplifiers = heuristic.execute();
//		endSignal = heuristic.getMonitors()[numberAmplifiers - 1].getOutputSignal();
//
//		System.out.println("****** AsHB ******");
//		printResults(4, amplifiers, endSignal, nfMetric, gnliMetric, heuristic.calculateTilt(endSignal),
//				heuristic.calculateOSNR(endSignal));
//		//AsHB end
//		
//		heuristic = new AsHBFlex(numberAmplifiers, linLosses, inputSignal, function);
//		heuristic.setSelectionOp(new OSNRWeightSelection());
//		heuristic.setInitialization(new UniformInitializationVOA(type));
//		heuristic.setVoaMaxAttenuation(simSet.getVOA_MAX_ATT());
//		heuristic.setRoadmAttenuation(simSet.getROADM_ATT());
//		heuristic.setMaxOutputPower(maxPout);
//		((AsHBFlex) heuristic).setMaxIteration(5);
//		
//		// When the selection uses weight
//		if (heuristic.getSelectionOp() instanceof OSNRWeightSelection) {
//		    ((OSNRWeightSelection) heuristic.getSelectionOp()).setNFWeight(nfWeight);
//		    ((OSNRWeightSelection) heuristic.getSelectionOp()).setGFWeight(gfWeight);
//		}
//
//		amplifiers = heuristic.execute();
//		endSignal = heuristic.getMonitors()[numberAmplifiers - 1].getOutputSignal();
//
//		System.out.println("****** AsHB NLI ******");
//		printResults(5, amplifiers, endSignal, nfMetric, gnliMetric, heuristic.calculateTilt(endSignal),
//				heuristic.calculateOSNR(endSignal));
//		//AsHB NLI end
//		
//		heuristic = new LossComp(numberAmplifiers, linLosses, inputSignal, function);
//		((LossComp) heuristic).setFirstAmplifierTargetGain(18);
//		heuristic.setInitialization(new UniformInitializationVOA(type));
//		heuristic.setVoaMaxAttenuation(simSet.getVOA_MAX_ATT());
//		heuristic.setRoadmAttenuation(simSet.getROADM_ATT());
//		heuristic.setMaxOutputPower(maxPout);
//		amplifiers = heuristic.execute();
//		endSignal = heuristic.getMonitors()[numberAmplifiers - 1].getOutputSignal();
//
//		amplifiers[0].getGainPerChannel();
//
//		System.out.println("****** LossComp ******");
//		printResults(6, amplifiers, endSignal, nfMetric, gnliMetric, heuristic.calculateTilt(endSignal),
//				heuristic.calculateOSNR(endSignal));
//		//LossComp end
//		
//		heuristic = new Regressor("mlp", numberAmplifiers, linLosses, inputSignal, linkLength, functionAux);
//		heuristic.setInitialization(new UniformInitializationVOA(type));
//		heuristic.setVoaMaxAttenuation(simSet.getVOA_MAX_ATT());
//		heuristic.setRoadmAttenuation(simSet.getROADM_ATT());
//		heuristic.setMaxOutputPower(maxPout);
//		amplifiers = heuristic.execute();
//		endSignal = heuristic.getMonitors()[numberAmplifiers - 1].getOutputSignal();
//
//		amplifiers[0].getGainPerChannel();
//
//		System.out.println("****** MLPRegressor ******");
//		printResults(7, amplifiers, endSignal, nfMetric, gnliMetric, heuristic.calculateTilt(endSignal),
//				heuristic.calculateOSNR(endSignal));
//	}
//
//	private static void printResults(int index, Amplifier[] amplifiers, OpticalSignal endSignal, BeckerNoiseFigureMetric nfMetric,
//			GNLIMetric gnliMetric, double tilt, double OSNR) {
//		gnliMetric.evaluate(amplifiers);
//		System.out.println("NF\tTilt_OSNR\tO_NLI\tO_ASE");
//		System.out.printf("%2.3f\t%2.3f\t%2.3f\t%2.3f", nfMetric.evaluate(amplifiers), gnliMetric.getTiltOSNR_NLI(),
//				gnliMetric.worstOSNR_NLI(), gnliMetric.worstOSNR_ASE());
//		System.out.println();
//
//		for (int i = 0; i < amplifiers.length; i++) {
//			System.out.println(amplifiers[i]);
//		}
//		System.out.println();
//
//		//if(gnliMetric.getTiltOSNR_NLI() > 0 && gnliMetric.worstOSNR_NLI() > 0) {
//			rippleResults.get(index).add(gnliMetric.getTiltOSNR_NLI());
//			osnrResults.get(index).add(gnliMetric.worstOSNR_NLI());
//		//}
//	}
//
//}
