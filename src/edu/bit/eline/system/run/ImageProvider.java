package edu.bit.eline.system.run;

import java.awt.image.BufferedImage;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.Calendar;
import java.util.Deque;
import java.util.List;

public class ImageProvider implements Runnable {
    private ImageStorage     storage;
    private Processer        processer;
    private Calendar         lastTime;
    private SimpleDateFormat format;
    private Deque<String>    usedPicQueue;
    private int              maxCapacity = 750;

    public ImageProvider(ImageStorage storage, Processer processer) {
        this.storage = storage;
        this.processer = processer;
        this.format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        this.lastTime = Calendar.getInstance();
        this.lastTime.roll(Calendar.MINUTE, -10);
        this.usedPicQueue = new ArrayDeque<String>();
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            List<String> pathList = null;
            List<String> runningLines = processer.getRunningLine();

            Calendar rightNow = Calendar.getInstance();
            rightNow.roll(Calendar.MINUTE, 5);
            String begTime = format.format(lastTime.getTime());
            String endTime = format.format(rightNow.getTime());
            rightNow.roll(Calendar.MINUTE, -15);
            lastTime = rightNow;
            for (String lineName : runningLines) {
                pathList = HttpInterface.getImageList(lineName, begTime,
                        endTime);
                for (String path : pathList) {
                    BufferedImage bimg = HttpInterface.getImage(path);
                    if (bimg == null || usedPicQueue.contains(path)) {
                        continue;
                    }
                    while (usedPicQueue.size() >= maxCapacity) {
                        usedPicQueue.removeFirst();
                    }
                    usedPicQueue.add(path);
                    storage.put(new Pair<String, BufferedImage>(lineName, bimg));
                }
            }
            try {
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
