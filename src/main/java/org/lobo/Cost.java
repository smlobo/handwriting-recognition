package org.lobo;

public interface Cost {
    void delta(double[] activations, int desired, double[] zValues, double[] answers);
}
