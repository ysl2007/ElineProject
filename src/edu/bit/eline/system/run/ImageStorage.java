package edu.bit.eline.system.run;

import java.awt.image.BufferedImage;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ImageStorage {
    private ConcurrentLinkedQueue<Pair<String, BufferedImage>> imgQueue;

    public ImageStorage() {
        imgQueue = new ConcurrentLinkedQueue<Pair<String, BufferedImage>>();
    }

    public void put(Pair<String, BufferedImage> pair) {
        imgQueue.add(pair);
        synchronized (this) {
            notifyAll();
        }
    }

    public synchronized Pair<String, BufferedImage> get() {
        Pair<String, BufferedImage> p = null;
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
