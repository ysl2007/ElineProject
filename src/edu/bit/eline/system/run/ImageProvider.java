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
    private SimpleDateFormat format;
    private Deque<String>    usedPicQueue;
    private int              maxCapacity = 750;

    public ImageProvider(ImageStorage storage, Processer processer) {
        this.storage = storage;
        this.processer = processer;
        this.format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        this.lastTime = Calendar.getInstance();
        this.lastTime.add(Calendar.MINUTE, -10);
        this.usedPicQueue = new ArrayDeque<String>();
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            List<String> pathList = null;
            List<String> runningLines = processer.getRunningLine();

            Calendar rightNow = Calendar.getInstance();
            rightNow.add(Calendar.MINUTE, 5);
            String begTime = format.format(lastTime.getTime());
            String endTime = format.format(rightNow.getTime());
            rightNow.add(Calendar.MINUTE, -20);
            lastTime = rightNow;
            for (String lineName : runningLines) {
//            	System.out.println(begTime + " " + endTime);
                pathList = HttpInterface.getImageList(lineName, begTime,
                        endTime);
                System.out.println(pathList.size());
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
                    Pair<String, BufferedImage> imgPair = new Pair<String, BufferedImage>(
                            imgFilename, bimg);
                    storage.put(new Pair<String, Pair<String, BufferedImage>>(lineName, imgPair));
                }
            }
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("while loop in provider...");
        }
    }

    public void test() {
        String line = "安都17(动态风险)";
        String st = "2015-12-22 14:20:00";
        String et = "2015-12-22 15:55:00";
        List<String> pathList = HttpInterface.getImageList(line, st, et);
        // System.out.println(pathList);
        System.out.println(pathList.size());
        for (String path : pathList) {
            System.out.println(path);
//            BufferedImage bimg = HttpInterface.getImage(path);
//            System.out.println(bimg);
        }
    }

    public static void main(String[] args) {
        ImageStorage storage = new ImageStorage();
        Processer processer = new Processer(storage);
        ImageProvider p = new ImageProvider(storage, processer);
        p.test();
    }
}
