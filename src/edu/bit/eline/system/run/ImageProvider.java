package edu.bit.eline.system.run;

import java.awt.image.BufferedImage;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.Calendar;
import java.util.Deque;
import java.util.List;

public class ImageProvider implements Runnable {
    private ImageStorage     storage;
    private Processer        processer;
    private Calendar         lastTime;
    private Calendar         rightNow;
    private SimpleDateFormat format;
    private Deque<String>    usedPicQueue;
    private int              maxCapacity = 750;

    public ImageProvider(ImageStorage storage, Processer processer) {
        this.storage = storage;
        this.processer = processer;
        this.format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        this.lastTime = Calendar.getInstance();
        this.lastTime.add(Calendar.MINUTE, -20);
        this.usedPicQueue = new ArrayDeque<String>();
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            List<String> pathList = null;
            List<String> runningLines = processer.getRunningLine();

            rightNow = Calendar.getInstance();
            rightNow.add(Calendar.MINUTE, 5);
            String begTime = format.format(lastTime.getTime());
            String endTime = format.format(rightNow.getTime());
            lastTime = generateLastTime(rightNow);
            for (String lineName : runningLines) {
                pathList = HttpInterface.getImageList(lineName, begTime, endTime);
                System.out.println("img list length: " + pathList.size());
                for (String path : pathList) {
                    BufferedImage bimg = HttpInterface.getImage(path);
                    if (bimg == null || usedPicQueue.contains(path)) {
                        continue;
                    }
                    String imgFilename = null;
                    try {
                        imgFilename = URLDecoder.decode(path, "UTF-8");
                        int index = imgFilename.lastIndexOf("/") + 1;
                        imgFilename = imgFilename.substring(index, imgFilename.length());
                    } catch (Exception e) {
                        e.printStackTrace();
                        continue;
                    }
                    while (usedPicQueue.size() >= maxCapacity) {
                        usedPicQueue.removeFirst();
                    }
                    usedPicQueue.add(path);
                    Pair<String, BufferedImage> imgPair = new Pair<String, BufferedImage>(imgFilename, bimg);
                    storage.put(new Pair<String, Pair<String, BufferedImage>>(lineName, imgPair));
                }
            }
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("while loop in provider...");
        }
    }

    private Calendar generateLastTime(Calendar rightNow) {
        Calendar zero = Calendar.getInstance();
        zero.set(Calendar.HOUR, -12);
        zero.set(Calendar.MINUTE, 0);
        zero.set(Calendar.SECOND, 0);
        zero.set(Calendar.MILLISECOND, 0);
        rightNow.add(Calendar.MINUTE, -20);
        if (rightNow.compareTo(zero) < 0) {
            rightNow = (Calendar) zero.clone();
        }
        return rightNow;
    }
}
