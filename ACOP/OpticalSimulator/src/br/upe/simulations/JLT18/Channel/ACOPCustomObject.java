package br.upe.simulations.JLT18.Channel;

public class ACOPCustomObject {
	private String channelType      = "regular";
	private Double initialFerquency = 1.921e14;
	private int numberOfChannels    = 40;
	private int numberOfAmplifiers  = 4;
	private float inputPowerCh      = -18;
	private double linkLength       = 90;

	public ACOPCustomObject(int numberOfAmplifiers, String channelType, int numberOfChannels) {
		this.numberOfAmplifiers = numberOfAmplifiers;
		this.channelType        = channelType;
		this.numberOfChannels 	= numberOfChannels;
			
		if (channelType.equals("regular") || channelType.equals("random")) {
			this.initialFerquency = 1.921e14;
		} else if (channelType.equals("alternating")) {
			this.initialFerquency = 1.941e14;
		} else {
			this.initialFerquency = 1.96e14;
		}
	}
	
	public ACOPCustomObject(int numberOfAmplifiers, float inputPowerCh, double linkLength) {
		this.numberOfAmplifiers = numberOfAmplifiers;
		this.inputPowerCh	    = inputPowerCh;
		this.linkLength 		= linkLength;
	}

	public int getNumberOfChannels() {
		return numberOfChannels;
	}

	public void setNumberOfChannels(int numberOfChannels) {
		this.numberOfChannels = numberOfChannels;
	}
	
	public int getNumberOfAmplifiers() {
		return numberOfAmplifiers;
	}

	public void setNumberOfAmplifiers(int numberOfAmplifiers) {
		this.numberOfAmplifiers = numberOfAmplifiers;
	}

	public float getInputPowerCh() {
		return inputPowerCh;
	}

	public void setInputPowerCh(float inputPowerCh) {
		this.inputPowerCh = inputPowerCh;
	}

	public double getLinkLength() {
		return linkLength;
	}

	public void setLinkLength(double linkLength) {
		this.linkLength = linkLength;
	}

	public String getChannelType() {
		return channelType;
	}

	public void setChannelType(String channelType) {
		this.channelType = channelType;
	}

	public Double getInitialFerquency() {
		return initialFerquency;
	}

	public void setInitialFerquency(Double initialFerquency) {
		this.initialFerquency = initialFerquency;
	}
}
