package edu.bit.eline.system.run;

import java.awt.image.BufferedImage;

public class Producer implements Runnable {
    private ImageStorage storage;

    public Producer(ImageStorage storage) {
        this.storage = storage;
    }

    @Override
    public void run() {
        // TODO: GET IMAGE FROM DB.
        while (!Thread.interrupted()) {
            BufferedImage bimg = null;
            String lineName = null;
            storage.put(new Pair<String, BufferedImage>(lineName, bimg));
            try {
                Thread.sleep(30000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
