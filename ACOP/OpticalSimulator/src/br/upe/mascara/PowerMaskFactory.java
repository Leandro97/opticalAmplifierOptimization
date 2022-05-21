package br.upe.mascara;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;

import br.upe.base.Amplifier;
import br.upe.base.AmplifierType;
import br.upe.metrics.GNLIMetric;
import br.upe.util.DecibelConverter;

public class PowerMaskFactory {

    private static final double C = 299792458; // m/s

    /**
     * Power mask files paths
     */
    private static String P14_PATH = "./masks/05_Com Tilt/P14/";
    private static String B24_PATH = "./masks/05_Com Tilt/B24/";
    private static String B21_L21_PATH = "./masks/05_Com Tilt/B21_L21/";
    private static String B21_L24_PATH = "./masks/05_Com Tilt/B21_L24/";
    private static String EDFA_1_STG_PATH = "./masks/05_Com Tilt/EDFA_1_STG/";
    private static String EDFA_1_STG_PAR_PATH = "./masks/05_Com Tilt/EDFA_1_STG_FreqPar_Filt/";
    private static String EDFA_1_STG_IMPAR_PATH = "./masks/05_Com Tilt/EDFA_1_STG_FreqImpar_Filt/";
    private static String EDFA_2_STG_PATH = "./masks/05_Com Tilt/EDFA_2_STG/";
    private static String EDFA_2_2_STG_PATH = "./masks/05_Com Tilt/EDFA_2_2_STG/";
    private static String EDFA_2_PATH = "./masks/05_Com Tilt/EDFA_2/";
    private static String EDFA_1_PATH = "./masks/05_Com Tilt/EDFA_1/";
    private static String EDFA_1_T1_PATH = "./masks/05_Com Tilt/EDFA_1_Tilt1/";
    private static String EDFA_1_T2_PATH = "./masks/05_Com Tilt/EDFA_1_Tilt2/";
    private static String EDFA_1_T3_PATH = "./masks/05_Com Tilt/EDFA_1_Tilt3/";
    private static String EDFA_1_T4_PATH = "./masks/05_Com Tilt/EDFA_1_Tilt4/";
    private static String EDFA_1_T5_PATH = "./masks/05_Com Tilt/EDFA_1_Tilt5/";
    private static String EDFA_1_T6_PATH = "./masks/05_Com Tilt/EDFA_1_Tilt6/";
    private static String EDFA_1_T7_PATH = "./masks/05_Com Tilt/EDFA_1_Tilt7/";
    private static String EDFA_1_T8_PATH = "./masks/05_Com Tilt/EDFA_1_Tilt8/";
    private static String EDFA_1_T9_PATH = "./masks/05_Com Tilt/EDFA_1_Tilt9/";
    private static String EDFA_1_T10_PATH = "./masks/05_Com Tilt/EDFA_1_Tilt10/";
    private static String EDFA_1_T11_PATH = "./masks/05_Com Tilt/EDFA_1_Tilt11/";
    private static String EDFA_1_T12_PATH = "./masks/05_Com Tilt/EDFA_1_Tilt12/";
    private static String EDFA_1_T13_PATH = "./masks/05_Com Tilt/EDFA_1_Tilt13/";
    private static String EDFA_1_T14_PATH = "./masks/05_Com Tilt/EDFA_1_Tilt14/";
    private static String EDFA_1_T15_PATH = "./masks/05_Com Tilt/EDFA_1_Tilt15/";
    private static String EDFA_1_T16_PATH = "./masks/05_Com Tilt/EDFA_1_Tilt16/";
    private static String EDFA_1_T17_PATH = "./masks/05_Com Tilt/EDFA_1_Tilt17/";
    private static String EDFA_1_T20_PATH = "./masks/05_Com Tilt/EDFA_1_Tilt20/";
    private static String EDFA_1_T21_PATH = "./masks/05_Com Tilt/EDFA_1_Tilt21/";
    private static String EDFA_1_T25_PATH = "./masks/05_Com Tilt/EDFA_1_Tilt25/";
    private static String EDFA_1_T30_PATH = "./masks/05_Com Tilt/EDFA_1_Tilt30/";
    private static String EDFA_2_T2_PATH = "./masks/05_Com Tilt/EDFA_2_Tilt2/";
    private static String EDFA_1_Tm1_PATH = "./masks/05_Com Tilt/EDFA_1_Tilt-1/";
    private static String EDFA_1_Tm2_PATH = "./masks/05_Com Tilt/EDFA_1_Tilt-2/";
    private static String EDFA_1_Tm3_PATH = "./masks/05_Com Tilt/EDFA_1_Tilt-3/";
    private static String EDFA_1_Tm4_PATH = "./masks/05_Com Tilt/EDFA_1_Tilt-4/";
    private static String EDFA_1_Tm5_PATH = "./masks/05_Com Tilt/EDFA_1_Tilt-5/";
    private static String EDFA_1_Tm6_PATH = "./masks/05_Com Tilt/EDFA_1_Tilt-6/";
    private static String EDFA_1_Tm7_PATH = "./masks/05_Com Tilt/EDFA_1_Tilt-7/";
    private static String EDFA_1_Tm8_PATH = "./masks/05_Com Tilt/EDFA_1_Tilt-8/";
    private static String EDFA_1_Tm9_PATH = "./masks/05_Com Tilt/EDFA_1_Tilt-9/";
    private static String EDFA_1_Tm10_PATH = "./masks/05_Com Tilt/EDFA_1_Tilt-10/";
    private static String EDFA_1_Tm11_PATH = "./masks/05_Com Tilt/EDFA_1_Tilt-11/";
    private static String EDFA_1_Tm12_PATH = "./masks/05_Com Tilt/EDFA_1_Tilt-12/";
    private static String EDFA_1_Tm13_PATH = "./masks/05_Com Tilt/EDFA_1_Tilt-13/";
    private static String EDFA_1_Tm14_PATH = "./masks/05_Com Tilt/EDFA_1_Tilt-14/";
    private static String EDFA_1_Tm15_PATH = "./masks/05_Com Tilt/EDFA_1_Tilt-15/";
    private static String EDFA_1_Tm16_PATH = "./masks/05_Com Tilt/EDFA_1_Tilt-16/";
    private static String EDFA_1_Tm17_PATH = "./masks/05_Com Tilt/EDFA_1_Tilt-17/";
    private static String EDFA_1_Tm18_PATH = "./masks/05_Com Tilt/EDFA_1_Tilt-18/";
    private static String EDFA_1_Tm19_PATH = "./masks/05_Com Tilt/EDFA_1_Tilt-19/";
    private static String EDFA_1_Tm20_PATH = "./masks/05_Com Tilt/EDFA_1_Tilt-20/";
    private static String EDFA_1_Tm22_PATH = "./masks/05_Com Tilt/EDFA_1_Tilt-22/";
    private static String EDFA_1_Tm25_PATH = "./masks/05_Com Tilt/EDFA_1_Tilt-25/";
    private static String EDFA_1_Tm26_PATH = "./masks/05_Com Tilt/EDFA_1_Tilt-26/";
    private static String EDFA_1_Tm30_PATH = "./masks/05_Com Tilt/EDFA_1_Tilt-30/";

