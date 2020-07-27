package org.lobo;

public class Constants {
    public static final String DATAFILE_PATH = "src/main/resources/";
    public static final String IMAGE_SUFFIX = "-images-idx3-ubyte";
    public static final String LABEL_SUFFIX = "-labels-idx1-ubyte";
    public static final String TRAIN_IMAGES = DATAFILE_PATH + "train" + IMAGE_SUFFIX;
    public static final String TRAIN_LABELS = DATAFILE_PATH + "train" + LABEL_SUFFIX;
    public static final String TEST_IMAGES = DATAFILE_PATH + "t10k" + IMAGE_SUFFIX;
    public static final String TEST_LABELS = DATAFILE_PATH + "t10k" + LABEL_SUFFIX;
}
