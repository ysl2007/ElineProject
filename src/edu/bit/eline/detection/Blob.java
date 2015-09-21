package edu.bit.eline.detection;

import static org.bytedeco.javacpp.opencv_core.cvRect;
import static org.bytedeco.javacpp.opencv_core.cvRectToROI;
import static org.bytedeco.javacpp.opencv_core.rectangle;

import org.bytedeco.javacpp.opencv_core.CvRect;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_core.IplROI;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Point2f;
import org.bytedeco.javacpp.opencv_core.Rect;

/**
 * 连通区域表示。
 * @author ysl
 */
public class Blob {

    private Point2f leftTop;
    private float   width;
    private float   height;
    private Point2f center;

    /**
     * @param x
     *            左上角顶点x坐标值
     * @param y
     *            左上角顶点y坐标值
     * @param w
     *            宽
     * @param h
     *            高
     */
    public Blob(float x, float y, float w, float h) {
        leftTop = new Point2f((float)x, (float)y);
        this.width = w;
        this.height = h;
        this.center = new Point2f((float) (x + 0.5 * w), (float) (y + 0.5 * h));
    }

    /**
     * @param leftTop
     *            左上角顶点
     * @param w
     *            宽
     * @param h
     *            高
     */
    public Blob(Point2f leftTop, float w, float h) {
        this.leftTop = leftTop;
        this.width = w;
        this.height = h;
        this.center = new Point2f((float) (leftTop.x() + 0.5 * w),
                (float) (leftTop.y() + 0.5 * h));
    }

    /**
     * 返回连通区域矩形边界
     * @return CvRect表示的矩形
     */
    public CvRect getRect() {
        CvRect rect = cvRect((int) leftTop.x(), (int) leftTop.y(), (int) width,
                (int) height);
        return rect;
    }

    /**
     * 返回左上角顶点
     * @return Point2f表示的顶点
     */
    public Point2f getLftTop() {
        return this.leftTop;
    }

    /**
     * 返回矩形中心
     * @return Point2f表示的中心
     */
    public Point2f getCenter() {
        return center;
    }

    /**
     * 返回右下角顶点
     * @return Point2f表示的顶点
     */
    public Point2f getRightBtn() {
        float x = leftTop.x() + width;
        float y = leftTop.y() + height;
        return new Point2f((float)x, (float)y);
    }

    /**
     * 高
     */
    public float height() {
        return height;
    }

    /**
     * 宽
     */
    public float width() {
        return width;
    }

    /**
     * 根据矩形区域切出子图
     * @param img
     *            输入图像
     */
    public IplImage cutByBorder(IplImage img) {
        CvRect rect = getRect();
        IplROI roi = cvRectToROI(rect, 0);
        IplImage subImage = img.roi(roi);
        return subImage;
    }

    /**
     * 计算与另外一个连通区域r中心的距离
     * @param r
     * @return 两连通区域中心的距离
     */
    public double distance(Blob r) {
        if (r == null)
            return Double.MAX_VALUE;
        double tmp1 = Math.pow(this.center.x() - r.center.x(), 2);
        double tmp2 = Math.pow(this.center.y() - r.center.y(), 2);
        return Math.sqrt(tmp1 + tmp2);
    }

    /**
     * 在输入图像上画出矩形区域并返回新图像
     * @param img
     *            输入图像
     * @param color
     *            颜色
     * @param thick
     *            矩形框边线厚度
     */
    public Mat drawRect(IplImage img, Color color, int thick) {
        return drawRect(new Mat(img), color, thick);
    }

    /**
     * 在输入图像上画出矩形区域并返回新图像
     * @param img
     *            输入图像
     * @param color
     *            颜色
     */
    public Mat drawRect(Mat img, Color color) {
        return drawRect(img, color, 1);
    }

    /**
     * 在输入图像上画出矩形区域并返回新图像
     * @param img
     *            输入图像
     * @param color
     *            颜色
     * @param thick
     *            矩形框边线厚度
     */
    public Mat drawRect(Mat img, Color color, int thick) {
        CvRect rect = getRect();
        Mat mat = new Mat(img);
        rectangle(mat, new Rect(rect), color.getColor(), thick, 8, 0);
        return mat;
    }

    /**
     * 合并两个连通区域，更新自身
     * @param b
     *            待合并区域
     */
    public void merge(Blob b) {
        // LT = leftTop, RB = rightButtom
        float LTX = Math.min(this.leftTop.x(), b.getLftTop().x());
        float LTY = Math.min(this.leftTop.y(), b.getLftTop().y());
        float RBX = Math.max(this.leftTop.x() + this.width, b.getRightBtn().x()
                + b.width());
        float RBY = Math.max(this.leftTop.y() + this.height, b.getRightBtn()
                .y() + b.height());

        Point2f newLT = new Point2f((float)LTX, (float)LTY);
        Point2f center = new Point2f((float) (LTX + 0.5 * RBX),
                (float) (LTY + 0.5 * RBY));
        float width = RBX - LTX;
        float height = RBY - LTY;
        this.leftTop = newLT;
        this.center = center;
        this.width = width;
        this.height = height;
    }

}
