package com.simaslog.webHook.request;

import java.util.List;

public record SiltExportResponse(
        String layout,
        List<java.util.Map<String, Object>> payload
) {}
