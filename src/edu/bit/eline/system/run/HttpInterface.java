package edu.bit.eline.system.run;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

public class HttpInterface {
    private static final String imgListUrl = "http://10.156.118.5/gwzd/ImageService.do?action=getImageListByLineName&LineName=";
    private static final String imgUrl     = "http://10.156.118.5/gwzd/ImageService.do?action=getImageDataByFileName&FileName=";
    private static final String deviceUrl  = "http://10.156.118.5/gwzd/TreeService.do?action=getTreeNew";

    // 两次编码
    private static String encodeTwice(String str) throws UnsupportedEncodingException {
        String _str;
        _str = URLEncoder.encode(str, "UTF-8");
        _str = _str.replaceAll("[+]", "%20");
        _str = URLEncoder.encode(_str, "UTF-8");
        return _str;
    }

    // 一次编码
    private static String encodeOnce(String str) throws UnsupportedEncodingException {
        String _str;
        _str = URLEncoder.encode(str, "UTF-8");
        _str = _str.replaceAll("[+]", "%20");
        return _str;
    }

    // 网络接口连接
    private static InputStream connect(String url) throws IOException {
        URL realUrl = new URL(url);
        URLConnection connection = realUrl.openConnection();
        connection.setReadTimeout(15000);
        connection.setRequestProperty("accept", "*/*");
        connection.setRequestProperty("connection", "close");
        connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
        connection.connect();
        // Map<String, List<String>> map = connection.getHeaderFields();
        // for (String key : map.keySet()){
        // System.out.println(key + ": " + map.get(key));
        // }
        return connection.getInputStream();
    }

    // 获取某时间段图片列表
    public static List<String> getImageList(String lineName, String start, String end) {
        List<String> imageList = new ArrayList<String>();
        String result = "";
        InputStream is;
        // 由于对方接口的原因，必须对中文线路名进行两次URL编码，对起止时间进行一次编码。
        try {
            lineName = encodeTwice(lineName);
            start = encodeOnce(start);
            end = encodeOnce(end);
            String url = imgListUrl + lineName + "&Type=2&StartDate=" + start + "&EndDate=" + end;
            is = connect(url);

        } catch (Exception e) {
            System.err.println("获取图片路径列表异常");
            e.printStackTrace();
            return imageList;
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String line;
        try {
            // 不出错的话，返回结果应该只有1行。
            while ((line = reader.readLine()) != null) {
                result += line;
            }
        } catch (IOException e1) {
            e1.printStackTrace();
            return imageList;
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 解析结果，返回图片路径列表
        if (result.length() == 0 || result.startsWith("null") || result.startsWith("[]")) {
            return imageList;
        }
        result = result.trim().substring(1, result.length() - 1);
        String[] paths = result.split(",");
        for (String path : paths) {
            path = path.substring(1, path.length() - 1);
            imageList.add(path);
        }
        return imageList;
    }

    // 提供图片路径，获取图片
    public static BufferedImage getImage(String imagePath) {
        try {
            imagePath = encodeOnce(imagePath);
        } catch (UnsupportedEncodingException e2) {
            e2.printStackTrace();
        }
        String url = imgUrl + imagePath;
        InputStream is;
        BufferedImage bimg = null;
        try {
            is = connect(url);
        } catch (IOException e1) {
            System.err.println("图片下载连接异常");
            e1.printStackTrace();
            return bimg;
        }
        try {
            bimg = ImageIO.read(is);
        } catch (IOException e) {
            System.err.println("图片读取异常");
            e.printStackTrace();
        }
        return bimg;
    }

    // 获取设备树
    public static String getDeviceTree() throws UnsupportedEncodingException {
        String url = deviceUrl;
        String result = "";
        InputStream is;
        try {
            is = connect(url);
        } catch (Exception e) {
            System.err.println("获取设备树异常" + e);
            e.printStackTrace();
            return null;
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                result += line;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        try {
            reader.close();
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        return result;
    }

}
