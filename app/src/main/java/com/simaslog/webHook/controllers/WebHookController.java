package com.simaslog.webHook.controllers;

import com.simaslog.webHook.models.SiltExport;
import com.simaslog.webHook.request.SiltExportRequest;
import com.simaslog.webHook.request.SiltExportResponse;
import com.simaslog.webHook.request.WebHookResponse;
import com.simaslog.webHook.services.SiltExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v2")
@Tag(name = "Swagger OpenApi - SIMASLOG")
public class WebHookController {

    @Operation(
            summary = "Recebe e guarda as exportações do WMS via webhook",
            description = "Este endpoint recebe um JSON contendo `chavelayout` e `list` para armazenar no sistema.",
            method = "POST"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Exportação recebida e salva com sucesso.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = WebHookResponse.class),
                            examples = @ExampleObject(value = "{\"status\": 201, \"message\": \"Exportação salva com sucesso.\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Parâmetros incorretos ou JSON inválido.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = WebHookResponse.class),
                            examples = @ExampleObject(value = "{\"status\": 400, \"message\": \"Campo 'chavelayout' é obrigatório.\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuário não autenticado.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = WebHookResponse.class),
                            examples = @ExampleObject(value = "{\"status\": 401, \"message\": \"Não autorizado. Chave de API faltando.\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = WebHookResponse.class),
                            examples = @ExampleObject(value = "{\"status\": 500, \"message\": \"Erro interno ao processar a requisição.\"}")
                    )
            )
    })
    @PostMapping(
            name = "/webhook",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @RequestMapping(path = "/webhook", method = RequestMethod.POST)
    public ResponseEntity<WebHookResponse> store(
            @RequestHeader(name = "ApiKey", required = true) String apiKey,
            @RequestBody(
                    required = true,
                    description = "Objeto JSON contendo a chave de layout e uma lista de objetos.",
                    content = @Content(
                            schema = @Schema(
                                    example = "{\"chavelayout\": \"layout123\", \"list\": [{\"id\": 1, \"nome\": \"Item 1\"}, {\"id\": 2, \"nome\": \"Item 2\"}]}"
                            )
                    )
            ) StringBuilder requestBody,
            HttpServletRequest request
    ) throws JSONException, SQLException, ClassNotFoundException {

        StringBuilder body = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                body.append(line);
            }
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new WebHookResponse(500, "Erro ao ler a requisição."));
        }

        if (body.isEmpty()) {
            return ResponseEntity.badRequest().body(new WebHookResponse(400, "O corpo da requisição não pode estar vazio."));
        }

        String bodyContent = body.toString().trim();

        try {
            JSONObject json = new JSONObject(bodyContent);

            // Verifica se os campos obrigatórios existem
            if (!json.has("chavelayout") || json.getString("chavelayout").isEmpty()) {
                return ResponseEntity.badRequest().body(new WebHookResponse(400, "Campo 'chavelayout' é obrigatório."));
            }

            if (!json.has("list") || !(json.get("list") instanceof JSONArray) || json.getJSONArray("list").isEmpty()) {
                return ResponseEntity.badRequest().body(new WebHookResponse(400, "Campo 'list' deve ser um array de objetos JSON e não pode estar vazio."));
            }

            String layout = json.getString("chavelayout");

            // Se passou na validação, processa o armazenamento
            new SiltExport(apiKey, layout, bodyContent).store();

            return ResponseEntity.status(HttpStatus.CREATED).body(new WebHookResponse(201, "Exportação salva com sucesso."));
        } catch (JSONException e) {
            return ResponseEntity.badRequest().body(new WebHookResponse(400, "JSON inválido: " + e.getMessage()));
        }
    }

    @Operation(
            summary = "Retorna exportações do WMS baseadas na chave API do depositante",
            description = "Este endpoint retorna exportações filtradas opcionalmente por `layout` e/ou `export_date`.",
            method = "GET"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Exportações retornadas com sucesso.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SiltExportResponse.class),
                            examples = @ExampleObject(value = "[{\"layout\":\"vi_int_or_lote_h\",\"payload\":[{\"chavelayout\":\"vi_int_or_lote_h\",\"list\":[{\"or\":\"1\",\"ano\":\"2\",\"mes\":\"1\",\"dia\":\"1\",\"hora\":\"1\",\"cnpj\":\"2\",\"codtransp\":\"2\",\"classificacao\":\"2\",\"placa\":\"1212\",\"motorista\":\"1\",\"qtdevolume\":\"1\",\"peso\":\"1\",\"tiporecebimento\":\"S\",\"infocomplementar\":\"asda\",\"momentoenvio\":\"asd\",\"idreplicacao\":null}],\"exported_at\":\"2025-03-06 08:26:16\"}]}]")
                    )
            ),
            @ApiResponse(
                    responseCode = "204",
                    description = "Nenhuma exportação encontrada para os filtros informados."
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Parâmetros incorretos na requisição.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = "{\"status\": 400, \"message\": \"O campo 'export_date' deve estar no formato 'yyyy-MM-dd'.\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuário não autenticado.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = "{\"status\": 401, \"message\": \"Não autorizado. Chave de API inválida.\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = "{\"status\": 500, \"message\": \"Erro interno ao processar a requisição.\"}")
                    )
            )
    })
    @GetMapping(
            name = "/export",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @RequestMapping(path = "/export", method = RequestMethod.GET)
    public ResponseEntity<?> export(
            @RequestHeader(name = "ApiKey") String apiKey,
            @RequestParam(value = "layout", required = false) String layout,  // Pega o parâmetro 'layout' da query
            @RequestParam(value = "export_date", required = false) String exportDateStr,  // Pega o parâmetro 'export_date' da query
            HttpServletRequest request
    ) throws ClassNotFoundException {

        if (apiKey == null) {
            return ResponseEntity.badRequest().body(new WebHookResponse(400, "O cabeçalho 'apikey' é obrigatório."));
        }

        if (!SiltExportRequest.auth(apiKey)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new WebHookResponse(401, "Não autorizado. Chave de API inválida."));
        }

        try {
            // Se a query string estiver vazia, usar um JSON vazio
            JSONObject json = new JSONObject();

            // Definir valores de layout e export_date se estiverem presentes
            if (layout != null) {
                json.put("layout", layout);
            }
            if (exportDateStr != null && !exportDateStr.isEmpty()) {
                try {
                    json.put("export_date", exportDateStr);
                } catch (Exception e) {
                    return ResponseEntity.badRequest().body(new WebHookResponse(400, "O campo 'export_date' deve estar no formato 'yyyy-MM-dd'."));
                }
            }

            StringBuilder body = new StringBuilder();
            body.append(json.toString()); // Aqui transformamos o JSON em uma string

            // Pega os parâmetros da URL
            Optional<String> layoutOpt = Optional.ofNullable(layout);
            Optional<Date> dataOpt = Optional.empty();

            // Convertendo o 'export_date' se necessário
            if (exportDateStr != null && !exportDateStr.isEmpty()) {
                try {
                    dataOpt = Optional.of(java.sql.Date.valueOf(exportDateStr));
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest().body(new WebHookResponse(400, "O campo 'export_date' deve estar no formato 'yyyy-MM-dd'."));
                }
            }

            SiltExportService service = new SiltExportService();
            List<SiltExportResponse> result = service.exportData(apiKey, layoutOpt, dataOpt);

            if (result.isEmpty()) {
                return ResponseEntity.noContent().build();
            }

            return ResponseEntity.ok(result);

        } catch (SQLException e) {
            return ResponseEntity.internalServerError()
                    .body(new WebHookResponse(500, "Erro interno ao processar a requisição."));
        }
    }
}