package edu.bit.eline.detection;

import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Size;
import org.bytedeco.javacpp.opencv_imgproc;

/**
 * 形态学运算器
 * @author ysl
 */
public class Morphology {
    private Mat       kernel;
    private int       op;
    /**
     * 开运算
     */
    public static int MORPH_OPEN     = opencv_imgproc.MORPH_OPEN;
    /**
     * 闭运算
     */
    public static int MORPH_CLOSE    = opencv_imgproc.MORPH_CLOSE;
    /**
     * 梯度运算
     */
    public static int MORPH_GRADIENT = opencv_imgproc.MORPH_GRADIENT;
    /**
     * 顶帽运算
     */
    public static int MORPH_TOPHAT   = opencv_imgproc.MORPH_TOPHAT;
    /**
     * 黑帽运算
     */
    public static int MORPH_BLACKHAT = opencv_imgproc.MORPH_BLACKHAT;

    /**
     * 矩形核
     */
    public static int MORPH_RECT     = opencv_imgproc.MORPH_RECT;
    /**
     * X型核
     */
    public static int MORPH_CROSS    = opencv_imgproc.MORPH_CROSS;
    /**
     * 椭圆核
     */
    public static int MORPH_ELLIPSE  = opencv_imgproc.MORPH_ELLIPSE;

    /**
     * 创建运算核的工厂方法
     * @param shape
     *            核形状，为MORPH_RECT, MORPH_CROSS, MORPH_ELLIPSE之一
     * @param row
     *            核大小，行数
     * @param col
     *            核大小，列数
     * @return 运算核
     */
    public static Mat kernelFactory(int shape, int row, int col) {
        Mat kernel = opencv_imgproc.getStructuringElement(shape, new Size(row,
                col));
        return kernel;
    }

    /**
     * @param op
     *            运算类型，值为MORPH_OPEN, MORPH_CLOSE, MORPH_GRADIENT, MORPH_TOPHAT,
     *            MORPH_BLACKHAT之一。
     * @param kernel
     *            运算核
     */
    public Morphology(int op, Mat kernel) {
        this.kernel = kernel;
        this.op = op;
    }

    /**
     * 执行运算
     * @param src
     *            待处理图像
     * @param tgt
     *            处理结果
     */
    public void apply(Mat src, Mat tgt) {
        opencv_imgproc.morphologyEx(src, tgt, op, kernel);
    }

    /**
     * 重新设定运算类型
     * @param op
     *            运算类型
     */
    public void setOperation(int op) {
        this.op = op;
    }

    /**
     * 重新设定运算核
     * @param kernel
     *            运算核
     */
    public void setKernel(Mat kernel) {
        this.kernel = kernel;
    }
}