    private static String EDFA_2_Tm0v5_PATH = "./masks/05_Com Tilt/EDFA_2_Tilt-0,5/";
    private static String EDFA_2_Tm1_PATH = "./masks/05_Com Tilt/EDFA_2_Tilt-1/";
    private static String EDFA_2_Tm1v5_PATH = "./masks/05_Com Tilt/EDFA_2_Tilt-1,5/";
    private static String EDFA_2_Tm2_PATH = "./masks/05_Com Tilt/EDFA_2_Tilt-2/";
    private static String EDFA_2_Tm2v5_PATH = "./masks/05_Com Tilt/EDFA_2_Tilt-2,5/";
    private static String EDFA_2_Tm3_PATH = "./masks/05_Com Tilt/EDFA_2_Tilt-3/";
    private static String EDFA_2_Tm3v5_PATH = "./masks/05_Com Tilt/EDFA_2_Tilt-3,5/";
    private static String EDFA_2_Tm4_PATH = "./masks/05_Com Tilt/EDFA_2_Tilt-4/";
    private static String EDFA_2_Tm4v5_PATH = "./masks/05_Com Tilt/EDFA_2_Tilt-4,5/";
    private static String EDFA_2_Tm5_PATH = "./masks/05_Com Tilt/EDFA_2_Tilt-5/";
    private static String EDFA_2_Tm8_PATH = "./masks/05_Com Tilt/EDFA_2_Tilt-8/";

