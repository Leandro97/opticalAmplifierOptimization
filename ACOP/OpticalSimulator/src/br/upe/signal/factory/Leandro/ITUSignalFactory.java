package br.upe.signal.factory.Leandro;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import br.upe.base.OpticalChannel;

public abstract class ITUSignalFactory extends SignalFactory {

	private static final int C = 299792458; //m/s
	
	private String type;
	private double initialFrequency = 192.10e12f; //THz
	private double channelSpacing;
	private int channelNumber;
	private OpticalChannel[] opticalChannelResult = {};
	private ArrayList<Integer> randomChannelIndexes;
	
	/**
	 * 
	 * @param channelNumber
	 * @param initialFrequency in Hz
	 * @param channelSpacing in Hz
	 * @param signalPower in dBm
	 * @param OSNR in dB
	 */
	public ITUSignalFactory(String type, int channelNumber, double initialFrequency, double channelSpacing) {
		super(channelNumber);
		this.type             = type;
		this.channelNumber    = channelNumber;
		this.initialFrequency = initialFrequency;
		this.channelSpacing   = channelSpacing;
	}

	@Override
	protected OpticalChannel createChannel(int channelIndex) {
		double frequency = 0;
		
		if(type.equals("regular")) {
			frequency = channelSpacing*channelIndex + initialFrequency;
		}
		
		if(type.equals("reversed")){
			frequency = initialFrequency - channelSpacing*channelIndex;
		}
		
		if(type.equals("alternating")){
			if (channelIndex % 2 != 0) {
				frequency        = initialFrequency - channelSpacing*channelIndex;
				initialFrequency = frequency;
			} else {
				frequency        = initialFrequency + channelSpacing*channelIndex;
				initialFrequency = frequency;
			}
		}
		
		return new OpticalChannel(frequency, calculateSignalPower(channelIndex), calculateNoisePower(channelIndex));
	}
	
	@Override
	protected ArrayList<OpticalChannel> createRandomChannels(int channelNumber) {
		ArrayList<OpticalChannel> channels = new ArrayList<OpticalChannel>();
		randomChannelIndexes               = new ArrayList<>(40);
		
		for (int i = 0; i < 40; i++){
			randomChannelIndexes.add(i);
		}
		
		Collections.shuffle(randomChannelIndexes, new Random(1));
		randomChannelIndexes.subList(0, 40 - channelNumber).clear();
		
		for (int i : randomChannelIndexes) {
			double frequency          = initialFrequency + channelSpacing*i;
			OpticalChannel newChannel = new OpticalChannel(frequency, calculateSignalPower(i), calculateNoisePower(i));
			
			channels.add(newChannel);
		}
		return channels;
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
	
	public ArrayList<Integer> getRandomChannelIndexes() {
		ArrayList<Integer> indexes = new ArrayList<Integer>();
		
		for(int i = 0; i < 40; i ++) {
			if (randomChannelIndexes.contains(i)) {
				indexes.add(1);
			} else {
				indexes.add(0);
			}
		}
		
		return indexes;
	}
}
