package others;

import java.util.List;

import javax.imageio.ImageIO;

import org.bytedeco.javacpp.opencv_core.CvRect;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Rect;

import edu.bit.eline.detection.Detector;
import edu.bit.eline.detection.DirProcesser;
import edu.bit.eline.detection.ImageConverter;
import edu.bit.eline.detection.Blob;
import edu.bit.eline.detection.BlobAnalyzer;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Negative1 {

	public void run(String path, String dest, int totalLimit) {
		if (!path.endsWith("/") && !path.endsWith("\\"))
			path += '/';
		if (!dest.endsWith("/") && !dest.endsWith("\\"))
			dest += '/';
		String[] subDirs = DirProcesser.getSubDirs(path);

		int subimgCount = 0;
		for (String subDir : subDirs) {
			String dir = path + subDir + '/';
			String[] imgList = DirProcesser.getFilenames(dir, "jpg");
			Detector detector = new Detector(0, 9, false);
			BlobAnalyzer analyzer = new BlobAnalyzer(100);
			ImageConverter converter = new ImageConverter();

			int srcImgIndex = 0;
			for (int i = 0; i < imgList.length; ++i) {
				String imgName = dir + imgList[i];
				BufferedImage img;
				CvRect taggedRect;
				try {
					img = ImageIO.read(new File(imgName));
					taggedRect = getTag(imgName);
				} catch (IOException e1) {
					e1.printStackTrace();
					continue;
				}

				srcImgIndex += 1;
				Mat imgMat = converter.convert2Mat(img);
				if (imgMat == null)
					continue;
				IplImage mask = detector.detect(imgMat, 0.1, true);
				List<Blob> blobList;
				blobList = analyzer.analyze(mask);

				if (srcImgIndex < 10)
					continue;
				BufferedImage bufimg = converter.convert2JavaImg(imgMat);
				for (Blob blob : blobList) {
					int subimgInOneImageCount = 0;

					double randNum = Math.random();
					if (randNum > 15.0 / blobList.size())
						continue;
					CvRect rect = blob.getRect();
					if (!sizeLimit(rect))
						continue;
					if (include(taggedRect, rect))
						continue;

					int x = rect.x();
					int y = rect.y();
					int width = rect.width();
					int height = rect.height();
					BufferedImage subimg = bufimg.getSubimage(x, y, width,
							height);
					try {
						ImageIO.write(subimg, "bmp", new File(dest
								+ subimgCount + ".bmp"));
						subimgInOneImageCount += 1;
						subimgCount += 1;
					} catch (IOException e) {
						e.printStackTrace();
						continue;
					}
					if (subimgCount % 100 == 0)
						System.out.println(subimgCount);
					if (subimgInOneImageCount > 5)
						break;
					if (subimgCount > totalLimit)
						return;
				}
			}
		}
	}

	protected CvRect getTag(String imgName) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(new File(imgName
				+ ".txt")));
		String line;
		while ((line = br.readLine()) != null) {
			if (line.startsWith("LeftTop"))
				break;
		}
		String _x = line.substring(line.indexOf("(") + 1, line.indexOf(","));
		String _y = line.substring(line.indexOf(",") + 1, line.indexOf(")"));
		line = br.readLine();
		String _m = line.substring(line.indexOf("(") + 1, line.indexOf(","));
		String _n = line.substring(line.indexOf(",") + 1, line.indexOf(")"));
		int x = Integer.parseInt(_x);
		int y = Integer.parseInt(_y);
		int w = Integer.parseInt(_m) - x;
		int h = Integer.parseInt(_n) - y;
		Rect rect = new Rect(x, y, w, h);
		br.close();
		return rect.asCvRect();
	}

	protected boolean include(CvRect tag, CvRect rect) {
		int tagx1 = tag.x();
		int tagy1 = tag.y();
		int tagx2 = tagx1 + tag.width();
		int tagy2 = tagy1 + tag.height();

		int rctx1 = rect.x();
		int rcty1 = rect.y();
		int rctx2 = rctx1 + rect.width();
		int rcty2 = rcty1 + rect.height();

		if (rctx1 >= tagx1 && rctx1 <= tagx2) {
			if ((rcty1 >= tagy1 && rcty1 <= tagy2)
					|| (rcty2 >= tagy1 && rcty2 <= tagy2)) {
				return false;
			}
		}
		if (rctx2 >= tagx1 && rctx2 <= tagx2) {
			if ((rcty1 >= tagy1 && rcty1 <= tagy2)
					|| (rcty2 >= tagy1 && rcty2 <= tagy2)) {
				return false;
			}
		}
		return true;
	}

	protected boolean sizeLimit(CvRect rect) {
		int height = rect.height();
		int width = rect.width();
		int size = height * width;
		if (size < 750 || size > 50000) {
			return false;
		}
		if (height < 30 || width < 30) {
			return false;
		}
		return true;
	}

	public static void main(String[] args) {
		 String path = "E:\\train\\";
		 String dest = "E:\\negative3\\";
		 
	}
}
