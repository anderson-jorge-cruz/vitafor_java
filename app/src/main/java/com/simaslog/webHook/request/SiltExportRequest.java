package com.simaslog.webHook.request;

import com.simaslog.webHook.database.MySQL;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SiltExportRequest {
    private String apikey;
    private String layout;
    private String exporteDate;
    private static Connection connection;

    static {
        try {
            connection = MySQL.getInstance();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public SiltExportRequest(String apikey, String layout, String exporteDate) throws ClassNotFoundException {
        this.apikey = apikey;
        this.layout = layout;
        this.exporteDate = exporteDate;
    }

    public static boolean auth(String apiKey) {
        String query = "SELECT COUNT(*) FROM integradorsm.silt_export WHERE apikey = ?";

        try {
             PreparedStatement stmt = connection.prepareStatement(query);

            stmt.setString(1, apiKey);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
