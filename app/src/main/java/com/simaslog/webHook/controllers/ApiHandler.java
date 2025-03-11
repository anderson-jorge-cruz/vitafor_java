package com.simaslog.webHook.controllers;

import com.simaslog.webHook.request.SiltExportRequest;
import com.simaslog.webHook.request.SiltExportResponse;
import com.simaslog.webHook.services.SiltExportService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.coyote.Response;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v2/export")
public class ApiHandler {

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<SiltExportResponse>> export(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ClassNotFoundException
    {
        String ApiKey = request.getHeader("apikey");
        if (ApiKey == null) {
            return ResponseEntity.badRequest().build();
        }

        if (! SiltExportRequest.auth(ApiKey)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            StringBuilder body = new StringBuilder();
            try (BufferedReader reader = request.getReader()) {
                String line;
                while ((line = reader.readLine()) != null) {
                    body.append(line);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            if (body.isEmpty()) {
                body.append("{}");
            }

            JSONObject json = new JSONObject(body.toString());
            String layout = json.optString("layout", null);
            String exportDate = json.optString("export_date", null);

            SiltExportService service = new SiltExportService();

            Optional<String> layoutOpt = Optional.ofNullable(layout);
            Optional<Date> dataOpt = Optional.empty();
            if (exportDate != null && !exportDate.isEmpty()) {
                try {
                    dataOpt = Optional.of(java.sql.Date.valueOf(exportDate));
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest().build();
                }
            }

            List<SiltExportResponse> result = service.exportData(ApiKey, layoutOpt, dataOpt);

            if (result.isEmpty()) {
                return ResponseEntity.noContent().build();
            }

            return ResponseEntity.ok(result);

        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
