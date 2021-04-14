package com.annora.photo.tensorflow;

import android.app.Activity;
import java.io.IOException;
import org.tensorflow.lite.support.common.TensorOperator;
import org.tensorflow.lite.support.common.ops.NormalizeOp;

/** This TensorFlow Lite classifier works with the quantized EfficientNet model.
 * note：兼容未适配*/
public class ClassifierQuantizedEfficientNet extends Classifier {

    /**
     * The quantized model does not require normalization, thus set mean as 0.0f, and std as 1.0f to
     * bypass the normalization.
     */
    private static final float IMAGE_MEAN = 0.0f;

    private static final float IMAGE_STD = 1.0f;

    /** Quantized MobileNet requires additional dequantization to the output probability. */
    private static final float PROBABILITY_MEAN = 0.0f;

    private static final float PROBABILITY_STD = 255.0f;

    /**
     * Initializes a {@code ClassifierQuantizedMobileNet}.
     *
     * @param activity
     */
    public ClassifierQuantizedEfficientNet(Activity activity)
            throws IOException {
        super(activity);
    }

    @Override
    protected String getModelPath() {
        // you can download this file from
        // see build.gradle for where to obtain this file. It should be auto
        // downloaded into assets.
        return "efficientnet-lite0-int8.tflite";
    }

    @Override
    protected String getLabelPath() {
        return "labels_without_background.txt";
    }

    @Override
    public int getImageSizeX() {
        return 0;
    }

    @Override
    public int getImageSizeY() {
        return 0;
    }

    @Override
    protected int getNumBytesPerChannel() {
        return 0;
    }

    @Override
    protected void addPixelValue(int pixelValue) {

    }

    @Override
    protected float getProbability(int labelIndex) {
        return 0;
    }

    @Override
    protected void setProbability(int labelIndex, Number value) {

    }

    @Override
    protected float getNormalizedProbability(int labelIndex) {
        return 0;
    }

    @Override
    protected void runInference() {

    }

//    @Override
//    protected TensorOperator getPreprocessNormalizeOp() {
//        return new NormalizeOp(IMAGE_MEAN, IMAGE_STD);
//    }
//
//    @Override
//    protected TensorOperator getPostprocessNormalizeOp() {
//        return new NormalizeOp(PROBABILITY_MEAN, PROBABILITY_STD);
//    }
}
