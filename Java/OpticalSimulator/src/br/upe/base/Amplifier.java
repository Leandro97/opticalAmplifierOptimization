package br.upe.base;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import br.upe.mascara.OperatingPoint;
import br.upe.objfunctions.linerInterpolation.LinearInterpolationFunction;
import br.upe.signal.factory.ITUGridUniformSignal;
import br.upe.util.DecibelConverter;

public class Amplifier implements IOpticalDevice {
    /**
     * Constante de Planck
     */
    public static final double H = 6.626068e-34;

    /**
     * Largura de banda �ptica que ser� considerada no projeto.
     */
    public static final double B0 = 1.25e10;

    private float inputPower; // dBm
    private float outputPower; // dBm
    private float noiseFigure; // dB
    private float flatness; // dB
    private float gain; // dB
    private float powerConsumption; // W
    private float maskOSNR;
    private AmplifierType type;
    private HashMap<Double, Float> gainPerChannel;
    private HashMap<Double, Float> noiseFigurePerChannel;

    /**
     * @return the type
     */
    public AmplifierType getType() {
	return type;
    }

    /**
     * @param type
     *            the type to set
     */
    public void setType(AmplifierType type) {
	this.type = type;
    }

    /**
     * @return the gain (dB)
     */
    public float getGain() {
	return gain;
    }

    /**
     * @param gain
     *            the gain to set
     */
    public void setGain(float gain) {
	this.gain = gain;
    }

    /**
     * @return the flatness
     */
    public float getFlatness() {
	return flatness;
    }

    /**
     * @param flatness
     *            the flatness to set
     */
    public void setFlatness(float flatness) {
	this.flatness = flatness;
    }

    /**
     * @return the inputPower
     */
    public float getInputPower() {
	return inputPower;
    }

    /**
     * @param inputPower
     *            the inputPower to set
     */
    public void setInputPower(float _inputPower) {
	this.inputPower = _inputPower;
    }

    /**
     * @return the outputPower
     */
    public float getOutputPower() {
	return outputPower;
    }

    /**
     * @param outputPower
     *            the outputPower to set
     */
    public void setOutputPower(float outputPower) {
	this.outputPower = outputPower;
    }

    /**
     * @return the noiseFigure
     */
    public float getNoiseFigure() {
	return noiseFigure;
    }

    /**
     * @param noiseFigure
     *            the noiseFigure to set
     */
    public void setNoiseFigure(float noiseFigure) {
	this.noiseFigure = noiseFigure;
    }

    public float getPowerConsumption() {
	return powerConsumption;
    }

    public void setPowerConsumption(float powerConsumption) {
	this.powerConsumption = powerConsumption;
    }

    private float defineMaxOutputPower(AmplifierType type) {
	switch (type) {
	case L17_10CH:
	case L17_20CH:
	case L17_40CH:
	case L17_80CH:
	    return (float) 17.0;
	case L21_40CH:
	    return (float) 21.0;
	default:
	    return -1;
	}
    }

    public Amplifier(float inputPower, AmplifierType type) {
	this.inputPower = inputPower;
	this.type = type;
    }

    public Amplifier(OperatingPoint operatingPoint, AmplifierType type) {
	this.inputPower = operatingPoint.getTotalInputPower();
	this.type = type;
	this.gain = operatingPoint.getGainSet();
	this.outputPower = inputPower + gain;
	this.gainPerChannel = operatingPoint.getGainPerChannel();
	this.noiseFigurePerChannel = operatingPoint.getNoiseFigurePerChannel();
    }

    public Amplifier(float inputPower, float gain, AmplifierType type) {
	this.inputPower = inputPower;
	this.type = type;
	this.outputPower = inputPower + gain;

	this.gain = gain;
    }

    public Amplifier(float inputPower, float gain, float outputPower, float noiseFigure, float flatness,
	    float powerConsumption, AmplifierType type) {
	this.inputPower = inputPower;
	this.outputPower = outputPower;
	this.noiseFigure = noiseFigure;
	this.flatness = flatness;
	this.gain = gain;
	this.type = type;
	this.powerConsumption = powerConsumption;
    }

    public Amplifier clone() {
	return new Amplifier(inputPower, gain, outputPower, noiseFigure, flatness, powerConsumption, type);
    }

    @Override
    public String toString() {
	NumberFormat nf = NumberFormat.getInstance();
	nf.setMaximumFractionDigits(2);
	nf.setMinimumFractionDigits(2);

	StringBuffer strBuff = new StringBuffer();
	strBuff.append("[" + nf.format(this.inputPower) + "\t");
	strBuff.append(nf.format(this.gain) + "\t");
	strBuff.append(nf.format(this.outputPower) + "\t");
	strBuff.append(nf.format(this.noiseFigure) + "\t");
	strBuff.append(nf.format(this.flatness) + "\t");
	strBuff.append(nf.format(this.maskOSNR) + "]");

	return strBuff.toString();
    }

    public void calculateGain() {
	this.gain = Math.round(this.outputPower - this.inputPower);
    }

