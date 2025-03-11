package com.simaslog.webHook.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.simaslog.webHook.database.MySQL;
import com.simaslog.webHook.request.SiltExportResponse;

import java.sql.*;
import java.util.*;
import java.util.Date;

public class SiltExportService {

    private final Connection connection;

    public SiltExportService() throws ClassNotFoundException {
        this.connection = MySQL.getInstance();
    }

    public List<SiltExportResponse> exportData(String apikey, Optional<String> layout, Optional<Date> exportDate) throws SQLException {
        String sql = """
            SELECT layout, payload, exported_at
            FROM integradorsm.silt_export
            WHERE apikey = ? 
            """ + (layout.isPresent() ? "AND layout = ? " : "") +
                (exportDate.isPresent() ? "AND date(exported_at) = ? " : "");

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            int paramIndex = 1;
            stmt.setString(paramIndex++, apikey);
            if (layout.isPresent()) stmt.setString(paramIndex++, layout.get());
            if (exportDate.isPresent()) stmt.setDate(paramIndex++, new java.sql.Date(exportDate.get().getTime()));

            ResultSet rs = stmt.executeQuery();
            Map<String, List<Map<String, Object>>> groupedData = new HashMap<>();

            while (rs.next()) {
                String layoutKey = rs.getString("layout");
                String payload = rs.getString("payload");
                String exportedAt = rs.getString("exported_at");

                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, Object> payloadMap = objectMapper.readValue(payload, new TypeReference<Map<String, Object>>() {});

                payloadMap.put("exported_at", exportedAt);
                groupedData.putIfAbsent(layoutKey, new ArrayList<>());
                groupedData.get(layoutKey).add(payloadMap);
            }

            List<SiltExportResponse> responses = new ArrayList<>();
            for (var entry : groupedData.entrySet()) {
                responses.add(new SiltExportResponse(entry.getKey(), entry.getValue()));
            }

            return responses;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
