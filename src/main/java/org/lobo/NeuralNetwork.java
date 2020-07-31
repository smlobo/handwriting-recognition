package org.lobo;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Scanner;

public class NeuralNetwork {
    private static final double ETA = 3.0;
    private static final int L2SIZE = 15;

    // Layer 1 node activations (the input image)
    private static double[] l1Activations = new double[784];

    // Layer 1 -> 2 weights
    private static double[][] l12Weights = new double[784][L2SIZE];
    // Layer 1 -> 2 nabla weights
    private static double[][] l12NablaWeights = new double[784][L2SIZE];

    // Layer 2 biases
    private static double[] l2Biases = new double[L2SIZE];
    // Layer 2 nabla biases
    private static double[] l2NablaBiases = new double[L2SIZE];

    // Layer 2 z-values
    private static double[] l2ZValues = new double[L2SIZE];

    // Layer 2 activations
    private static double[] l2Activations = new double[L2SIZE];

    // Layer 2 -> 3 weights
    private static double[][] l23Weights = new double[L2SIZE][];
    // Layer 2 -> 3 nabla weights
    private static double[][] l23NablaWeights = new double[L2SIZE][];

    // Layer 3 biases
    private static double[] l3Biases;
    // Layer 3 nabla biases
    private static double[] l3NablaBiases;

    // Layer 3 z-values
    private static double[] l3ZValues;

    // Layer 3 activations
    private static double[] l3Activations;

    private static Random random = new Random();

    private static DataType dataType;
    private static LabelImageData trainingData;
    private static LabelImageData testData;

    public static void main(String[] args) throws Exception {
        // At least 1 arg is expected - the type of data
        if (args.length < 1) {
            System.out.println("Usage: java NeuralNetwork <data-type> [<num-of-epochs>]");
            System.exit(1);
        }

        // Parse the 1st arg
        dataType = DataType.getType(args[0]);
        System.out.println("Using data: " + dataType);

        // Default epochs are 1. Unless specified on the command line.
        int epochs = 1;
        if (args.length == 2) {
            epochs = Integer.parseInt(args[1]);
        }
        System.out.println("Running with " + epochs + " epochs.");

        // Initialize the network with random "standard normal" values
        initialize();

        trainingData = new LabelImageData(dataType.trainName());
        testData = new LabelImageData(dataType.testName());

        // Iterate over epochs
        for (int i = 0; i < epochs; i++) {
            System.out.println("Epoch #" + i);

            // Train the network
            //trainNetwork();
            trainNetworkRandom();

            // Test the network
            //testNetwork();
            testNetworkAuto();
        }

        // Interactive test
        //testNetwork();
    }

    private static void trainNetwork() throws Exception {
        InputStream trainLabelStream = new BufferedInputStream(
                new FileInputStream(Constants.TRAIN_LABELS));
        int trainLabelSize = DisplayImage.verifyLabelFile(trainLabelStream);

        InputStream trainImageStream = new BufferedInputStream(
                new FileInputStream(Constants.TRAIN_IMAGES));
        int trainImageSize = DisplayImage.verifyImageFile(trainImageStream);

        assert(trainLabelSize == trainImageSize);

        //System.out.println("Training with " + trainLabelSize + " images.");

        // Iterate over all the data
        for (int i = 0; i < trainLabelSize; i++) {
            //for (int i = 0; i < 10; i++) {
            if (i % 10000 == 0) {
                //System.out.println("Training index: " + i);
            }

            int label = DisplayImage.getLabel(trainLabelStream, i);
            int[][] image = DisplayImage.getImage(trainImageStream, i);

            backPropagate(image, label);
            if (i % 10000 == 0) {
                //printBiases(l3Activations);
            }
        }

        trainLabelStream.close();
        trainImageStream.close();
    }

    private static void trainNetworkRandom() {
        ArrayList<LabelImagePair> dataList = trainingData.data();
        Collections.shuffle(dataList);
        for (LabelImagePair pair : dataList) {
            backPropagate(pair.image(), pair.label());
        }
    }

    private static void backPropagate(int[][] image, int answer) {
        // Update the answer to reflect the offset (numbers 0 -> 0, but letters 1 -> A/a)
        answer -= dataType.offset();

        // Feedforward
        feedForward(image);
        //printBiases(l3Activations);

        // Backward pass
        // Layer 3 delta
        calculateLastNablaBiases(answer, l3Activations, l3ZValues, l3NablaBiases);
        calculateNablaWeights(l3NablaBiases, l2Activations, l23NablaWeights);

        // Layer 2 delta
        calculateNablaBiases(l23Weights, l3NablaBiases, l2ZValues, l2NablaBiases);
        calculateNablaWeights(l2NablaBiases, l1Activations, l12NablaWeights);

        // Update weights to reflect nabla weights
        updateWeights(l12Weights, l12NablaWeights);
        updateWeights(l23Weights, l23NablaWeights);

        // Update biases to reflect nabla biases
        updateBiases(l2Biases, l2NablaBiases);
        updateBiases(l3Biases, l3NablaBiases);
        //printBiases(l2Biases);
        //printBiases(l3Biases);
    }

