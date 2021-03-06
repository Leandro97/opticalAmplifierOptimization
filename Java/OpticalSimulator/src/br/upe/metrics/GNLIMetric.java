package br.upe.metrics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import br.upe.base.Amplifier;
import br.upe.base.AmplifierVOA;

public class GNLIMetric implements Metric {

    // Constants
    private static final double h = 6.6256e-34; // Planck's constant [J/s]
    private static final double nu = 299792458 / 1550e-9; // light frequency
    // [Hz]
    private static final double dB2Neper = 10 / Math.log(10);

    // System parameters
    private static final double D = 16.65; // ps/nm/km
    private static final double gamma = 1.3e-3; // 1/W/m
    private static final double alpha = 0.2e-3; // fibre attenuation (dB/m)
    private static final int N_pol = 2; // number of polarizations

    private double B_signal;
    private double delta_f;
    private int N_channel;
    private double P_Tx_dBm;
    private double Length;
    private List<Double> freq;

    // Other Results
    private double[] SNR_NLI;
    private double[] SNR_ASE;
    private double[] OSNR_NLI;
    private double[] OSNR_EDFA;
    private double[] NOISE_NLI;
    private double[] SIGNAL;

    public GNLIMetric(double signalBandwidth, double channelSpacing, int numberCh, double pinSystem,
	    double spanLength) {
	this.B_signal = signalBandwidth;
	this.delta_f = channelSpacing;
	this.N_channel = numberCh;
	this.P_Tx_dBm = pinSystem;
	this.Length = spanLength;
    }

    private double[][] getNFdB(Amplifier[] link) {
	double[][] nfdB = new double[link.length][N_channel];

	for (int i = 0; i < nfdB.length; i++) {
	    Collection<Double> frequencys = link[i].getNoiseFigurePerChannel().keySet();
	    List<Double> opFreqList = new ArrayList<Double>(frequencys);
	    Collections.sort(opFreqList);

	    for (int j = 0; j < nfdB[i].length; j++) {
	    	nfdB[i][j] = link[i].getNoiseFigurePerChannel().get(opFreqList.get(j));
	    }

	    if (i == 0)
		freq = opFreqList;
	}

	return nfdB;
    }

    private double[][] getGaindB(Amplifier[] link) {
	double[][] gaindB = new double[link.length][N_channel];

	for (int i = 0; i < gaindB.length; i++) {
	    Collection<Double> frequencys = link[i].getGainPerChannel().keySet();
	    List<Double> opFreqList = new ArrayList<Double>(frequencys);
	    Collections.sort(opFreqList);

	    float voaLoss = 0;
	    if (link[i] instanceof AmplifierVOA) {
		voaLoss = ((AmplifierVOA) link[i]).getVoaOutAttenuation();
	    }

	    for (int j = 0; j < gaindB[i].length; j++) {
		gaindB[i][j] = link[i].getGainPerChannel().get(opFreqList.get(j)) - voaLoss;

		// System.out.print(gaindB[i][j] + ", ");
	    }
	    // System.out.println();
	}

	return gaindB;
    }

