package com.simaslog.webHook.controllers;

import com.simaslog.webHook.models.SiltExport;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;

@RestController
@RequestMapping("/api/v2")
public class WebHookHandler {

    @CrossOrigin(origins = {"https://wms.simaslog.com.br/*", "https://wmshml.simaslog.com.br/*"})
    @RequestMapping(value = "vitafor",method =  RequestMethod.POST)
    public ResponseEntity<Void> store(HttpServletRequest request, HttpServletResponse response) throws JSONException, SQLException, ClassNotFoundException {
        StringBuilder body = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                body.append(line);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String bodyContent = body.toString().trim();
        String layout = "";
        String apiKey = request.getHeader("apiKey");

        if (bodyContent.trim().startsWith("{")) {
            JSONObject json = new JSONObject(bodyContent);
            if (json.has("chavelayout")) {
                layout = json.getString("chavelayout");
            }
        }

        new SiltExport(apiKey, layout, bodyContent).store();

        return ResponseEntity.noContent().build();
    }
}
