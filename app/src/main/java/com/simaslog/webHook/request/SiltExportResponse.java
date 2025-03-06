package com.simaslog.webHook.request;

import java.util.List;
import java.util.Map;

public record SiltExportResponse(
        String layout,
        List<Map<String, Object>> payload
) {}