    @Override
    public double evaluate(Amplifier[] link) {
	double[] powerVec = new double[N_channel];
	double[] G_tx_ch = new double[N_channel];
	double[] G_NLI = new double[N_channel];

	double[][] NFdB = getNFdB(link);
	double[][] GaindB = getGaindB(link);

	// Set launch power per channel first span
	for (int i = 0; i < powerVec.length; i++) {
	    powerVec[i] = P_Tx_dBm + GaindB[0][i];
	}


	// Some more quantities
	// double B_total = N_channel*B_signal; //total system bandwidth
	double beta2 = -(1550e-9*1550e-9) * (D*1e-6) / (2*Math.PI*3e8);	//propagation constant
	double L_eff = ((1-Math.exp(-(alpha/dB2Neper)*Length))/(alpha/dB2Neper)); //effective length [m]
	double L_effa = 1/(alpha/dB2Neper); // asymptotic effective length [m]

	for (int i = 0; i < N_channel; i++) {
	    G_tx_ch[i] = Math.pow(10.0, (powerVec[i] - 30) / 10.0) / B_signal; // [W/Hz]
	}

	//// C???LCULO DA ASE ////
	// Coeficiente da Ase gerada no 1??? amp
	double[] ASE_Coef = new double[N_channel];
	for (int i = 0; i < ASE_Coef.length; i++) {
	    ASE_Coef[i] = Math.pow(10, NFdB[0][i] / 10) / 2 * (Math.pow(10, GaindB[0][i] / 10) - 1);
	}

	for (int i = 1; i < link.length; i++) {
	    // Coeficiente da Ase gerada no i-???simo amp somada ??? ASE propagante
	    // dos spans anteriores.
	    for (int j = 0; j < ASE_Coef.length; j++) {
		ASE_Coef[j] = Math.pow(10, NFdB[i][j] / 10) / 2 * (Math.pow(10, GaindB[i][j] / 10) - 1)
			+ ASE_Coef[j] * Math.pow(10, ((GaindB[i][j] - alpha * Length) / 10));
	    }
	}

	double ASE[] = new double[N_channel];
	for (int i = 0; i < ASE_Coef.length; i++) {
	    ASE[i] = N_pol * h * nu * ASE_Coef[i]; // Eq. (50) Ase total na
	    // s???ida
	    // do ???ltimo amp.
	}

	//// IGN model	////
	double[][] G_NLI_sum_ch = new double[N_channel][link.length - 1];


	for (int i = 0; i < N_channel; i++) {
	    for (int j = 0; j < link.length - 1; j++) {
		for (int n = 0; n < N_channel; n++) {
		    double[] temp = new double[1];
		    temp[0] = 1;
		    if(j>0){
			temp = new double[j];

			for (int k = 0; k < temp.length; k++) {
			    temp[k] = Math
				    .exp(1 / dB2Neper * (2 * GaindB[k + 1][n] + GaindB[k + 1][i] - 3 * alpha * Length));
			}
		    }

		    // prod(exp(1/dB2Neper.*(2*GaindB(n,2:j)+GaindB(i,2:j)-3*alpha*Length)))
		    G_NLI_sum_ch[n][j] = prod(temp);

		    temp = new double[link.length - j - 1];
		    for (int k = 0; k < temp.length; k++) {
			temp[k] = Math.exp(1 / dB2Neper * (GaindB[k + j + 1][i] - alpha * Length));
		    }

		    // prod(exp(1/dB2Neper.*(GaindB(i,j+1:N_spans+1)-alpha*Length)))
		    G_NLI_sum_ch[n][j] *= prod(temp);

		    // G_tx_ch(n)^2*G_tx_ch(i)*(2-double(kroneckerDelta(sym(n-i))))
		    G_NLI_sum_ch[n][j] *= G_tx_ch[n] * G_tx_ch[n] * G_tx_ch[i] * (2 - kroneckerDelta(n - i));

		    // (1/(4*pi*abs(beta2)*(alpha/dB2Neper)^-1))
		    G_NLI_sum_ch[n][j] *= (1.0 / (4.0 * Math.PI * Math.abs(beta2) * Math.pow((alpha / dB2Neper), -1)));

		    //(asinh(pi^2*(alpha/dB2Neper)^-1*abs(beta2)*(f_ch(n)-f_ch(i)+B_signal/2)*B_signal)
		    // -
		    // asinh(pi^2*(alpha/dB2Neper)^-1*abs(beta2)*(f_ch(n)-f_ch(i)-B_signal/2)*B_signal)
		    G_NLI_sum_ch[n][j] *= (asinh(Math.PI * Math.PI * L_effa * Math.abs(beta2)
			    * (freq.get(n) - freq.get(i) + B_signal / 2.0) * B_signal)
			    - asinh(Math.PI * Math.PI * Math.pow(alpha / dB2Neper, -1) * Math.abs(beta2)
				    * (freq.get(n) - freq.get(i) - B_signal / 2.0) * B_signal));
		}
	    }

	    G_NLI[i] = (16.0 / 27.0) * gamma * gamma * L_eff * L_eff * sum(G_NLI_sum_ch);
	}

	SNR_NLI = new double[N_channel];
	SNR_ASE = new double[N_channel];
	NOISE_NLI = new double[N_channel];
	SIGNAL = new double[N_channel];

	//SNR calculation
	for (int i = 0; i < N_channel; i++) {
	    double[] temp = new double[link.length - 1];
	    for (int j = 0; j < temp.length; j++) {
		temp[j] = Math.pow(10, (GaindB[j + 1][i] - alpha * Length) / 10.0);
	    }

	    SIGNAL[i] = 10.0 * Math.log10(G_tx_ch[i] * prod(temp));
	    NOISE_NLI[i] = 10.0 * Math.log10(ASE[i] + G_NLI[i]);

	    SNR_NLI[i] = 10.0 * Math.log10(G_tx_ch[i] * prod(temp) / (ASE[i] + G_NLI[i]));
	    SNR_ASE[i] = 10.0 * Math.log10(G_tx_ch[i] * prod(temp) / ASE[i]);
	}

	double SNR2OSNR = 10 * Math.log10(B_signal / 12.5e9 * N_pol / 2.0);

	// OSNR calculation
	OSNR_NLI = new double[N_channel];
	OSNR_EDFA = new double[N_channel];
	for (int i = 0; i < SNR_NLI.length; i++) {
	    OSNR_NLI[i] = SNR_NLI[i] + SNR2OSNR;
	    OSNR_EDFA[i] = SNR_ASE[i] + SNR2OSNR;
	    SIGNAL[i] += SNR2OSNR;
	    NOISE_NLI[i] += SNR2OSNR;
	}

	return worstGNLI(G_NLI);
    }

