package com.example.nutrimind;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Color;
import org.tensorflow.lite.Interpreter;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class EmotionClassifier {

    private Interpreter interpreter; // TensorFlow Lite model
    private static final int INPUT_SIZE = 48; // Model input size

    private final String[] emotions = {
            "Angry", "Disgust", "Fear", "Happy", "Sad", "Surprise", "Neutral"}; // Emotion labels

    // Prediction result
    public static class ClassificationResult {
        public final String label; // Emotion name
        public final float confidence; // Confidence score

        public ClassificationResult(String label, float confidence) {
            this.label = label;
            this.confidence = confidence;
        }
    }

    // Load model
    public EmotionClassifier(Context context) throws IOException {
        interpreter = new Interpreter(loadModelFile(context));
    }

    private MappedByteBuffer loadModelFile(Context context) throws IOException {  // Read model file
        // Open model file
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd("emotion_model.tflite");
        // Create input stream
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        // Get file channel
        FileChannel fileChannel = inputStream.getChannel();
        // Model start position
        long startOffset = fileDescriptor.getStartOffset();
        // Model size
        long declaredLength = fileDescriptor.getDeclaredLength();
        // Load model
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    // Prepare image
    public float[][][][] preprocess(Bitmap bitmap) {

        // Resize image
        Bitmap resized = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true);

        // Create input array
        float[][][][] input = new float[1][INPUT_SIZE][INPUT_SIZE][1];

        // Read pixels
        for (int y = 0; y < INPUT_SIZE; y++) {
            for (int x = 0; x < INPUT_SIZE; x++) { // Get pixel
                int pixel = resized.getPixel(x, y);

                // Get RGB values
                int r = Color.red(pixel);
                int g = Color.green(pixel);
                int b = Color.blue(pixel);
                float gray = (0.299f * r) + (0.587f * g) + (0.114f * b); // Convert to grayscale

                input[0][y][x][0] = gray / 255.0f; // Normalize value
            }
        }
        return input;
    }

    // Predict emotion
    public ClassificationResult classify(Bitmap bitmap) {
        float[][][][] input = preprocess(bitmap); // Prepare input
        float[][] output = new float[1][7]; // Create output array

        // Run model
        interpreter.run(input, output);

        // First prediction
        int maxIndex = 0;
        float maxVal = output[0][0];

        // Find highest score
        for (int i = 1; i < output[0].length; i++) {
            if (output[0][i] > maxVal) {
                maxVal = output[0][i];
                maxIndex = i;
            }
        }

        // Return result
        return new ClassificationResult(emotions[maxIndex], maxVal * 100f);
    }

    // Release model
    public void close() {
        if (interpreter != null) {
            interpreter.close();
            interpreter = null;
        }
    }
}