    /**
     * Store one power mask for each amplifier type.
     */
    private static HashMap<AmplifierType, PowerMask> powerMasks;

    private static PowerMaskFactory instance;

    public static PowerMaskFactory getInstance() {
	if (instance == null) {
	    instance = new PowerMaskFactory();
	}

	return instance;
    }

    private PowerMaskFactory() {
	powerMasks = new HashMap<AmplifierType, PowerMask>();
    }

    /**
     * Fabricates and returns a power mask according to the amplifier type
     * 
     * @param type
     *            Optical amplifier type
     * @return The amplifier power mask
     */
    public PowerMask fabricatePowerMask(AmplifierType type) {
	String dirPath = "";

	PowerMask pm = powerMasks.get(type);

	if (pm == null) {
	    switch (type) {
	    case P14_L21:
		dirPath = P14_PATH;
		break;
	    case B24:
		dirPath = B24_PATH;
		break;
	    case B21_L21:
		dirPath = B21_L21_PATH;
		break;
	    case B21_L24:
		dirPath = B21_L24_PATH;
		break;
	    case EDFA_1_STG:
		dirPath = EDFA_1_STG_PATH;
		break;
	    case EDFA_1_STG_PAR:
		dirPath = EDFA_1_STG_PAR_PATH;
		break;
	    case EDFA_1_STG_IMPAR:
		dirPath = EDFA_1_STG_IMPAR_PATH;
		break;
	    case EDFA_2_STG:
		dirPath = EDFA_2_STG_PATH;
		break;
	    case EDFA_2_2_STG:
		dirPath = EDFA_2_2_STG_PATH;
		break;
	    case EDFA_2:
		dirPath = EDFA_2_PATH;
		break;
	    case EDFA_1:
		dirPath = EDFA_1_PATH;
		break;
	    case EDFA_1_T1:
		dirPath = EDFA_1_T1_PATH;
		break;
	    case EDFA_1_T2:
		dirPath = EDFA_1_T2_PATH;
		break;
	    case EDFA_1_T3:
		dirPath = EDFA_1_T3_PATH;
		break;
	    case EDFA_1_T4:
		dirPath = EDFA_1_T4_PATH;
		break;
	    case EDFA_1_T5:
		dirPath = EDFA_1_T5_PATH;
		break;
	    case EDFA_1_T6:
		dirPath = EDFA_1_T6_PATH;
		break;
	    case EDFA_1_T7:
		dirPath = EDFA_1_T7_PATH;
		break;
	    case EDFA_1_T8:
		dirPath = EDFA_1_T8_PATH;
		break;
	    case EDFA_1_T9:
		dirPath = EDFA_1_T9_PATH;
		break;
	    case EDFA_1_T10:
		dirPath = EDFA_1_T10_PATH;
		break;
	    case EDFA_1_T11:
		dirPath = EDFA_1_T11_PATH;
		break;
	    case EDFA_1_T12:
		dirPath = EDFA_1_T12_PATH;
		break;
	    case EDFA_1_T13:
		dirPath = EDFA_1_T13_PATH;
		break;
	    case EDFA_1_T14:
		dirPath = EDFA_1_T14_PATH;
		break;
	    case EDFA_1_T15:
		dirPath = EDFA_1_T15_PATH;
		break;
	    case EDFA_1_T16:
		dirPath = EDFA_1_T16_PATH;
		break;
	    case EDFA_1_T17:
		dirPath = EDFA_1_T17_PATH;
		break;
	    case EDFA_1_T20:
		dirPath = EDFA_1_T20_PATH;
		break;
	    case EDFA_1_T21:
		dirPath = EDFA_1_T21_PATH;
		break;
	    case EDFA_1_T25:
		dirPath = EDFA_1_T25_PATH;
		break;
	    case EDFA_1_T30:
		dirPath = EDFA_1_T30_PATH;
		break;
	    case EDFA_1_Tm1:
		dirPath = EDFA_1_Tm1_PATH;
		break;
	    case EDFA_1_Tm2:
		dirPath = EDFA_1_Tm2_PATH;
		break;
	    case EDFA_1_Tm3:
		dirPath = EDFA_1_Tm3_PATH;
		break;
	    case EDFA_1_Tm4:
		dirPath = EDFA_1_Tm4_PATH;
		break;
	    case EDFA_1_Tm5:
		dirPath = EDFA_1_Tm5_PATH;
		break;
	    case EDFA_1_Tm6:
		dirPath = EDFA_1_Tm6_PATH;
		break;
	    case EDFA_1_Tm7:
		dirPath = EDFA_1_Tm7_PATH;
		break;
	    case EDFA_1_Tm8:
		dirPath = EDFA_1_Tm8_PATH;
		break;
	    case EDFA_1_Tm9:
		dirPath = EDFA_1_Tm9_PATH;
		break;
	    case EDFA_1_Tm10:
		dirPath = EDFA_1_Tm10_PATH;
		break;
	    case EDFA_1_Tm11:
		dirPath = EDFA_1_Tm11_PATH;
		break;
	    case EDFA_1_Tm12:
		dirPath = EDFA_1_Tm12_PATH;
		break;
	    case EDFA_1_Tm13:
		dirPath = EDFA_1_Tm13_PATH;
		break;
	    case EDFA_1_Tm14:
		dirPath = EDFA_1_Tm14_PATH;
		break;
	    case EDFA_1_Tm15:
		dirPath = EDFA_1_Tm15_PATH;
		break;
	    case EDFA_1_Tm16:
		dirPath = EDFA_1_Tm16_PATH;
		break;
	    case EDFA_1_Tm17:
		dirPath = EDFA_1_Tm17_PATH;
		break;
	    case EDFA_1_Tm18:
		dirPath = EDFA_1_Tm18_PATH;
		break;
	    case EDFA_1_Tm19:
		dirPath = EDFA_1_Tm19_PATH;
		break;
	    case EDFA_1_Tm20:
		dirPath = EDFA_1_Tm20_PATH;
		break;
	    case EDFA_1_Tm22:
		dirPath = EDFA_1_Tm22_PATH;
		break;
	    case EDFA_1_Tm25:
		dirPath = EDFA_1_Tm25_PATH;
		break;
	    case EDFA_1_Tm26:
		dirPath = EDFA_1_Tm26_PATH;
		break;
	    case EDFA_1_Tm30:
		dirPath = EDFA_1_Tm30_PATH;
		break;
	    case EDFA_2_Tm0v5:
		dirPath = EDFA_2_Tm0v5_PATH;
		break;
	    case EDFA_2_Tm1:
		dirPath = EDFA_2_Tm1_PATH;
		break;
	    case EDFA_2_Tm1v5:
		dirPath = EDFA_2_Tm1v5_PATH;
		break;
	    case EDFA_2_Tm2:
		dirPath = EDFA_2_Tm2_PATH;
		break;
	    case EDFA_2_Tm2v5:
		dirPath = EDFA_2_Tm2v5_PATH;
		break;
	    case EDFA_2_Tm3:
		dirPath = EDFA_2_Tm3_PATH;
		break;
	    case EDFA_2_Tm3v5:
		dirPath = EDFA_2_Tm3v5_PATH;
		break;
	    case EDFA_2_Tm4:
		dirPath = EDFA_2_Tm4_PATH;
		break;
	    case EDFA_2_Tm4v5:
		dirPath = EDFA_2_Tm4v5_PATH;
		break;
	    case EDFA_2_Tm5:
		dirPath = EDFA_2_Tm5_PATH;
		break;
	    case EDFA_2_Tm8:
		dirPath = EDFA_2_Tm8_PATH;
		break;
	    case EDFA_2_T2:
		dirPath = EDFA_2_T2_PATH;
		break;
	    default:
		dirPath = P14_PATH;
		break;
	    }

	    pm = readPowerMask(dirPath);

	    powerMasks.put(type, pm);
	}

	return pm;
    }

