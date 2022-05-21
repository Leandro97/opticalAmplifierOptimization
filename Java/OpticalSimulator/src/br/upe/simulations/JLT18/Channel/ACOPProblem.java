package br.upe.simulations.JLT18.Channel;

import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.BinaryIntegerVariable;
import org.moeaframework.core.variable.EncodingUtils;
import org.moeaframework.problem.AbstractProblem;

import br.upe.base.AmplifierType;
import br.upe.mascara.PowerMask;
import br.upe.mascara.PowerMaskFactory;

/**
 * Implementation of the ACOP function.
 */
public class ACOPProblem extends AbstractProblem {
	private ACOPCustomObject acopCO;
    private AmplifierType ampType;
    private static double MAX_VOA_ATT = 20.0f;

    /**
     * Constructs a new instance of the ACOPProblem function, defining it to
     * include 2*number of amplifier decision variables and 2 objectives.
     */
    public ACOPProblem(ACOPCustomObject acopCO, AmplifierType type) {
		super(acopCO.getNumberOfAmplifiers() * 2, 2);
		
		this.acopCO  = acopCO;
		this.ampType = type;
    }

    /**
     * Constructs a new solution and defines the bounds of the decision
     * variables.
     */
    @Override
    public Solution newSolution() {
	Solution solution = new Solution(getNumberOfVariables(), getNumberOfObjectives());

	PowerMask pm = PowerMaskFactory.getInstance().fabricatePowerMask(ampType);

	for (int i = 0; i < getNumberOfVariables(); i++) {
	    if (i < acopCO.getNumberOfAmplifiers()) {
		solution.setVariable(i, new BinaryIntegerVariable(pm.getMinGain(), pm.getMaxGain()));
	    } else {
		solution.setVariable(i, new BinaryIntegerVariable(0, (int) MAX_VOA_ATT));
	    }
	}

	return solution;
    }

    /**
     * Extracts the decision variables from the solution, evaluates the
     * function, and saves the resulting objective value back to the solution.
     */
    @Override
    public void evaluate(Solution solution) {
	int[] x = EncodingUtils.getInt(solution);
	double[] f = new double[numberOfObjectives];

	ACOP_MOOProblem problem = new ACOP_MOOProblem(ampType, acopCO);

	float[] gains = new float[acopCO.getNumberOfAmplifiers()];
	float[] attenuations = new float[acopCO.getNumberOfAmplifiers()];

	for (int i = 0; i < attenuations.length; i++) {
	    gains[i] = x[i];
	    attenuations[i] = x[i + acopCO.getNumberOfAmplifiers()];
	}

	double[] r = problem.evaluate(gains, attenuations);

	for (int i = 0; i < getNumberOfObjectives(); i++) {
	    if (r != null)
		f[i] = r[i];
	    else
		f[i] = Double.MAX_VALUE;
	}

	solution.setObjectives(f);
    }

    public static void main(String[] args) {
    ACOPCustomObject acopCO = new ACOPCustomObject(2, -30, 80);
	ACOPProblem problem = new ACOPProblem(acopCO, AmplifierType.EDFA_2);

	int[] values = { 23, 27, 27, 27, 15, 7, 9, 0 };

	Solution solution = new Solution(8, 2);
	for (int i = 0; i < values.length; i++) {
	    if (i < 4) {
		solution.setVariable(i, new BinaryIntegerVariable(values[i], 17, 27));
	    } else {
		solution.setVariable(i, new BinaryIntegerVariable(values[i], 0, (int) MAX_VOA_ATT));
	    }
	}
	
	problem.evaluate(solution);
    }

}