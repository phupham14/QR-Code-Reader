package com.example.qrcode.Utils;

import javax.sound.sampled.*;
import java.io.*;
import java.util.*;

public class AudioMerger {

    public void playMoneyAmount(String amountText) throws Exception {
        String basePath = "D:/Audio/"; // nơi chứa các file .wav
        String[] words = amountText.trim().toLowerCase().split("\\s+");

        List<AudioInputStream> audioList = new ArrayList<>();
        AudioFormat audioFormat = null;

        for (String word : words) {
            File file = new File(basePath + word + ".wav");
            if (!file.exists()) continue; // bỏ qua nếu không có file tương ứng

            AudioInputStream ais = null;
            try {
                ais = AudioSystem.getAudioInputStream(file);
                if (ais == null) continue; // bỏ qua nếu không thể đọc file

                // Kiểm tra nếu audioFormat chưa được gán, thì gán nó từ ais
                if (audioFormat == null) {
                    audioFormat = ais.getFormat();
                }

                audioList.add(ais);
            } catch (UnsupportedAudioFileException | IOException e) {
                System.err.println("Không thể mở file: " + file.getName());
                e.printStackTrace();
            }
        }

        if (audioList.isEmpty()) {
            throw new Exception("Không có file âm thanh hợp lệ để phát.");
        }

        // Tạo SequenceInputStream từ danh sách các AudioInputStream
        SequenceInputStream sequenceInputStream = new SequenceInputStream(
                Collections.enumeration(audioList));

        // Kiểm tra lại audioFormat trước khi tạo AudioInputStream mới
        if (audioFormat == null) {
            throw new Exception("Không thể xác định định dạng âm thanh.");
        }

        AudioInputStream appendedAudio = new AudioInputStream(
                sequenceInputStream, audioFormat,
                audioList.stream().mapToLong(AudioInputStream::getFrameLength).sum());

        // Phát âm thanh
        Clip clip = AudioSystem.getClip();
        clip.open(appendedAudio);
        clip.start();

        // Chờ cho đến khi âm thanh chạy xong
        Thread.sleep((long)(clip.getMicrosecondLength() / 1000 / 1.5)); // Giả lập tăng tốc 1.5x

    }

}