    /**
     * Power mask file parser
     * 
     * @param path
     *            Power mask file path
     * @return Power mask
     */
    private PowerMask readPowerMask(String path) {
	ArrayList<OperatingPoint> opsList = new ArrayList<OperatingPoint>();
	int minGain = Integer.MAX_VALUE, maxGain = Integer.MIN_VALUE;
	float minPin = Float.MAX_VALUE, maxPin = Float.NEGATIVE_INFINITY;
	HashMap<Integer, Float> minTotalPinPerGain = new HashMap<Integer, Float>();
	HashMap<Integer, Float> maxTotalPinPerGain = new HashMap<Integer, Float>();
	float minFrequency = Float.MAX_VALUE, maxFrequency = Float.MIN_VALUE;
	float minNF = Float.MAX_VALUE, maxNF = Float.MIN_VALUE;
	float minGainChannel = Float.MAX_VALUE, maxGainChannel = Float.MIN_VALUE;

	try {
	    File file = new File(path + "TotalInfo.txt");
	    Scanner reader = new Scanner(file);

	    while (reader.hasNextLine()) {
		String[] line = reader.nextLine().split("\t"); // G, A, Pin,
		// Pout, NF, Ripple

		OperatingPoint op = new OperatingPoint();
		op.setTotalInputPower(Float.parseFloat(line[2])); // Pin
		op.setGainSet(Integer.parseInt(line[0])); // Gset

		// Updating min and max gain
		if (op.getGainSet() < minGain)
		    minGain = op.getGainSet();
		if (op.getGainSet() > maxGain)
		    maxGain = op.getGainSet();

		// Updating min and max Pin
		if (op.getTotalInputPower() < minPin)
		    minPin = op.getTotalInputPower();
		if (op.getTotalInputPower() > maxPin)
		    maxPin = op.getTotalInputPower();

		// Updating max pin per gain
		if (maxTotalPinPerGain.containsKey(op.getGainSet())) {
		    float maxPinG = maxTotalPinPerGain.get(op.getGainSet());
		    if (op.getTotalInputPower() > maxPinG)
			maxTotalPinPerGain.put(op.getGainSet(), op.getTotalInputPower());
		} else {
		    maxTotalPinPerGain.put(op.getGainSet(), op.getTotalInputPower());
		}

		// Updating min pin per gain
		if (minTotalPinPerGain.containsKey(op.getGainSet())) {
		    float minPinG = maxTotalPinPerGain.get(op.getGainSet());
		    if (op.getTotalInputPower() < minPinG)
			minTotalPinPerGain.put(op.getGainSet(), op.getTotalInputPower());
		} else {
		    minTotalPinPerGain.put(op.getGainSet(), op.getTotalInputPower());
		}

		int aParameter = Integer.parseInt(line[1]);

		File filePinPeak = new File(path + "PinPeak_G" + op.getGainSet() + "_A" + aParameter + ".txt");
		File filePoutPeak = new File(path + "PoutPeak_G" + op.getGainSet() + "_A" + aParameter + ".txt");
		File fileNF = new File(path + "NF_G" + op.getGainSet() + "_A" + aParameter + ".txt");

		float constantNF = 0;
		Scanner readerNF = null;
		if (!fileNF.exists()) {
		    constantNF = Float.parseFloat(line[4]); // NF from TotalInfo
		} else {
		    readerNF = new Scanner(fileNF);
		}

		float labRipple = Float.parseFloat(line[5]); // Ripple from
		// TotalInfo

		Scanner readerPin = new Scanner(filePinPeak);
		Scanner readerPout = new Scanner(filePoutPeak);

		HashMap<Double, Float> pinPerChannel = new HashMap<Double, Float>();
		HashMap<Double, Float> gainPerChannel = new HashMap<Double, Float>();
		HashMap<Double, Float> nfPerChannel = new HashMap<Double, Float>();

		while (readerPin.hasNextLine()) {
		    String[] linePin = readerPin.nextLine().split("\t");
		    String[] linePout = readerPout.nextLine().split("\t");

		    double wavelength = Float.parseFloat(linePin[0]); // Frequency
		    double frequency = ((C / wavelength));
		    frequency = (int) (frequency);
		    frequency *= 1e9;
		    float pinCh = Float.parseFloat(linePin[1]);
		    float poutCh = Float.parseFloat(linePout[1]);

		    float gain = poutCh - pinCh; // Calculate gain using Pin and
		    // Pout

		    pinPerChannel.put(frequency, pinCh);
		    gainPerChannel.put(frequency, gain);

		    float nf = constantNF;
		    if (readerNF != null) {
			String[] lineNF = readerNF.nextLine().split("\t");
			nf = Float.parseFloat(lineNF[1]);

			// If nf is less than 3 dB, it was an error in the
			// characterization
			if (nf < 3)
			    nf += 3; // Sum 3 dB or duplicate
		    }
		    nfPerChannel.put(frequency, nf);

		    // Updating min and max frequency
		    if (frequency < minFrequency)
			minFrequency = (float) frequency;
		    if (frequency > maxFrequency)
			maxFrequency = (float) frequency;

		    // Updating min and max gain channel
		    if (gain < minGainChannel)
			minGainChannel = (float) gain;
		    if (gain > maxGainChannel)
			maxGainChannel = (float) gain;

		    // Updating min and max NF channel
		    if (nf < minNF)
			minNF = (float) nf;
		    if (nf > maxNF)
			maxNF = (float) nf;
		}

		readerPin.close();
		readerPout.close();

		if (readerNF != null)
		    readerNF.close();

		op.setInputPowerPerChannel(pinPerChannel);
		op.setGainPerChannel(gainPerChannel);
		op.setNoiseFigurePerChannel(nfPerChannel);
		op.setLabRipple(labRipple);

		opsList.add(op);
	    }

	    reader.close();
	} catch (FileNotFoundException e) {
	    e.printStackTrace();
	    return null;
	}

	PowerMask pm = new PowerMask();
	pm.setOperatingPoints(opsList);
	pm.setMaxGain(maxGain);
	pm.setMinGain(minGain);
	pm.setMaxTotalInputPower(maxPin);
	pm.setMinTotalInputPower(minPin);
	pm.setMinTotalPinPerGain(minTotalPinPerGain);
	pm.setMaxTotalPinPerGain(maxTotalPinPerGain);
	pm.setMaxFrequency(maxFrequency);
	pm.setMinFrequency(minFrequency);
	pm.setMaxGainPerChannel(maxGainChannel);
	pm.setMinGainPerChannel(minGainChannel);
	pm.setMaxNoiseFigure(maxNF);
	pm.setMinNoiseFigure(minNF);
	return pm;
    }

