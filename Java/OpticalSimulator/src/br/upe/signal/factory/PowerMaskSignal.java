package br.upe.signal.factory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import br.upe.base.AmplifierType;
import br.upe.base.OpticalChannel;
import br.upe.mascara.PowerMask;
import br.upe.mascara.PowerMaskFactory;
import br.upe.util.DecibelConverter;

public class PowerMaskSignal extends SignalFactory {
	private int channelNumber;
	private double signalPower;
	private double OSNR;
	private List<Double> frequencys;

	public PowerMaskSignal(int channelNumber, AmplifierType type, double signalPower, double OSNR) {
		super(channelNumber);
		
		this.channelNumber = channelNumber;
		this.signalPower = signalPower;
		this.OSNR = OSNR;

		loadFrequencys(type);
	}

	private void loadFrequencys(AmplifierType type) {
		PowerMask pm = PowerMaskFactory.getInstance().fabricatePowerMask(type);

		Set<Double> frequencys = pm.getOperatingPoints().get(0).getGainPerChannel().keySet();
		List<Double> opFreqList = new ArrayList<Double>(frequencys);
		Collections.sort(opFreqList);

		this.frequencys = opFreqList;
	}

	@Override
	protected OpticalChannel createChannel(int channelIndex) {
		return new OpticalChannel(frequencys.get(channelIndex), calculateSignalPower(channelIndex),
				calculateNoisePower(channelIndex));
	}

	@Override
	protected double calculateSignalPower(int channelIndex) {
		return this.signalPower;
	}

	@Override
	protected double calculateNoisePower(int channelIndex) {
		double signalLin = DecibelConverter.toLinearScale(signalPower);
		double osnrLin = DecibelConverter.toLinearScale(OSNR);
		double noiseLin = signalLin / osnrLin;

		return DecibelConverter.toDecibelScale(noiseLin);
	}

	public ArrayList<Integer> getChannelIndexes() {
		ArrayList<Integer> indexes = new ArrayList<Integer>();
		
		for(int i = 0; i < 40; i ++) {
			if (i < channelNumber) {
				indexes.add(1);
			} else {
				indexes.add(0);
			}
		}
		
		return indexes;
	}
}
