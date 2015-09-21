package edu.bit.eline.detection;

import static org.bytedeco.javacpp.opencv_core.IPL_DEPTH_8U;
import static org.bytedeco.javacpp.opencv_core.cvCreateImage;
import static org.bytedeco.javacpp.opencv_core.cvCreateMemStorage;
import static org.bytedeco.javacpp.opencv_core.cvGetSize;
import static org.bytedeco.javacpp.opencv_core.cvPoint;
import static org.bytedeco.javacpp.opencv_imgproc.CV_BGR2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.CV_CHAIN_APPROX_SIMPLE;
import static org.bytedeco.javacpp.opencv_imgproc.CV_RETR_CCOMP;
import static org.bytedeco.javacpp.opencv_imgproc.CV_THRESH_BINARY;
import static org.bytedeco.javacpp.opencv_imgproc.cvBoxPoints;
import static org.bytedeco.javacpp.opencv_imgproc.cvCvtColor;
import static org.bytedeco.javacpp.opencv_imgproc.cvFindContours;
import static org.bytedeco.javacpp.opencv_imgproc.cvMinAreaRect2;
import static org.bytedeco.javacpp.opencv_imgproc.cvThreshold;

import java.util.ArrayList;
import java.util.List;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.opencv_core.CvBox2D;
import org.bytedeco.javacpp.opencv_core.CvContour;
import org.bytedeco.javacpp.opencv_core.CvMemStorage;
import org.bytedeco.javacpp.opencv_core.CvPoint2D32f;
import org.bytedeco.javacpp.opencv_core.CvSeq;
import org.bytedeco.javacpp.opencv_core.IplImage;

/**
 * 连通区域分析器。
 * @author ysl
 */
public class BlobAnalyzer {

    private float minRegion = 200;

    public BlobAnalyzer() {
    }

    /**
     * @param minRegion
     *            最小连通区域的面积，默认值为200。
     */
    public BlobAnalyzer(int minRegion) {
        this.minRegion = minRegion;
    }

    protected float max(float[] nums) {
        if (nums.length == 0)
            return -1;
        else if (nums.length == 1)
            return nums[0];
        float max = nums[0];
        for (int i = 1; i < nums.length; ++i) {
            if (nums[i] > max)
                max = nums[i];
        }
        return max;
    }

    protected float min(float[] nums) {
        if (nums.length == 0)
            return -1;
        else if (nums.length == 1)
            return nums[0];
        float min = nums[0];
        for (int i = 1; i < nums.length; ++i) {
            if (nums[i] < min)
                min = nums[i];
        }
        return min;
    }

    /**
     * 连通区域分析。
     * @param img
     *            待分析图像
     * @return 分析结果，二值图像。
     */
    public List<Blob> analyze(IplImage img) {
        // 图像转换
        IplImage grayImage;
        if (img.nChannels() != 1 || img.depth() != IPL_DEPTH_8U) {
            grayImage = cvCreateImage(cvGetSize(img), IPL_DEPTH_8U, 1);
            cvCvtColor(img, grayImage, CV_BGR2GRAY);
        } else {
            grayImage = img;
        }
        cvThreshold(grayImage, grayImage, 10, 255, CV_THRESH_BINARY);

        CvMemStorage mem;
        mem = cvCreateMemStorage(0);
        CvSeq contours = new CvSeq();
        cvFindContours(grayImage, mem, contours,
                Loader.sizeof(CvContour.class), CV_RETR_CCOMP,
                CV_CHAIN_APPROX_SIMPLE, cvPoint(0, 0));

        List<Blob> blobList = new ArrayList<Blob>();
        if(contours.isNull() == true){
            return blobList;
        }
        for (CvSeq ptr = contours; ptr != null; ptr = ptr.h_next()) {
            try{
                CvBox2D rect = cvMinAreaRect2(ptr, mem);
                float[] points = new float[8];
                rect.get(points);
                CvPoint2D32f rect_pts = new CvPoint2D32f(6);
                cvBoxPoints(rect, rect_pts);
                rect_pts.get(points);
    
                float[] Xes = { points[0], points[2], points[4], points[6] };
                float[] Ys = { points[1], points[3], points[5], points[7] };
    
                float maxX = max(Xes);
                float minX = min(Xes);
                float maxY = max(Ys);
                float minY = min(Ys);
                
                // 检查区域面积
                if ((maxX - minX) * (maxY - minY) > minRegion) {
                    // 将区域矩形长宽各扩大10%
                    float deltaX = (maxX - minX) * (float)0.05;
                    float deltaY = (maxY - minY) * (float)0.05;
                    minX = Math.max(0, minX - deltaX);
                    minY = Math.max(0, minY - deltaY);
                    maxX = Math.min(grayImage.width(), maxX + deltaX + 1);
                    maxY = Math.min(grayImage.height(), maxY + deltaY + 1);
                    Blob newBlob = new Blob(minX, minY, maxX - minX, maxY - minY);
                    blobList.add(newBlob);
                }
            } catch(Exception e){
                e.printStackTrace();
                continue;
            }
        }
        mem.release();
        contours.free_blocks();
        return blobList;
    }

}
