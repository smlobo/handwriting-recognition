package org.lobo;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;

class LabelImagePair {
    private int index;
    private int label;
    private int[][] image = new int[28][28];

    public LabelImagePair(int index, int label, int[][] image) {
        this.index = index;
        this.label = label;

        // Copy the 2d array
        for (int i = 0; i < image.length; i++) {
            for (int j = 0; j < image[i].length; j++) {
                this.image[i][j] = image[i][j];
            }
        }
    }

    public int index() {
        return index;
    }

    public int label() {
        return label;
    }

    public int[][] image() {
        return image;
    }
}

public class LabelImageData {
    private ArrayList<LabelImagePair> data;

    public LabelImageData(String prefix) throws Exception {
        // Generate the real filename for the image & label
        String imageFile = Constants.DATAFILE_PATH + prefix + Constants.IMAGE_SUFFIX;
        String labelFile = Constants.DATAFILE_PATH + prefix + Constants.LABEL_SUFFIX;

        InputStream labelStream = new BufferedInputStream(
                new FileInputStream(labelFile));
        int labelSize = DisplayImage.verifyLabelFile(labelStream);

        InputStream imageStream = new BufferedInputStream(
                new FileInputStream(imageFile));
        int imageSize = DisplayImage.verifyImageFile(imageStream);

        assert(labelSize == imageSize);

        data = new ArrayList<>(labelSize);

        // Iterate over all the data
        for (int i = 0; i < labelSize; i++) {
            LabelImagePair entry = new LabelImagePair(i,
                    DisplayImage.getLabel(labelStream, i),
                    DisplayImage.getImage(imageStream, i));
            data.add(entry);
        }

        labelStream.close();
        imageStream.close();
    }

    public static void main(String[] args) throws Exception {
        String prefix = args[0];
        System.out.println("Reading data: " + prefix);
        LabelImageData myData = new LabelImageData(prefix);

        // Verify data
        myData.verify(0, 1);
        myData.verify(100, 200);
    }

    private void verify(int x, int y) {
        System.out.println("Compare indices: " + x + ", " + y);
        LabelImagePair xx = data.get(x);
        LabelImagePair yy = data.get(y);
        if (xx.label() != yy.label())
            System.out.println("  The labels differ");
        else
            System.out.println("  The labels are the same");

        compareImage(xx.image(), yy.image());
    }

    private void compareImage(int[][] x, int[][] y) {
        for (int i = 0; i < x.length; i++) {
            for (int j = 0; j < x[i].length; j++) {
                if (x[i][j] != y [i][j]) {
                    System.out.println("  Images differ");
                    return;
                }
            }
        }
        System.out.println("  Images are the same");
    }

    public ArrayList<LabelImagePair> data() {
        return data;
    }
}
