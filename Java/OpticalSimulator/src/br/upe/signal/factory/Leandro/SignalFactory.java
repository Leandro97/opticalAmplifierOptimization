package br.upe.signal.factory.Leandro;

import java.util.ArrayList;

import br.upe.base.OpticalChannel;
import br.upe.base.OpticalSignal;

public abstract class SignalFactory {
	private int channelNumber;
	
	public SignalFactory(int channelNumber) {
		this.channelNumber = channelNumber;
	}
	
	public OpticalSignal createSignal(){
		ArrayList<OpticalChannel> channels = new ArrayList<OpticalChannel>(channelNumber);
		for(int i=0; i<channelNumber; i++){
			channels.add(createChannel(i));
		}
		
		return new OpticalSignal(channels);
	}
	
	public OpticalSignal createRandomChannelSignal(){
		ArrayList<OpticalChannel> channels = createRandomChannels(channelNumber);
		
		return new OpticalSignal(channels);
	}
	
	abstract protected OpticalChannel createChannel(int channelIndex);
	
	abstract protected ArrayList<OpticalChannel> createRandomChannels(int channelNumber);
	
	abstract protected ArrayList<Integer> getRandomChannelIndexes();
	
	abstract protected double calculateSignalPower(int channelIndex);
	
	abstract protected double calculateNoisePower(int channelIndex);
}
