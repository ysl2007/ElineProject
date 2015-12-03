package edu.bit.eline.system.run;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLConnection {
    private String     dbURL;
    private String     userName;
    private String     passwd;
    private Connection dbConn;

    public SQLConnection(String userName, String passwd) {
        this.userName = userName;
        this.passwd = passwd;
    }

    public boolean connect() {
        String driverName = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
        try {
            Class.forName(driverName);
            System.out.println("成功加载MySQL驱动！");
            dbConn = DriverManager.getConnection(dbURL, userName, passwd);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean isClosed() {
        try {
            return dbConn.isClosed();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void main(String[] srg) {

    }
}
