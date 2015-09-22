package edu.bit.eline.system;

import static org.bytedeco.javacpp.opencv_highgui.cvLoadImage;

import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import org.bytedeco.javacpp.opencv_core.CvRect;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.Frame;

import edu.bit.eline.detection.Detector;
import edu.bit.eline.detection.DirProcesser;
import edu.bit.eline.detection.ImageConverter;
import edu.bit.eline.detection.Blob;
import edu.bit.eline.detection.BlobAnalyzer;
import edu.bit.eline.detection.Color;
import edu.bit.eline.recognise.feature.ExtractFeature;
import edu.bit.eline.recognise.svm.ImageClassification;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Eline {

    private JFrame frame;

    /**
     * Launch the application.
     */
    private String configpath;

    public Eline(String configpath) {
        this.configpath = configpath;
    }

    public static String classifyOneImg(BufferedImage img) {

        ExtractFeature ef = new ExtractFeature();
        String feature = ef.extractIMGfeature(img);

        ImageClassification ic = new ImageClassification();

        return ic.classifyOneImg("4 " + feature, ".\\config\\models\\model0.0.4.0.model", ".\\config\\models\\scale.params");

    }

    private static final long serialVersionUID = -8054742885149944542L;

    public static void run(String pathname) throws InterruptedException {
        // 获取图片文件列表
        String[] imgList = DirProcesser.getFilenames(pathname, "jpg");
        System.out.println(imgList.length);

        // 初始化模型
        Detector detector = new Detector(0, 9, false);
        BlobAnalyzer analyzer = new BlobAnalyzer(4000);

        // 初始化格式转换器
        ImageConverter converter = new ImageConverter();

        // GUI窗口初始化
        CanvasFrame out = new CanvasFrame("Output");
        CanvasFrame inp = new CanvasFrame("Input");
        inp.setVisible(true);
        out.setVisible(true);
        inp.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        out.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 逐个处理图片
        for (int i = 0; i < imgList.length; ++i) {
            String imgName = pathname + imgList[i];
            System.out.println(i + 1);
            // IplImage为OpenCV中的图像
            BufferedImage bimg = null;
            try {
                bimg = ImageIO.read(new File(imgName));
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            // IplImage img = cvLoadImage(imgName);
            if (bimg != null) {
                Mat imgMat = converter.convert2Mat(bimg);
                // 检测
                IplImage mask = detector.detect(imgMat, 0.1, true);

                // 连通区域分析

                List<Blob> blobList;
                blobList = analyzer.analyze(mask);

                // 绘制连通区域
                Color color = new Color(255, 0, 0);
                int blobnum = 0;
                BufferedImage bufimg;
                bufimg = converter.convert2JavaImg(imgMat);

                // bufimg=converter.convert2JavaImg(mat)

                for (Blob blob : blobList) {

                    // BufferedImage
                    // subimg=converter.convert2JavaImg(blob.cutByBorder(img));
                    CvRect rect = blob.getRect();

                    BufferedImage subimg = bufimg.getSubimage(rect.x(), rect.y(), rect.width(), rect.height());
                    // String label = classifyOneImg(subimg);
                    // if (!label.equals("0.0"))
                    imgMat = blob.drawRect(imgMat, color);
                    // imgMat = blob.drawRect(img, color, 1);

                    // System.out.println(label);
                    /*
                     * try{ File outputfile = new
                     * File("E:/examples/Detected/"+Integer
                     * .toString(i+1)+"-"+Integer.toString(blobnum)+".jpg");
                     * ImageIO.write(subimg, "jpg", outputfile); } catch
                     * (Exception ex) {ex.printStackTrace(); }
                     */
                    blobnum++;
                }

                // 显示图像
                Frame inpFrame = converter.convert2Frame(imgMat);
                Frame outFrame = converter.convert2Frame(mask);
                inp.showImage(inpFrame);
                out.showImage(outFrame);

                // 暂停1秒
                Thread.sleep(500);
            }
        }
    }

    public static void main(String[] args) {

        try {
            run("e:/example/5/");
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 提取特征
        // ExtractFeature ef=new ExtractFeature();
        // ef.extractFoldfeature("E:\\电网项目\\config\\classes",
        // "E:\\电网项目\\config\\features\\0.0.4.0.features.feature");

        // 归一化并保存参数模型(True为保存)
        ImageClassification ic = new ImageClassification();
        // ic.scaledata("E:\\电网项目\\config\\features\\0.0.4.0.features.feature",
        // "E:\\电网项目\\config\\features\\0.0.4.0.features.feature.scale", true,
        // "E:\\电网项目\\config\\models\\scale.params");
        // 生成训练数据和预测数据
        // ic.generateTrainPredict("E:\\电网项目\\config\\features\\0.0.4.0.features.feature.scale",
        // 50, "E:\\电网项目\\config\\features\\0.0.4.0.train.feature",
        // "E:\\电网项目\\config\\features\\0.0.4.0.predict.feature");

        // 参数优化
        // String []classes={"0.0","4.0"};
        // String
        // result=ic.paramoptimize("E:\\电网项目\\config\\features\\0.0.4.0.train.feature",
        // "E:\\电网项目\\config\\models\\model0.0.4.0.model","E:\\电网项目\\config\\features\\0.0.4.0.predict.feature","E:\\电网项目\\config\\temp\\0.0.4.0.result",classes
        // );
        // System.out.println(result);
        // Best C: 2.0 Best G: 0.00390625 Best Acc: 0.9974358677864075
        // Best C: 4.0 Best G: 0.015625 Best Acc: 0.9772099256515503
        // Best C: 1.0 Best G: 0.0078125 Best Acc: 0.9967897534370422
        // Best C: 1.0 Best G: 9.765625E-4 Best Acc: 0.9756097793579102
        // ic.train("-t 2 -c 2.0 -g 0.00390625",
        // "E:\\电网项目\\config\\features\\0.0.4.0.features.feature.scale",
        // "E:\\电网项目\\config\\models\\model0.0.4.0.model");
        // ic.predict("-b 0",
        // "E:\\电网项目\\config\\features\\0.0.4.0.predict.feature",
        // "E:\\电网项目\\config\\models\\model0.0.4.0.model",
        // "E:\\电网项目\\config\\temp\\0.0.4.0.result");

    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        frame = new JFrame();
        frame.getContentPane().setSize(new Dimension(200, 200));
        frame.setBounds(200, 200, 900, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

}
