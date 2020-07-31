package org.lobo;

public enum DataType {

    MNIST_DIGITS("train", "t10k", 10, '0', 0),
    EMNIST_LETTERS("emnist-letters-train", "emnist-letters-test", 26, 'A', 1);

    private final String trainName;
    private final String testName;
    private final int outputs;
    private final char mappingStart;
    private final int offset;           // Digit labels are 0-9; Letter labels are 1-26

    DataType(String trainName, String testName, int outputs, char mappingStart, int offset) {
        this.trainName = trainName;
        this.testName = testName;
        this.outputs = outputs;
        this.mappingStart = mappingStart;
        this.offset = offset;
    }

    public static DataType getType(String arg) {
        switch (arg) {
            case "1":
                return MNIST_DIGITS;
            case "2":
                return EMNIST_LETTERS;
            default:
                System.out.println("Bad data type -> " + arg);
                System.exit(1);
        }

        return null;
    }

    public String trainName() {
        return trainName;
    }

    public String testName() {
        return testName;
    }

    public int outputs() {
        return outputs;
    }

    public char mappingStart() {
        return mappingStart;
    }

    public int offset() {
        return offset;
    }

    public String toString() {
        return "{" + trainName + ", " + testName + ", " + outputs + ", " + mappingStart + ", " + offset + "}";
    }
}