    private double worstGNLI(double[] v) {
	int maxIndex = 0;
	for (int i = 1; i < v.length; i++) {
	    if(v[i] > v[maxIndex])
		maxIndex = i;
	}
	
	return v[maxIndex];
    }

    public double worstOSNR_NLI() {
	int minIndex = 0;
	for (int i = 1; i < OSNR_NLI.length; i++) {
	    if (OSNR_NLI[i] < OSNR_NLI[minIndex])
		minIndex = i;
	}

	return OSNR_NLI[minIndex];
    }

    public double worstNF_NLI() {
	int minIndex = 0;
	for (int i = 1; i < OSNR_NLI.length; i++) {
	    if (OSNR_NLI[i] < OSNR_NLI[minIndex])
		minIndex = i;
	}

	double NF = P_Tx_dBm / (OSNR_NLI[minIndex] * h * freq.get(minIndex) * 1.25e10);
	return NF;
    }

    public double getTiltOSNR_NLI() {
	int minIndex = 0, maxIndex = 0;
	for (int i = 1; i < OSNR_NLI.length; i++) {
	    if (OSNR_NLI[i] < OSNR_NLI[minIndex])
		minIndex = i;

	    if (OSNR_NLI[i] > OSNR_NLI[minIndex])
		maxIndex = i;
	}

	return OSNR_NLI[maxIndex] - OSNR_NLI[minIndex];
    }

    public double worstOSNR_ASE() {
	int minIndex = 0;
	for (int i = 1; i < OSNR_EDFA.length; i++) {
	    if (OSNR_EDFA[i] < OSNR_EDFA[minIndex])
		minIndex = i;
	}

	return OSNR_EDFA[minIndex];
    }

    private double sum(double[][] v) {
	double sum = 0;

	for (int i = 0; i < v.length; i++) {
	    for (int j = 0; j < v[i].length; j++) {
		sum += v[i][j];
	    }
	}

	return sum;
    }

    private int kroneckerDelta(int m) {
	if (m == 0)
	    return 1;
	return 0;
    }

    private double prod(double[] v) {
	if (v.length < 1)
	    return 1;

	double product = v[0];
	for (int i = 1; i < v.length; i++) {
	    product *= v[i];
	}

	return product;
    }

    private double asinh(double x) {
	return Math.log(x + Math.sqrt(x * x + 1.0));
    }

    public double[] getSNR_NLI() {
	return SNR_NLI;
    }

    public double[] getSNR_ASE() {
	return SNR_ASE;
    }

    public double[] getOSNR_NLI() {
	return OSNR_NLI;
    }

    public double[] getOSNR_EDFA() {
	return OSNR_EDFA;
    }
}
