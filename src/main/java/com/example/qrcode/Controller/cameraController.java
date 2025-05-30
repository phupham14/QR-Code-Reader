package com.example.qrcode.Controller;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.qrcode.QRCodeMultiReader;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Button;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.objdetect.QRCodeDetector;
import org.opencv.videoio.VideoCapture;
import org.opencv.imgproc.Imgproc;
import javafx.embed.swing.SwingFXUtils;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class cameraController {
    @FXML
    private ImageView cameraView;

    @FXML
    private Button cameraButton;

    @FXML
    private TextArea textArea; // TextArea để hiển thị nội dung QR Code dưới dạng JSON

    @FXML
    private ImageView imageView;

    private VideoCapture camera;
    private AtomicBoolean isCameraActive = new AtomicBoolean(false);

    public void initialize() {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        camera = new VideoCapture();
    }

    public void setTextArea(TextArea textArea) {
        this.textArea = textArea;
    }

    private Consumer<String> onQRCodeScanned;

    public void setOnQRCodeScanned(Consumer<String> callback) {
        this.onQRCodeScanned = callback;
    }

    private void processQRCode(String qrContent) {
        System.out.println("Scanned QR Code: " + qrContent);
        if (onQRCodeScanned != null) {
            onQRCodeScanned.accept(qrContent);
        }
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

                            if (qrContent != null && !qrContent.isEmpty()) {
                                // Lưu hình ảnh QR
                                String imagePath = saveImage(frame);

                                if (imagePath != null) {
                                    // Cập nhật ImageView trên giao diện
                                    Platform.runLater(() -> imageController.getInstance().updateImageView(imagePath));

                                    // Dừng camera sau khi lưu ảnh
                                    Platform.runLater(() -> stopCamera());
                                }
                            }

                            // Hiển thị hình ảnh camera lên giao diện
                            Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2RGB);
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
            stopCamera();
        }
    }

    public void stopCamera() {
        isCameraActive.set(false);
        cameraButton.setText("Open Camera");
        camera.release();
    }

    public String saveImage(Mat frame) {
        MatOfPoint2f points = detectQRCode(frame);
        if (points != null) {
            // Cắt vùng QR từ ảnh gốc
            Mat qrCodeRegion = cropQRCode(frame, points);

            // Lấy thư mục lưu ảnh hợp lệ
            String picturesPath = System.getProperty("user.home") + File.separator + "Pictures";
            File dir = new File(picturesPath);
            if (!dir.exists()) {
                dir.mkdirs(); // Tạo thư mục nếu chưa tồn tại
            }

            // Lưu ảnh QR đã cắt vào thư mục Pictures
            String filename = picturesPath + File.separator + "qr_code.png";
            if (Imgcodecs.imwrite(filename, qrCodeRegion)) {
                System.out.println("QR Code image saved to: " + filename);
                qrCodeRegion.release();
                return filename;
            } else {
                System.out.println("Failed to save QR code image.");
            }
        }
        return null;
    }

    // Hàm phát hiện QR Code và trả về tọa độ
    private MatOfPoint2f detectQRCode(Mat frame) {
        QRCodeDetector detector = new QRCodeDetector();
        Mat points = new Mat();
        boolean detected = detector.detect(frame, points);

        if (detected && points.rows() > 0) {
            MatOfPoint2f result = new MatOfPoint2f(points);
            points.release();
            return result;
        }
        return null;
    }

    // Hàm cắt vùng QR Code
    private Mat cropQRCode(Mat frame, MatOfPoint2f points) {
        Rect rect = Imgproc.boundingRect(points);
        return new Mat(frame, rect);
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
                    float xMin = Float.MAX_VALUE, yMin = Float.MAX_VALUE;
                    float xMax = Float.MIN_VALUE, yMax = Float.MIN_VALUE;

                    for (ResultPoint point : points) {
                        float x = point.getX();
                        float y = point.getY();
                        xMin = Math.min(xMin, x);
                        yMin = Math.min(yMin, y);
                        xMax = Math.max(xMax, x);
                        yMax = Math.max(yMax, y);
                    }

                    Point topLeft = new Point(xMin, yMin);
                    Point bottomRight = new Point(xMax, yMax);
                    Imgproc.rectangle(frame, topLeft, bottomRight, new Scalar(0, 255, 0), 2);
                }

                String rawContent = result.getText();
                System.out.println("QR Code Raw Content: " + rawContent);

                // Gọi xử lý hiển thị và lưu QR
                Platform.runLater(() -> {
                    imageController.getInstance().processQRCode(rawContent, "camera");
                });

                return rawContent;
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