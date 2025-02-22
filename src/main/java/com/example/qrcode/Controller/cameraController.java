package com.example.qrcode.Controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.qrcode.QRCodeMultiReader;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Button;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;
import org.opencv.imgproc.Imgproc;
import javafx.embed.swing.SwingFXUtils;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import javax.imageio.ImageIO;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.example.qrcode.Controller.imageController.parseEMVQRCode;

public class cameraController {
    @FXML
    private ImageView cameraView;

    @FXML
    private Button cameraButton;

    @FXML
    private TextArea textArea; // TextArea để hiển thị nội dung QR Code dưới dạng JSON

    private VideoCapture camera;
    private AtomicBoolean isCameraActive = new AtomicBoolean(false);

    public void initialize() {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        camera = new VideoCapture();
    }

    public void setTextArea(TextArea textArea) {
        this.textArea = textArea;
    }

    @FXML
    public void startCamera() {
        if (!isCameraActive.get()) {
            camera.open(0); // Mở webcam

            if (camera.isOpened()) {
                isCameraActive.set(true);
                cameraButton.setText("Stop Camera");

                new Thread(() -> {
                    Mat frame = new Mat();
                    while (isCameraActive.get()) {
                        if (camera.read(frame)) {
                            if (frame.empty()) {
                                System.out.println("Frame rỗng!");
                                continue;
                            }

                            // Giải mã QR Code
                            String qrContent = decodeQRCodeAndDisplay(frame);

                            // Hiển thị hình ảnh lên giao diện
                            Image image = matToImage(frame);
                            if (image != null) {
                                Platform.runLater(() -> cameraView.setImage(image));
                            }
                        }
                    }
                    frame.release();
                }).start();
            } else {
                System.out.println("Can't open camera!");
            }
        } else {
            isCameraActive.set(false);
            cameraButton.setText("Open Camera");
            camera.release();
        }
    }

    private String decodeQRCodeAndDisplay(Mat frame) {
        try {
            MatOfByte matOfByte = new MatOfByte();
            Imgcodecs.imencode(".jpg", frame, matOfByte);
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(matOfByte.toArray()));

            LuminanceSource source = new BufferedImageLuminanceSource(image);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

            Result result = new QRCodeMultiReader().decode(bitmap);

            if (result != null) {
                ResultPoint[] points = result.getResultPoints();

                if (points.length >= 4) {
                    // Tìm điểm nhỏ nhất và lớn nhất để xác định khung QR Code
                    float xMin = Float.MAX_VALUE, yMin = Float.MAX_VALUE;
                    float xMax = Float.MIN_VALUE, yMax = Float.MIN_VALUE;

                    for (ResultPoint point : points) {
                        float x = point.getX();
                        float y = point.getY();

                        if (x < xMin) xMin = x;
                        if (y < yMin) yMin = y;
                        if (x > xMax) xMax = x;
                        if (y > yMax) yMax = y;
                    }

                    // Vẽ bounding box màu xanh lá cây
                    Point topLeft = new Point(xMin, yMin);
                    Point bottomRight = new Point(xMax, yMax);
                    Imgproc.rectangle(frame, topLeft, bottomRight, new Scalar(0, 255, 0), 2);
                }

                // Lấy nội dung QR Code
                String rawContent = result.getText();
                System.out.println("QR Code Raw Content: " + rawContent);

                // Bóc tách dữ liệu và hiển thị JSON trong TextArea
                String jsonContent = parseEMVQRCode(rawContent);
                Platform.runLater(() -> textArea.setText(jsonContent)); // Cập nhật TextArea trên giao diện

                return jsonContent;
            }
        } catch (Exception e) {
            //System.err.println("Lỗi khi đọc QR Code: " + e.getMessage());
        }
        return null;
    }

    private Image matToImage(Mat frame) {
        try {
            Mat convertedFrame = new Mat();
            Imgproc.cvtColor(frame, convertedFrame, Imgproc.COLOR_BGR2RGB);

            BufferedImage bufferedImage = new BufferedImage(
                    convertedFrame.width(),
                    convertedFrame.height(),
                    BufferedImage.TYPE_3BYTE_BGR
            );

            byte[] data = ((DataBufferByte) bufferedImage.getRaster().getDataBuffer()).getData();
            convertedFrame.get(0, 0, data);

            return SwingFXUtils.toFXImage(bufferedImage, null);
        } catch (Exception e) {
            System.err.println("Lỗi khi chuyển đổi ảnh: " + e.getMessage());
            return null;
        }
    }
}