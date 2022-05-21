package br.upe.signal.factory.Leandro;

import java.util.ArrayList;

import br.upe.base.OpticalChannel;
import br.upe.base.OpticalSignal;
import br.upe.signal.factory.Leandro.ITUGridUniformSignal;

public class ChannelCreationTest {
	public static int[] channelNumberArray = { 10, 20, 40 };
	
	public static void main(String[] args) {
		
		for (int channelNumber : channelNumberArray) {
			System.out.println(channelNumber + " channels: ");
			ITUGridUniformSignal regularSignal     = new ITUGridUniformSignal("regular", channelNumber, 1.921e14, 1e11, -18, 50);
			ITUGridUniformSignal reversedSignal    = new ITUGridUniformSignal("reversed", channelNumber, 1.96e14, 1e11, -18, 50);
			ITUGridUniformSignal alternatingSignal = new ITUGridUniformSignal("alternating", channelNumber, 1.941e14, 1e11, -18, 50);
			ITUGridUniformSignal randomSignal      = new ITUGridUniformSignal("random", channelNumber, 1.921e14, 1e11, -18, 50);
			
			OpticalSignal inputSignal1           = regularSignal.createSignal();
			ArrayList<Double> regularFrequencies = getFrequencies(inputSignal1);
			printFrequencies(1.86e14, regularFrequencies);
			
			OpticalSignal inputSignal2            = reversedSignal.createSignal();
			ArrayList<Double> reversedFrequencies = getFrequencies(inputSignal2);
			printFrequencies(2.009e14, reversedFrequencies);
			
			OpticalSignal inputSignal3               = alternatingSignal.createSignal();
			ArrayList<Double> alternatingFrequencies = getFrequencies(inputSignal3);
			printFrequencies(1.941e14, alternatingFrequencies);
			
			OpticalSignal inputSignal4          = randomSignal.createRandomChannelSignal();
			ArrayList<Double> randomFrequencies = getFrequencies(inputSignal4);
			printFrequencies(1.86e14, randomFrequencies);
			
			System.out.println();
			System.out.println(randomSignal.getRandomChannelIndexes());
		}
	}
	
	public static ArrayList<Double> getFrequencies(OpticalSignal inputSignal) {
		ArrayList<Double> frequencies =  new ArrayList<Double>();
		
		for (OpticalChannel channel: inputSignal.getChannels()) {
			frequencies.add(channel.getFrequency());
		}
		
		return frequencies;
	}
	
	public static void printFrequencies(double initialFrequency, ArrayList<Double> frequencies) {
		System.out.println(frequencies);
		for (Double frequency: frequencies) {
			System.out.print(frequency - initialFrequency + " ");
		}
		System.out.println();
		System.out.println();
	}
}