    /**
     * Transforms a power mask in its worst case version, considering NLI. It
     * means that all gain channels are equal to the gain_set, all the channels
     * nf are equal to the worst nf and that all channels GNLIare equal to the
     * worst GNLI.
     * 
     * @param originalPM
     * @return A PM updated with the gain_set and worst NF_ase+nli
     */
    public PowerMask fabricateWorstCasePMwNLI(AmplifierType type){
	//Fetch the original mask
	PowerMask pmTemp = fabricatePowerMask(type);
	PowerMask pm = null;
	try {
	    pm = (PowerMask) pmTemp.clone();
	} catch (CloneNotSupportedException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

	//For each operating point
	for(OperatingPoint op : pm.getOperatingPoints()){
	    // Calculate the worst NF
	    float worstNF = calculateWorstNF(op);

	    // HashMap<Double, Float> gnliPerChannel = new HashMap<Double,
	    // Float>();

	    for (Double freq : op.getNoiseFigurePerChannel().keySet()) {
		// Update all the channels NF to worst NF
		op.getNoiseFigurePerChannel().put(freq, worstNF);
		// Update all the channels gain to Gset
		op.getGainPerChannel().put(freq, (float) op.getGainSet());
		// Update all the channels GNLI to worst GNLI
		// gnliPerChannel.put(freq, worstGNLI);
	    }

	    // Calculate the worst GNLI
	    // float worstGNLI = (float) calculateWorstGNLI(op, type);

	    // op.setGnliPerChannel(gnliPerChannel);
	}
	return pm;
    }

    private float calculateWorstNF(OperatingPoint op) {
	float maxNF = 0;
	for (Double freq : op.getNoiseFigurePerChannel().keySet()) {
	    float tempNF = op.getNoiseFigurePerChannel().get(freq);
	    if (maxNF < tempNF) {
		maxNF = tempNF;
	    }
	}

	return maxNF;
    }

    private double calculateWorstGNLI(OperatingPoint op, AmplifierType type) {
	Set<Double> freqSet = op.getInputPowerPerChannel().keySet();
	ArrayList<Double> frequencys = new ArrayList<Double>(freqSet);
	Collections.sort(frequencys);

	int numberCh = frequencys.size();
	float pinSystem = (float) op.getInputPowerPerChannel().get(frequencys.get(0));

	double linkLength = 100000; // 100km
	GNLIMetric gnliMetric = new GNLIMetric(28e9, 100e9, numberCh, pinSystem, linkLength);

	// Creating optical link with two amplifiers
	Amplifier[] amplifiers = new Amplifier[2];

	// The first amplifier is the one that will have its OSNR calculated
	amplifiers[0] = new Amplifier(op, type);

	// The second amplifier is a dummy amplifier, just to use the IGN
	// without errors
	HashMap<Double, Float> gainPerChannel = new HashMap<Double, Float>();
	HashMap<Double, Float> nfPerChannel = new HashMap<Double, Float>();
	for (int i = 0; i < frequencys.size(); i++) {
	    gainPerChannel.put(frequencys.get(i), 0f); // Dummy gain: 0 dB
	    nfPerChannel.put(frequencys.get(i), 1f); // Dummy nf: 1 dB
	}
	amplifiers[1] = new Amplifier(pinSystem, 0, pinSystem, 1, 0, 0, type);
	amplifiers[1].setGainPerChannel(gainPerChannel);
	amplifiers[1].setNoiseFigurePerChannel(nfPerChannel);

	if (op.getTotalInputPower() < -27.0f && op.getGainSet() == 27)
	    System.out.println();

	// Calling the IGN
	gnliMetric.evaluate(amplifiers);
	System.out.print(op + " ");
	System.out.print(gnliMetric.evaluate(amplifiers) + "\t");
	System.out.print(gnliMetric.worstOSNR_NLI() + "\t" + gnliMetric.worstOSNR_ASE() + "\n");

	double[] osnrNLI = gnliMetric.getOSNR_NLI();
	double[] nfNLI = new double[osnrNLI.length];
	double worstNF = 3;
	for (int i = 0; i < osnrNLI.length; i++) {
	    nfNLI[i] = DecibelConverter.toLinearScale(pinSystem);
	    nfNLI[i] /= DecibelConverter.toLinearScale(osnrNLI[i]) * 6.6256e-34 * frequencys.get(i) * 28e9
		    * DecibelConverter.toLinearScale(op.getGainPerChannel().get(frequencys.get(i)));

	    nfNLI[i] = DecibelConverter.toDecibelScale(nfNLI[i]);

	    if(nfNLI[i] > worstNF)
		worstNF = nfNLI[i];
	}

	// System.out.println(worstNF);

	return gnliMetric.worstOSNR_NLI();
    }

    public static void main(String[] args) {
	PowerMaskFactory pmf = new PowerMaskFactory();


	// pmf.fabricatePowerMask(AmplifierType.EDFA_1_STG);
	PowerMask pm = pmf.fabricateWorstCasePMwNLI(AmplifierType.EDFA_2_2_STG);

	System.out.println();
    }
}