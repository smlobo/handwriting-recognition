package org.lobo;

import static org.lobo.NeuralNetwork.sigmoidPrime;

public class QuadraticCost implements Cost {
    @Override
    public void delta(double[] activations, int desired, double[] zValues, double[] answers) {
        for (int i = 0; i < activations.length; i++) {
            if (desired == i)
                answers[i] = activations[i] - 1.0;
            else
                answers[i] = activations[i] - 0.0;
            answers[i] *= sigmoidPrime(zValues[i]);
        }
    }
}
