package edu.bit.eline.system.run;

import java.awt.image.BufferedImage;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ImageStorage {
    private ConcurrentLinkedQueue<Pair<String, Pair<String, BufferedImage>>> imgQueue;

    // 使用队列存储未处理图片
    public ImageStorage() {
        imgQueue = new ConcurrentLinkedQueue<Pair<String, Pair<String, BufferedImage>>>();
    }

    public synchronized void put(Pair<String, Pair<String, BufferedImage>> pair) {
        imgQueue.add(pair);
        notifyAll();
    }

    public synchronized Pair<String, Pair<String, BufferedImage>> get() {
        Pair<String, Pair<String, BufferedImage>> p = null;
        p = imgQueue.poll();
        while (p == null) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            p = imgQueue.poll();
        }
        return p;
    }
}
