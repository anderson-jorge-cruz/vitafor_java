package com.simaslog.webHook.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQL {
    private static final String URL = "jdbc:mysql://216.238.121.35:3306/integradorsm";
    private static final String USER = "root";
    private static final String PASSWORD = "integradorsm@2024";

    private static Connection instance;

    public static Connection getInstance() throws ClassNotFoundException {
        if  (instance == null) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                instance = DriverManager.getConnection(URL, USER, PASSWORD);
            } catch (ClassNotFoundException e) {
                System.err.println("Driver JDBC MySQL não encontrado:" + e.getMessage() + "\n");
            } catch (SQLException e) {
                System.err.println("Erro ao conectar ao banco de dados MySQL:" + e.getMessage() + "\n");
            }
        }
        return instance;
    }

    private MySQL() {}

    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    public static void close() {
        if (instance != null) {
            try {
                instance.close();
                instance = null;
            } catch (SQLException e) {
                System.err.println("Erro ao fechar a conexão: " + e.getMessage() + "\n");
            }
        }
    }
}
