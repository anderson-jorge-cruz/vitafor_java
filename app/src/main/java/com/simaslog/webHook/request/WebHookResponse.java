package com.simaslog.webHook.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Modelo de resposta padrão da API")
public class WebHookResponse {

    @Schema(description = "Código HTTP da resposta", example = "200")
    private int status;

    @Schema(description = "Mensagem da resposta", example = "Operação realizada com sucesso")
    private String message;

    public WebHookResponse(int status, String message) {
        this.status = status;
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}