    private static void updateWeights(double[][] weights,
                                      double[][] nablaWeights) {
        for (int i = 0; i < weights.length; i++) {
            for (int j = 0; j < weights[0].length; j++) {
                weights[i][j] -= (ETA * nablaWeights[i][j]);
            }
        }
    }

    private static void updateBiases(double[] biases, double[] nablaBiases) {
        for (int i = 0; i < biases.length; i++) {
            biases[i] -= (ETA * nablaBiases[i]);
        }
    }

    private static void calculateLastNablaBiases(int x, double[] activations,
                                                 double[] zValues, double[] answers) {
        // Calculate the cost derivative
        // Then, multiply by the sigmoid derivative of the z-value

        assert(activations.length == zValues.length);
        assert(zValues.length == answers.length);

        for (int i = 0; i < activations.length; i++) {
            if (x == i)
                answers[i] = activations[i] - 1.0;
            else
                answers[i] = activations[i] - 0.0;
            answers[i] *= sigmoidPrime(zValues[i]);
        }
    }

    private static void calculateNablaBiases(double[][] weights, double[] delta,
                                             double[] zValues, double[] answers) {
        // The dot product of the weights (next layer) and the delta (next
        // layer) (The cost derivative?)
        // Then, multiply by the sigmoid derivative of the z-value

        assert(weights.length == answers.length);
        assert(weights[0].length == delta.length);
        assert(zValues.length == answers.length);

        for (int i = 0; i < weights.length; i++) {
            double sum = 0.0;
            for (int j = 0; j < delta.length; j++) {
                sum += weights[i][j] * delta[j];
            }
            answers[i] = sum * sigmoidPrime(zValues[i]);
        }
    }

    private static void calculateNablaWeights(double[] delta,
                                              double[] activations, double[][] answers) {
        // The dot product of the activations (previous layer) and the delta
        // (next layer)

        assert(delta.length == answers[0].length);
        assert(activations.length == answers.length);

        for (int i = 0; i < answers.length; i++) {
            for (int j = 0; j < delta.length; j++) {
                answers[i][j] = activations[i] * delta[j];
            }
        }
    }

    private static void testNetwork() throws Exception {
        // Ask for an input to test ('q' to quit)
        Scanner inputScanner = new Scanner(System.in);

        InputStream testLabelStream = new BufferedInputStream(
                new FileInputStream(Constants.DATAFILE_PATH + dataType.testName() + Constants.LABEL_SUFFIX));
        int testLabelSize = DisplayImage.verifyLabelFile(testLabelStream);

        InputStream testImageStream = new BufferedInputStream(
                new FileInputStream(Constants.DATAFILE_PATH + dataType.testName() + Constants.IMAGE_SUFFIX));
        int testImageSize = DisplayImage.verifyImageFile(testImageStream);

        assert(testLabelSize == testImageSize);

        while (true) {
            System.out.print("Enter an index into the test images ... ");
            String input = inputScanner.nextLine();

            if ("q".equals(input)) {
                System.out.println("Thanks for playing! Goodbye!");
                break;
            }

            int index = Integer.parseInt(input);
            if (index >= testLabelSize)
                continue;

            // For MNIST digit 0 -> 0, 1 -> 1, ...
            // For EMNIST letter 1 -> a, 2 -> b, ...
            int label = DisplayImage.getLabel(testLabelStream, index) - dataType.offset();
            char charLabel = (char) (label + (int)dataType.mappingStart());

            System.out.println("The label for image [" + index + "] is: " + charLabel + " (" + label + ")");

            showImage(index);

            // Read the image, returning a 2d array
            int[][] imageArray = DisplayImage.getImage(testImageStream, index);

            feedForward(imageArray);

            //System.out.print("l2 biases: ");
            //printBiases(l2Biases);
            //System.out.print("l2 activations: ");
            //printBiases(l2Activations);
            //System.out.print("l3 biases: ");
            //printBiases(l3Biases);
            //System.out.print("l2 activations: ");
            printBiases(l3Activations);

            int answer = getNetworkOutput();
            char charAnswer = (char) (answer + (int)dataType.mappingStart());
            System.out.println("The network says: " + charAnswer + " (" + answer + ")");
        }
        testLabelStream.close();
        testImageStream.close();
    }

