package edu.bit.eline.detection;

import java.util.List;

import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_video.BackgroundSubtractorMOG2;

/**
 * 目标检测器
 * @author ysl
 */
public class Detector {
    private BackgroundSubtractorMOG2 detector;

    public Detector() {
        detector = new BackgroundSubtractorMOG2();
    }

    /**
     * @param history
     *            作为训练数据的历史图像数目
     * @param varThreshold
     *            阈值方差
     */
    public Detector(int history, float varThreshold) {
        detector = new BackgroundSubtractorMOG2(history, varThreshold);
    }

    /**
     * @param history
     *            作为训练数据的历史图像数目
     * @param varThreshold
     *            阈值方差
     * @param bShadowDetection
     *            是否检测阴影，true为检测阴影，默认为false
     */
    public Detector(int history, float varThreshold, boolean bShadowDetection) {
        detector = new BackgroundSubtractorMOG2(history, varThreshold,
                bShadowDetection);
    }

    /**
     * 设定高斯模型个数
     * @param nmixtures
     *            高斯模型个数
     */
    public void setNMixtures(int nmixtures) {
        detector.setInt("nmixtures", nmixtures);
    }

    /**
     * 设定背景比率
     * @param backgroundRatio
     *            背景比率
     */
    public void setBackgroundRatio(double backgroundRatio) {
        detector.setDouble("backgroundRatio", backgroundRatio);
    }

    /**
     * 训练，和detect没有区别，只是不返回结果
     * @param imageList
     *            训练图像集合
     * @param learningRate
     *            学习率
     */
    public void train(List<Mat> imageList, double learningRate) {
        Mat outImg = new Mat();
        for (Mat img : imageList) {
            detector.apply(img, outImg, learningRate);
        }
    }

    /**
     * 检测
     * @param image
     *            待检测图像
     * @param learningRate
     *            学习率
     * @param bMorphology
     *            是否进行形态学运算
     * @return 检测结果，二值图像
     */
    public IplImage detect(Mat image, double learningRate, boolean bMorphology) {
        Mat outImg = new Mat();
        detector.apply(image, outImg, learningRate);

        if (bMorphology) {
            Mat openKernel = Morphology.kernelFactory(Morphology.MORPH_RECT, 3,
                    3);
            Morphology opener = new Morphology(Morphology.MORPH_OPEN,
                    openKernel);
            Mat closeKernel = Morphology.kernelFactory(Morphology.MORPH_RECT,
                    15, 15);
            Morphology closer = new Morphology(Morphology.MORPH_CLOSE,
                    closeKernel);
            opener.apply(outImg, outImg);
            closer.apply(outImg, outImg);
        }
        return outImg.asIplImage();
    }

}
