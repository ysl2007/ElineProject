package edu.bit.eline.demo;

import java.awt.Graphics;
import java.awt.Image;
import javax.swing.JPanel;

public class ImgPanelTest extends JPanel {
    private static final long serialVersionUID = 1L;
    private Image             image;
    private int               imgWidth;
    private int               imgHeight;

    public Image getImg() {
        return image;
    }

    public int getImgWidth() {
        return imgWidth;
    }

    public void setImgWidth(int imgWidth) {
        this.imgWidth = imgWidth;
    }

    public int getImgHeight() {
        return imgHeight;
    }

    public void setImgHeight(int imgHeight) {
        this.imgHeight = imgHeight;
    }

    public ImgPanelTest() {
    }

    public void setImage(Image img) {
        if (null != img) {
            image = img;
            double scale = Math.min((double)this.getWidth() / image.getWidth(this), (double)this.getHeight() / image.getHeight(this));
            image = image.getScaledInstance((int)(image.getWidth(this) * scale), (int)(image.getHeight(this) * scale), Image.SCALE_DEFAULT);
            setImgWidth(image.getWidth(this));
            setImgHeight(image.getHeight(this));
        }
    }

    @Override
    public void paintComponent(Graphics g1) {
        super.paintComponent(g1);
        int x = 0;
        int y = 0;
        Graphics g = (Graphics) g1;
        if (null == image) {
            return;
        }

        g.drawImage(image, x, y, this.getWidth(), this.getHeight(), this);
        g = null;
    }
    
    public static void main(String[] args) {
        
    }
}
