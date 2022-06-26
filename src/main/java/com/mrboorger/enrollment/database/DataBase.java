package com.mrboorger.enrollment.database;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Properties;

public class DataBase {
    static private Connection connection;
    static private Statement statement;

    private static class SingletonObjectDataBase {
        private static final DataBase INSTANCE;
        static {
            try {
                INSTANCE = new DataBase();
            } catch (Exception e) {
                throw new ExceptionInInitializerError(e);
            }
        }

        static private DataBase getInstance() {
            return INSTANCE;
        }
    }

    public static DataBase getInstance(){
        return SingletonObjectDataBase.getInstance();
    }

    private DataBase() throws SQLException {
        Properties props = new Properties();
        try(InputStream in = Files.newInputStream(Paths.get("database.properties"))){
            props.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String url = props.getProperty("url");
        String username = props.getProperty("username");
        String password = props.getProperty("password");

        try {
            Class.forName("com.mysql.cj.jdbc.Driver").getDeclaredConstructor().newInstance();
            connection = DriverManager.getConnection(url, username, password);
            statement = connection.createStatement();
        }
        catch(Exception ex){
            throw new SQLException();
        }
    }

    public ResultSet executeQuery(String sql)throws SQLException {
        return statement.executeQuery(sql);
    }

    public int executeUpdate(String sql)throws SQLException {
        return statement.executeUpdate(sql);
    }
}
