package org.lobo;

public enum DataType {

    MNIST_DIGITS("train", "t10k", 10, 0) {
        @Override
        public char mapping(int i) {
            return (char) (i - offset() + (int) '0');
        }
    },
    EMNIST_LETTERS("emnist-letters-train", "emnist-letters-test", 26, 1) {
        @Override
        public char mapping(int i) {
            return (char) (i - offset() + (int) 'A');
        }
    },
    EMNIST_CHARS("emnist-balanced-train", "emnist-balanced-test", 47, 0) {
        @Override
        public char mapping(int i) {
            // 0-9 -> 0-9
            if (i <= 9)
                return (char) (i + (int) '0');
            // 10-35 => A-Z
            else if (i <= 35)
                return (char) (i - 10 + (int) 'A');

            // 36-46 are somewhat random
            switch (i) {
                case 36:
                    return 'a';
                case 37:
                    return 'b';
                case 38:
                    return 'd';
                case 39:
                    return 'e';
                case 40:
                    return 'f';
                case 41:
                    return 'g';
                case 42:
                    return 'h';
                case 43:
                    return 'n';
                case 44:
                    return 'q';
                case 45:
                    return 'r';
                case 46:
                    return 't';
            }
            return '-';
        }
    };

    private final String trainName;
    private final String testName;
    private final int outputs;
    private final int offset;           // Digit labels are 0-9; Letter labels are 1-26

    public abstract char mapping(int i);

    DataType(String trainName, String testName, int outputs, int offset) {
        this.trainName = trainName;
        this.testName = testName;
        this.outputs = outputs;
        this.offset = offset;
    }

    public static DataType getType(String arg) {
        switch (arg) {
            case "1":
                return MNIST_DIGITS;
            case "2":
                return EMNIST_LETTERS;
            case "3":
                return EMNIST_CHARS;
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

    public int offset() {
        return offset;
    }

    public String toString() {
        return "{" + trainName + ", " + testName + ", " + outputs + ", " + offset + "}";
    }
}
