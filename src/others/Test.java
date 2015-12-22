package others;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class Test {
    public static void main(String[] args) throws UnsupportedEncodingException {
        String url = "H%3A%2FVideo_Photo%2F%E6%98%8C%E5%9F%8E18%E5%A4%A7%28%E6%96%BD%E5%B7%A5%E4%BD%9C%E4%B8%9A%29%2F2015-12%2F2015-12-01%2FPIC%2F%E6%98%8C%E5%9F%8E18%E5%A4%A7%28%E6%96%BD%E5%B7%A5%E4%BD%9C%E4%B8%9A%29_20151201_120255_T_CH1_P1.jpg";
        String test = URLDecoder.decode(url, "UTF-8");
        System.out.println(test);
    }
}
