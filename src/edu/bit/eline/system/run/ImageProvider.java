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

    // 生产者线程。storage图片存储器，processer消费者
    public ImageProvider(ImageStorage storage, Processer processer) {
        this.storage = storage;
        this.processer = processer;
        this.format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        this.lastTime = Calendar.getInstance();
        this.lastTime.add(Calendar.MINUTE, -20);
        // 每隔若干秒钟获取一次图片，用队列存储处理过的图片
        this.usedPicQueue = new ArrayDeque<String>();
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            List<String> pathList = null;
            // 获取正在运行的线路名
            List<String> runningLines = processer.getRunningLine();

            // 每次获取当前时间点前20分钟至后5分钟的图片，每10秒钟获取一次
            rightNow = Calendar.getInstance();
            rightNow.add(Calendar.MINUTE, 5);
            String begTime = format.format(lastTime.getTime());
            String endTime = format.format(rightNow.getTime());
            lastTime = generateLastTime(rightNow);
            for (String lineName : runningLines) {
                // 对每一条线路获取起止时间内的图片路径列表
                pathList = HttpInterface.getImageList(lineName, begTime, endTime);
                System.out.println("img list length: " + pathList.size());
                // 对路径列表中的每一个图片路径，获取图片
                for (String path : pathList) {
                    BufferedImage bimg = HttpInterface.getImage(path);
                    // 获取失败或获取到已处理的图片，跳过
                    if (bimg == null || usedPicQueue.contains(path)) {
                        continue;
                    }
                    // 否则，从路径中解析文件名，如从e:\abc\def.jpg中解析def.jpg
                    String imgFilename = null;
                    try {
                        imgFilename = URLDecoder.decode(path, "UTF-8");
                        int index = imgFilename.lastIndexOf("/") + 1;
                        imgFilename = imgFilename.substring(index, imgFilename.length());
                    } catch (Exception e) {
                        e.printStackTrace();
                        continue;
                    }
                    // 维护处理历史队列长度
                    while (usedPicQueue.size() >= maxCapacity) {
                        usedPicQueue.removeFirst();
                    }
                    usedPicQueue.add(path);
                    // Pair<String, Pair<String, Image>>分别存放：(线路名，(文件名，图片))
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

    // 这个方法为对方接口需要。对方获取图片列表的接口存在bug，如果起止时间跨越0点，则会返回该摄像头所有
    // 历史图片列表，因此要判断事件是否跨越0点。
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
