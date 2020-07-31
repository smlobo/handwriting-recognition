package org.lobo;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;

public class DisplayImage extends Application {
    private static int label;
    private static char charLabel;
    private static int[][] digitImage = new int[28][28];
    private static byte[] buffer = new byte[4];

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.out.println("Usage: java DisplayImage <t10k|train> <n>");
            System.exit(1);
        }

        // Generate the real filename for the image & label
        String imageFile = Constants.DATAFILE_PATH + args[0] + Constants.IMAGE_SUFFIX;
        String labelFile = Constants.DATAFILE_PATH + args[0] + Constants.LABEL_SUFFIX;

        // Get the index into the data
        int index = Integer.parseInt(args[1]);

        // Get the label
        InputStream inputStream = new BufferedInputStream(new FileInputStream(labelFile));
        int size = verifyLabelFile(inputStream);
        // Sanity check
        if (size <= index) {
            System.out.println("Index [" + index + "] is less than the size [" + size + "]");
            System.exit(3);
        }
        label = getLabel(inputStream, index);

        // For the MNIST digits 0 -> 0, 1 -> 1, ...
        charLabel = (char) (label + (int)'0');
        // For EMNIST letters 1 -> A, 2 -> B, ...
        if (args[0].contains("letters")) {
            charLabel = (char) (label - 1 + (int)'A');
        }

        System.out.println("Index [" + index + "] label is: " + charLabel + " {" + label + "}");
        inputStream.close();

        // Get the image
        inputStream = new BufferedInputStream(new FileInputStream(imageFile));
        size = verifyImageFile(inputStream);
        // Sanity check
        if (size <= index) {
            System.out.println("Index [" + index + "] is less than the size [" + size + "]");
            System.exit(3);
        }
        getImage(inputStream, index, args[0].contains("emnist"));
        inputStream.close();

        // Draw the image
        launch(args);
    }

    private static int readInteger(InputStream inputStream) throws Exception {
        if (inputStream.read(buffer, 0, 4) != 4) {
            System.out.println("Could not read the integer");
            System.exit(2);
        }
        return ByteBuffer.wrap(buffer).getInt();
    }

    private static void skipStream(InputStream inputStream, int count) throws Exception {
        while (count > 0) {
            count -= inputStream.skip(count);
        }
        assert(count == 0);
    }

    public static int verifyLabelFile(InputStream inputStream) throws Exception {
        // Read the magic number
        int magicNumber = readInteger(inputStream);
        assert(magicNumber == 0x801);

        // Read the size number
        int size = readInteger(inputStream);

        // Mark the start of the real data for re-reading
        inputStream.mark(size);

        return size;
    }

    public static int getLabel(InputStream inputStream, int index) throws Exception {

        // Skip previous indices
        //assert(inputStream.skip(index) == index);
        skipStream(inputStream, index);

        // Read the byte
        int byteRead = inputStream.read();
        assert(byteRead >=0 && byteRead <= 9);

        // Reset for the next invocation
        inputStream.reset();

        return byteRead;
    }

    public static int verifyImageFile(InputStream inputStream) throws Exception {

        // Read the magic number
        int magicNumber = readInteger(inputStream);
        assert(magicNumber == 0x803);

        // Read the size number
        int size = readInteger(inputStream);

        // Number of rows & columns
        int nRows = readInteger(inputStream);
        assert(nRows == 28);
        int nColumns = readInteger(inputStream);
        assert(nColumns == 28);

        // Mark the start of the real data for re-reading
        inputStream.mark(size*nRows*nColumns);

        return size;
    }

    // Temporary until EMNIST is used everywhere - until then assume MNIST
    public static int[][] getImage(InputStream inputStream, int index) throws Exception {
        return getImage(inputStream, index, false);
    }

    public static int[][] getImage(InputStream inputStream, int index, boolean columnWise) throws Exception {

        // Skip previous indices
        //assert(inputStream.skip(index*28*28) == index*28*28);
        skipStream(inputStream, index*28*28);

        // Read the byte
        for (int i = 0; i < 28; i++) {
            for (int j = 0; j < 28; j++) {
                int byteRead = inputStream.read();
                assert(byteRead >=0 && byteRead <= 255);
                if (!columnWise)
                    digitImage[i][j] = byteRead;
                else
                    digitImage[j][i] = byteRead;
            }
        }

        // Reset for the next invocation
        inputStream.reset();

        return digitImage;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Group root = new Group();
        Scene scene = new Scene(root,280,280,Color.WHITE);

        // Display the image
        for (int i = 0; i < 28; i++) {
            for (int j = 0; j < 28; j++) {
                Rectangle sq = new Rectangle(j*10,i*10,10,10);
                // Input file format 0 == white, 255 == black
                sq.setFill(Color.rgb(digitImage[i][j], digitImage[i][j], digitImage[i][j]));
                root.getChildren().add(sq);
            }
        }

        primaryStage.setTitle("Digit: " + charLabel + "/" + label);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
