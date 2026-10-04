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

    private Interpreter interpreter;

    private static final int INPUT_SIZE = 48;

    // Emotion labels must match the order used when training the model
    private final String[] emotions = {
            "Angry",
            "Disgust",
            "Fear",
            "Happy",
            "Sad",
            "Surprise",
            "Neutral"
    };

    // Result returned after classification
    public static class ClassificationResult {

        public final String label;
        public final float confidence;

        public ClassificationResult(String label, float confidence) {
            this.label = label;
            this.confidence = confidence;
        }
    }

    // Load TensorFlow Lite model
    public EmotionClassifier(Context context) throws IOException {
        interpreter = new Interpreter(loadModelFile(context));
    }

    private MappedByteBuffer loadModelFile(Context context) throws IOException {

        AssetFileDescriptor fileDescriptor =
                context.getAssets().openFd("emotion_model.tflite");

        FileInputStream inputStream =
                new FileInputStream(fileDescriptor.getFileDescriptor());

        FileChannel fileChannel = inputStream.getChannel();

        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();

        return fileChannel.map(
                FileChannel.MapMode.READ_ONLY,
                startOffset,
                declaredLength
        );
    }

    // Prepare image for the model
    public float[][][][] preprocess(Bitmap bitmap) {

        // Resize image to 48 x 48
        Bitmap resized = Bitmap.createScaledBitmap(
                bitmap,
                INPUT_SIZE,
                INPUT_SIZE,
                true
        );

        // Model expects:
        // [1][48][48][1]
        float[][][][] input =
                new float[1][INPUT_SIZE][INPUT_SIZE][1];

        for (int y = 0; y < INPUT_SIZE; y++) {

            for (int x = 0; x < INPUT_SIZE; x++) {

                int pixel = resized.getPixel(x, y);

                int r = Color.red(pixel);
                int g = Color.green(pixel);
                int b = Color.blue(pixel);

                // Convert RGB to grayscale
                float gray =
                        (0.299f * r) +
                                (0.587f * g) +
                                (0.114f * b);

                // Normalize to 0 - 1
                input[0][y][x][0] = gray / 255.0f;
            }
        }

        return input;
    }

    // Predict emotion
    public ClassificationResult classify(Bitmap bitmap) {

        float[][][][] input = preprocess(bitmap);

        // Seven emotion outputs
        float[][] output = new float[1][7];

        // Run TensorFlow Lite model
        interpreter.run(input, output);

        // Find emotion with highest probability
        int maxIndex = 0;
        float maxVal = output[0][0];

        for (int i = 1; i < output[0].length; i++) {

            if (output[0][i] > maxVal) {

                maxVal = output[0][i];
                maxIndex = i;
            }
        }

        return new ClassificationResult(
                emotions[maxIndex],
                maxVal * 100f
        );
    }

    // ---------------------------------------------------------
    // EMOTION EMOJI
    // ---------------------------------------------------------

    public String getEmoji(String emotion) {

        switch (emotion) {

            case "Angry":
                return "😠";

            case "Disgust":
                return "🤢";

            case "Fear":
                return "😨";

            case "Happy":
                return "😊";

            case "Sad":
                return "😢";

            case "Surprise":
                return "😲";

            case "Neutral":
                return "😐";

            default:
                return "🙂";
        }
    }


    // Release TensorFlow Lite resources
    public void close() {

        if (interpreter != null) {

            interpreter.close();
            interpreter = null;
        }
    }
}

