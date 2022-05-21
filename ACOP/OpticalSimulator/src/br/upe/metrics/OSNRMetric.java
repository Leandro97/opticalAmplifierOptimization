package br.upe.metrics;

import br.upe.base.Amplifier;
import br.upe.base.OpticalChannel;
import br.upe.base.OpticalSignal;
import br.upe.util.DecibelConverter;

public class OSNRMetric implements Metric {

    private OpticalSignal signal;

    public OpticalSignal getSignal() {
	return signal;
    }

    private float[] linkLosses;

    public OSNRMetric(OpticalSignal inputSignal, float[] linkLosses) {
	super();
	this.signal = inputSignal.clone();
	this.linkLosses = linkLosses;
    }

    @Override
    public double evaluate(Amplifier[] link) {
	for (int i = 0; i < link.length; i++) {
	    signal = link[i].transferFunction(signal);

	    if (i < link.length - 1)
		linkTrasferFunction(linkLosses[i]);
	}

	return calculateOSNR();
    }

    private void linkTrasferFunction(float linkLoss) {
	for (OpticalChannel c : signal.getChannels()) {
	    // Signal Total Gain
	    double signalLin = DecibelConverter.toLinearScale(c.getSignalPower());
	    signalLin *= DecibelConverter.toLinearScale(-1 * linkLoss);
	    // Noise Gain
	    double noiseLin = DecibelConverter.toLinearScale(c.getNoisePower());
	    noiseLin *= DecibelConverter.toLinearScale(-1 * linkLoss);

	    c.setSignalPower(DecibelConverter.toDecibelScale(signalLin));
	    c.setNoisePower(DecibelConverter.toDecibelScale(noiseLin));
	}
    }

    private double calculateOSNR() {
	double minOSNR = Double.MAX_VALUE;
	for (OpticalChannel c : signal.getChannels()) {
	    double signalLin = DecibelConverter.toLinearScale(c.getSignalPower());
	    double noiseLin = DecibelConverter.toLinearScale(c.getNoisePower());
	    double OSNR = signalLin / noiseLin;

	    if (OSNR < minOSNR) {
		minOSNR = OSNR;
	    }
	}

	return DecibelConverter.toDecibelScale(minOSNR);
    }

}