    /**
     * Amplifier transfer function. Alters the optical signal according to the
     * amplifier operating point. Changes the signal and noise power of each
     * channels individually.
     */
    public OpticalSignal transferFunction(OpticalSignal signal) {
	OpticalSignal result = signal.clone();
	
//	if(gainPerChannel == null) {
//		return null;
//	}
	
	for (OpticalChannel c : result.getChannels()) {
	    // Signal Gain
	    double channelGain = calculateChannelFeature(c.getFrequency(), gainPerChannel);
	    double signalLin = DecibelConverter.toLinearScale(c.getSignalPower());
	    signalLin *= channelGain;
	    // Noise Gain
	    double noiseLin = DecibelConverter.toLinearScale(c.getNoisePower());
	    noiseLin *= channelGain;
	    // ASE Added
	    noiseLin += calculateAsePower(c.getFrequency(), channelGain);

	    c.setSignalPower(DecibelConverter.toDecibelScale(signalLin));
	    c.setNoisePower(DecibelConverter.toDecibelScale(noiseLin));
	}

	return result;
    }

    /**
     * Calculates the channel ASE power
     * 
     * @param frequency
     *            in Hz
     * @param channelGain
     * @return Added noise in mW
     */
    protected double calculateAsePower(double frequency, double channelGain) {
	double noiseFactor = calculateChannelFeature(frequency, noiseFigurePerChannel);
	double addedNoisinW = 1000 * noiseFactor * (channelGain - 1) * H * frequency * B0;
	return addedNoisinW;
    }

    /**
     * Calculates a channel feature (gain or noise figure). Considers
     * frequencies not characterized.
     * 
     * @param frequency
     *            The channel frequency
     * @param featurePerChannel
     *            HasMap with frequencyXfeature
     * @return The channel feature (gain or noise figure)
     */
    protected double calculateChannelFeature(double frequency, HashMap<Double, Float> featurePerChannel) {    
	if (featurePerChannel.containsKey(frequency)) {
	    return DecibelConverter.toLinearScale(featurePerChannel.get(frequency));
	} else {
	    Collection<Double> frequencys = featurePerChannel.keySet();
	    List<Double> freqList = new ArrayList<Double>(frequencys);
	    Collections.sort(freqList);

	    double freqMin = Double.MAX_VALUE, freqMax = Double.MAX_VALUE;

	    for (int i = 0; i < freqList.size(); i++) {
		if (frequency < freqList.get(i)) {
		    if (i == 0) {
			if (Math.abs(frequency - freqList.get(i)) > 50e9) // greater
			    // than
			    // 50GHz
			    throw new RuntimeException("Frequency " + frequency + " is out of the mask!");
			else
			    freqMin = freqMax = freqList.get(i);
			break;
		    }

		    freqMin = freqList.get(i - 1);
		    freqMax = freqList.get(i);
		    break;
		} else if (i == freqList.size() - 1) {
		    if (Math.abs(frequency - freqList.get(i)) > 50e9) // greater
			// than
			// 50GHz
			throw new RuntimeException("Frequency " + frequency + " is out of the mask!");
		    else
			freqMin = freqMax = freqList.get(i);
		}
	    }

	    double factorX = 0;
	    if (freqMin != freqMax)
		factorX = (frequency - freqMin) / (freqMax - freqMin);

	    double featureLinMin = DecibelConverter.toLinearScale(featurePerChannel.get(freqMin));
	    double featureLinMax = DecibelConverter.toLinearScale(featurePerChannel.get(freqMax));

	    return ((1 - factorX) * featureLinMin + factorX * featureLinMax);
	}
    }

    public HashMap<Double, Float> getGainPerChannel() {
    	return gainPerChannel;
    }

    public void setGainPerChannel(HashMap<Double, Float> gainPerChannel) {
	this.gainPerChannel = gainPerChannel;
    }

    public HashMap<Double, Float> getNoiseFigurePerChannel() {
	return noiseFigurePerChannel;
    }

    public void setNoiseFigurePerChannel(HashMap<Double, Float> noiseFigurePerChannel) {
	this.noiseFigurePerChannel = noiseFigurePerChannel;
    }

    public static void main(String[] args) {
	LinearInterpolationFunction function = new LinearInterpolationFunction();

	float linkInputPower = DecibelConverter.calculateInputPower(40, -26.31f);

	ITUGridUniformSignal signal = new ITUGridUniformSignal(40, 1.92103e14, 100e9, -26.31f, 30);
	OpticalSignal inputSignal = signal.createSignal();

	int ganho = 30;
	Amplifier amplifier = new AmplifierVOA(linkInputPower, ganho, (linkInputPower + ganho), 0.0f, 0.0f, 0.0f,
		AmplifierType.EDFA_1_STG);

	function.defineNewOperationPoint(amplifier, inputSignal);

	amplifier.transferFunction(inputSignal);
    }

    public double getMaskOSNR() {
	return this.maskOSNR;
    }

    public void setMaskOSNR(float osnr) {
	this.maskOSNR = osnr;
    }
}
