package org.lobo;

public class LabelImagePair {
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
