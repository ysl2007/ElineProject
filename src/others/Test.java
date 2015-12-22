package others;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import edu.bit.eline.system.run.SQLConnection;

public class Test {
    public static void main(String[] args) throws UnsupportedEncodingException {
        SQLConnection dbconn = new SQLConnection();
        dbconn.connect();
        System.out.println(dbconn.isClosed());
    }
}
