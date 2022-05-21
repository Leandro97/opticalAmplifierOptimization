package br.upe.simulations.JLT18.Channel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

import javax.script.ScriptException;

import br.upe.base.ACOPHeuristic;
import br.upe.base.Amplifier;
import br.upe.base.AmplifierType;
import br.upe.base.AmplifierVOA;
import br.upe.base.ObjectiveFunction;
import br.upe.base.OpticalChannel;
import br.upe.base.OpticalSignal;
import br.upe.heuristics.lossComp.LossComp;
import br.upe.initializations.UniformInitialization;
import br.upe.mascara.PowerMask;
import br.upe.mascara.PowerMaskFactory;
import br.upe.objfunctions.linerInterpolation.LinearInterpolationFunction;
import br.upe.signal.factory.ITUGridUniformSignal;
import br.upe.signal.tracker.AmplifierSignalMonitor;
import br.upe.simulations.simsetups.SimSetAMPVOA;
import br.upe.simulations.simsetups.SimulationSetup;

public class Regressor extends ACOPHeuristic {
	private String regressor;
	private float firstAmplifierTargetGain    = 0.0f;
	private ArrayList<Integer> channelIndexes;
	
	public static float[][] gainMedians = {{22.0f, 22.0f}, {23.0f, 23.0f, 24.0f}, {23.0f, 23.0f, 23.0f, 21.0f}};
	public static float[][] lossMedians = {{2.0f, 0.0f}, {3.0f, 7.0f, 0.0f}, {3.0f, 2.0f, 1.0f, 1.0f}};
	
	
	public ArrayList<Float> predict(String regressor, Object[] objects)
			throws IOException {
		String aux = "";
		
		for (Object ch : objects) {
			aux += "&channelList=" + ch.toString();
		}
		
		URL url = new URL("http://127.0.0.1:5000/predict?regressor=" + regressor + aux);

		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");

		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));

		String[] strArray             = in.readLine().split(",");
		ArrayList<Float> resultArray = new ArrayList();

		for (String result : strArray) {
			resultArray.add(Float.parseFloat(result)); 
		}
		return resultArray;
	}

	public Regressor(String regressor, 
					 int numberOfAmplifiers, 
					 float[] linkLosses, 
					 OpticalSignal inputSignal, 
					 ArrayList<Integer> channelIndexes,
					 ObjectiveFunction function) throws IOException {
		super(numberOfAmplifiers, linkLosses, inputSignal, function);
		monitors[0] = new AmplifierSignalMonitor();
		monitors[0].setInputSignal(inputSignal);
		
		this.regressor      = regressor;
		this.channelIndexes = channelIndexes;
		
		URL url = new URL("http://127.0.0.1:5000/createModel?regressor=" + regressor + "&ampNumber=" + numberOfAmplifiers);

		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");
		
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
	}

	public void setFirstAmplifierTargetGain(float gain) {
		firstAmplifierTargetGain = gain;
	}
	
	@Override
	public Amplifier[] execute() {
		this.amplifiers = initialization.initialize(numberOfAmplifiers, linkInputPower, linkOutputPower, linkLosses,
				function, monitors[0].getInputSignal());
		
		ArrayList<Float> line = new ArrayList<Float>();
		
		try {
			line = predict(regressor, channelIndexes.toArray());			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for (int i = 0; i < numberOfAmplifiers; i++) {			
			float gain = line.get(i * 2);
			float loss = line.get(i * 2 + 1);
			
			if(gain - loss < 15) {				
				gain = 24;
				loss = 1;
			}
			
			if (gain > 24) {
				gain = 24;
			}
			
			// updating the output power of the current amplifier
			amplifiers[i].setGain(Math.round(17));
			((AmplifierVOA) amplifiers[i]).setVoaOutAttenuation(Math.round(0));
			function.defineNewOperationPoint(amplifiers[i], monitors[i].getInputSignal());

			// updating the output power of the current amplifier
			amplifiers[i].setGain(Math.round(gain));
			((AmplifierVOA) amplifiers[i]).setVoaOutAttenuation(Math.round(loss)); 
			function.defineNewOperationPoint(amplifiers[i], monitors[i].getInputSignal());
			
			// Last amplifier
			if (i + 1 == numberOfAmplifiers) {
				if (amplifiers[i].getOutputPower() > linkOutputPower + voaMaxAttenuation + roadmAttenuation) {
					int diff = Math.round(
							amplifiers[i].getOutputPower() - (linkOutputPower + voaMaxAttenuation + roadmAttenuation));
					amplifiers[i].setGain(amplifiers[i].getGain() - diff);
					function.defineNewOperationPoint(amplifiers[i], monitors[i].getInputSignal());
				}

				float ampVoaAtt = ((AmplifierVOA) amplifiers[i]).getVoaOutAttenuation();
				float voaAttenuation = (float) (amplifiers[i].getOutputPower() - ampVoaAtt - linkInputPower
						- roadmAttenuation);
				((AmplifierVOA) amplifiers[i]).increaseVoaOutAttenuation(voaAttenuation);
			}
				
			OpticalSignal outputSignal = amplifiers[i].transferFunction(monitors[i].getInputSignal());
			
			if(outputSignal == null) {
				System.out.println();
			}
			
			monitors[i].setOutputSignal(outputSignal);

			if (i + 1 < numberOfAmplifiers) {
				// updating the input power of the next amplifier
				OpticalSignal result = super.linkTrasferFunction(monitors[i].getOutputSignal(), linkLosses[i]);
				amplifiers[i + 1].setInputPower(result.getTotalPower());
				// Save the input power of this amplifier
				monitors[i + 1] = new AmplifierSignalMonitor();
				monitors[i + 1].setInputSignal(result);
			}
		}

		return amplifiers;
	}

	private float applyRestriction(float targetGain, Amplifier amplifier) {
		PowerMask pm = PowerMaskFactory.getInstance().fabricatePowerMask(amplifier.getType());

		if (targetGain < pm.getMinGain()) {
			return pm.getMinGain();
		} else if (targetGain > pm.getMaxGain()) {
			return pm.getMaxGain();
		}

		return targetGain;
	}
}
