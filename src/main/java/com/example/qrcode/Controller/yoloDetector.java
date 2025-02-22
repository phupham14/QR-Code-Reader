package com.example.qrcode.Controller;

import org.opencv.core.Rect;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class yoloDetector {
    private Net net;

    public yoloDetector(String modelPath) {
        net = Dnn.readNetFromONNX(modelPath);
    }

    public Mat detectQR(Mat frame) {
        Mat blob = Dnn.blobFromImage(frame, 1 / 255.0, new Size(640, 640), new Scalar(0, 0, 0), true, false);
        net.setInput(blob);
        List<Mat> outputs = new ArrayList<>();
        net.forward(outputs, net.getUnconnectedOutLayersNames());

        for (Mat output : outputs) {
            for (int i = 0; i < output.rows(); i++) {
                float[] data = new float[output.cols()];
                output.get(i, 0, data);
                float confidence = data[4]; // Xác suất
                if (confidence > 0.5) {
                    int x = (int) (data[0] * frame.cols());
                    int y = (int) (data[1] * frame.rows());
                    int width = (int) (data[2] * frame.cols());
                    int height = (int) (data[3] * frame.rows());

                    Imgproc.rectangle(frame, new org.opencv.core.Point(x, y), new org.opencv.core.Point(x + width, y + height), new Scalar(0, 255, 0), 2);
                }
            }
        }
        return frame;
    }

}
