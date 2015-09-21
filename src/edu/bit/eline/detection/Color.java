package edu.bit.eline.detection;

import org.bytedeco.javacpp.opencv_core.Scalar;

/**
 * 表示颜色的包装类，用于替代JavaCV中的Scalar类来表示颜色。
 * @author ysl
 */
public class Color {
    private Scalar color;

    /**
     * @param r
     *            红色，0-255。
     * @param g
     *            绿色，0-255
     * @param b
     *            蓝色，0-255
     */
    public Color(int r, int g, int b) {
        this.color = new Scalar(b, g, r, 0);
    }

    /**
     * 返回OpenCV中表示颜色的Scalar对象
     * @return Scalar对象
     */
    public Scalar getColor() {
        return this.color;
    }

}