    private static void testNetworkAuto() {
        ArrayList<LabelImagePair> dataList = testData.data();
        //Collections.shuffle(dataList);
        int correctCount = 0;
        for (LabelImagePair pair : dataList) {
            feedForward(pair.image());

            // update the label to reflect the mapping offset 0 -> 0, 1 -> A/a
            int answer = pair.label() - dataType.offset();
            if (answer == getNetworkOutput())
                correctCount++;
        }
        double percentCorrect = ((double) correctCount)/dataList.size() * 100;
        System.out.println(correctCount + " out of " + dataList.size() +
                " correct = " + percentCorrect + "%");
    }

    private static void feedForward(int[][] imageArray) {

        // Image 2d format to a flat array
        int counter = 0;
        for (int i = 0; i < 28; i++)
            for (int j = 0; j < 28; j++)
                l1Activations[counter++] = imageArray[i][j]/255.0;

        // Calculate hidden layer 2 values
        //System.out.println("calculating l2 activations ...");
        calculateLayer(l1Activations, l12Weights, l2Biases, l2ZValues, l2Activations);

        // Calculate output layer 3 values
        //System.out.println("calculating l3 activations ...");
        calculateLayer(l2Activations, l23Weights, l3Biases, l3ZValues, l3Activations);
    }

    private static void calculateLayer(double[] input, double[][] weights,
                                       double[] biases, double[] zValues, double[] output) {
        assert(input.length == weights.length);
        assert(biases.length == weights[0].length);
        assert(biases.length == output.length);

        for (int i = 0; i < output.length; i++) {
            double sum = 0;
            for (int j = 0; j < input.length; j++) {
                sum += input[j] * weights[j][i];
            }
            sum += biases[i];
            zValues[i] = sum;
            output[i] = sigmoid(sum);
        }
    }

    private static double sigmoid(double z) {
        double xx = 1.0/(1.0 + Math.exp(-z));
        //System.out.println("  {sigmoid: " + z + " = " + xx);
        return xx;
    }

    private static double sigmoidPrime(double z) {
        //double eExpMinusZ = Math.exp(-z);
        //return eExpMinusZ/Math.pow(1.0+eExpMinusZ, 2.0);
        return sigmoid(z) * (1-sigmoid(z));
    }

    private static int getNetworkOutput() {
        double maxValue = l3Activations[0];
        int maxIndex = 0;

        for (int i = 1; i < l3Activations.length; i++) {
            if (l3Activations[i] > maxValue) {
                maxValue = l3Activations[i];
                maxIndex = i;
            }
        }

        return maxIndex;
    }
    private static void initialize() {
        System.out.println("Initializing the network : 784, " + L2SIZE + ", " + dataType.outputs() + "...");

        // Layer 2 -> 3 weights
        for (int i = 0; i < l23Weights.length; i++)
            l23Weights[i] = new double[dataType.outputs()];
        // Layer 2 -> 3 nabla weights
        for (int i = 0; i < l23NablaWeights.length; i++)
            l23NablaWeights[i] = new double[dataType.outputs()];

        // Layer 3 biases
        l3Biases = new double[dataType.outputs()];
        // Layer 3 nabla biases
        l3NablaBiases = new double[dataType.outputs()];
        // Layer 3 z-values
        l3ZValues = new double[dataType.outputs()];
        // Layer 3 activations
        l3Activations = new double[dataType.outputs()];

        initializeWeights(l12Weights);
        initializeWeights(l23Weights);
        initializeBiases(l2Biases);
        initializeBiases(l3Biases);

        // Debug
        //printBiases(l3Biases);
    }

    private static void initializeWeights(double[][] array) {
        for (int i = 0; i < array.length; i++)
            for (int j = 0; j < array[i].length; j++)
                array[i][j] = random.nextGaussian();
    }

    private static void initializeBiases(double[] array) {
        for (int i = 0; i < array.length; i++)
            array[i] = random.nextGaussian();
    }

    private static void printBiases(double[] array) {
        System.out.print("[");
        for (int i = 0; i < array.length; i++) {
            if (i != 0)
                System.out.print(", ");
            System.out.print(String.format("%1.4f", array[i]));
        }
        System.out.println("]");
    }

    private static void showImage(int index) throws Exception {
        // Execute "java -cp <this-jar> org.lobo.DisplayImage <test-file-name> <index>"
        String jarPath = new File(NeuralNetwork.class.getProtectionDomain().getCodeSource().getLocation().toURI()).
                getPath();

        ArrayList<String> execArgs = new ArrayList<>();
        execArgs.add("java");
        execArgs.add("-cp");
        execArgs.add(jarPath);
        execArgs.add("org.lobo.DisplayImage");
        execArgs.add(dataType.testName());
        execArgs.add(Integer.toString(index));
        Process process = Runtime.getRuntime().exec(execArgs.toArray(new String[execArgs.size()]));
    }
}
