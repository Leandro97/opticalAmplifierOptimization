package br.upe.signal.factory.Leandro;

import br.upe.util.DecibelConverter;

public class ITUGridUniformSignal extends ITUSignalFactory {
	private double signalPower;
	private double OSNR;
	
	public ITUGridUniformSignal(String type, int channelNumber, double initialFrequency, double channelSpacing, double signalPower, double OSNR) {
		super(type, channelNumber, initialFrequency, channelSpacing);
		this.signalPower = signalPower;
		this.OSNR = OSNR;
	}

	@Override
	protected double calculateSignalPower(int channelIndex) {
		return this.signalPower;
	}

	@Override
	protected double calculateNoisePower(int channelIndex) {
		double signalLin = DecibelConverter.toLinearScale(signalPower);
		double osnrLin = DecibelConverter.toLinearScale(OSNR);
		double noiseLin = signalLin/osnrLin;
		
		return DecibelConverter.toDecibelScale(noiseLin);
	}

}
