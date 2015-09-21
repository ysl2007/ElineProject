package edu.bit.eline.detection;

import java.awt.image.BufferedImage;

import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;

/**
 * 转换多种表示图片的内置类型。
 * @author ysl
 */
public class ImageConverter {

    private Java2DFrameConverter    frame2bufferedimg;
    private OpenCVFrameConverter<?> frame2Mat;
    private OpenCVFrameConverter<?> frame2Ipl;

    public ImageConverter() {
        frame2bufferedimg = new Java2DFrameConverter();
        frame2Mat = new OpenCVFrameConverter.ToMat();
        frame2Ipl = new OpenCVFrameConverter.ToIplImage();
    }

    /**
     * 转换为JavaCV的Frame
     */
    public Frame convert2Frame(IplImage img) {
        Frame frame = frame2Ipl.convert(img);
        return frame;
    }

    /**
     * 转换为JavaCV的Frame
     */
    public Frame convert2Frame(Mat mat) {
        Frame frame = frame2Mat.convert(mat);
        return frame;
    }

    /**
     * 转换为JavaCV的Frame
     */
    public Frame convert2Frame(BufferedImage img) {
        Frame frame = frame2bufferedimg.convert(img);
        return frame;
    }

    /**
     * 转换为BufferedImage
     */
    public BufferedImage convert2JavaImg(Frame frame) {
        BufferedImage img = frame2bufferedimg.convert(frame);
        return img;
    }

    /**
     * 转换为BufferedImage
     */
    public BufferedImage convert2JavaImg(IplImage img) {
        Frame frame = convert2Frame(img);
        BufferedImage bimg = convert2JavaImg(frame);
        return bimg;
    }

    /**
     * 转换为BufferedImage
     */
    public BufferedImage convert2JavaImg(Mat mat) {
        Frame frame = convert2Frame(mat);
        BufferedImage bimg = convert2JavaImg(frame);
        return bimg;
    }

    /**
     * 转换为IplImage
     */
    public IplImage convert2IplImg(BufferedImage img) {
        Frame frame = convert2Frame(img);
        IplImage Iimg = frame2Ipl.convertToIplImage(frame);
        return Iimg;
    }

    /**
     * 转换为IplImage
     */
    public IplImage convert2IplImg(Frame frame) {
        IplImage Iimg = frame2Ipl.convertToIplImage(frame);
        return Iimg;
    }

    /**
     * 转换为Mat
     */
    public Mat convert2Mat(BufferedImage img) {
        Frame frame = convert2Frame(img);
        Mat mat = frame2Mat.convertToMat(frame);
        return mat;
    }

    /**
     * 转换为Mat
     */
    public Mat convert2Mat(Frame frame) {
        Mat mat = frame2Mat.convertToMat(frame);
        return mat;
    }

}
