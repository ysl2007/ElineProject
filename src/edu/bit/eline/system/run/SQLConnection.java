package edu.bit.eline.system.run;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.JOptionPane;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class SQLConnection {
    private String     configFile = "./config.json";
    private String     dbURL;
    private String     userName;
    private String     passwd;
    private Connection dbConn;

    public SQLConnection() {
        JSONTokener tokener;
        try {
            tokener = new JSONTokener(new FileReader(configFile));
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(null, "找不到配置文件。", "错误",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return;
        }
        JSONObject jo = new JSONObject(tokener);
        try {
            dbURL = jo.getString("database_url");
            userName = jo.getString("database_username");
            passwd = jo.getString("database_password");
        } catch (JSONException e) {
            JOptionPane.showMessageDialog(null, "配置文件不完整。", "错误",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return;
        }
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

    public boolean insert(String sql) {
        try {
            Statement stmt = dbConn.createStatement();
            int lines = stmt.executeUpdate(sql);
            if (lines != 0) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void main(String[] srg) {

    }
}
