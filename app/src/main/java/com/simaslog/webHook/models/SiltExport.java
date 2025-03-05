package com.simaslog.webHook.models;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.simaslog.webHook.database.MySQL;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SiltExport {
    private String apikey;
    private String layout;
    private String payload;
    private Connection connection;

    public SiltExport(String apikey, String layout, String payload) throws ClassNotFoundException {
        this.apikey = apikey;
        this.layout = layout;
        this.payload = payload;
        this.connection = MySQL.getInstance();
    }

    public void store() throws SQLException {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String compactJson = objectMapper.writeValueAsString(objectMapper.readTree(payload));

            var sql = "INSERT INTO integradorsm.silt_export (apikey,layout,payload) values (?,?,?)";
            var stmt = connection.prepareStatement(sql);
            stmt.setString(1, apikey);
            stmt.setString(2, layout);
            stmt.setString(3, compactJson);
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
