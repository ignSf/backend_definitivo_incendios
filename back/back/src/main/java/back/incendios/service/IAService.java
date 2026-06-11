package back.incendios.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class IAService {

    @Value("${app.huggingface.url}")
    private String hfUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Llama a la API de HuggingFace para clasificar la gravedad de un incendio.
     * Retorna un Map con "nivel" (1-5) y "confianza" (0-100).
     */
    public Map<String, Object> clasificarImagen(String imagePath) {
        try {
            // Por ahora, la clasificación se haría con la URL de la imagen
            // En producción, enviar la imagen como multipart al Space de HF
            String apiUrl = hfUrl + "/api/predict";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Gradio API expects { "data": [...] }
            Map<String, Object> body = Map.of(
                    "data", List.of(imagePath)
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    apiUrl, HttpMethod.POST, request, Map.class);

            if (response.getBody() != null && response.getBody().containsKey("data")) {
                List<Object> data = (List<Object>) response.getBody().get("data");
                Map<String, Object> result = new HashMap<>();
                result.put("nivel", data.get(0));
                result.put("confianza", data.get(1));
                return result;
            }

            log.warn("Respuesta IA sin datos válidos");
            return defaultResponse();

        } catch (Exception e) {
            log.error("Error llamando a HuggingFace: {}", e.getMessage());
            return defaultResponse();
        }
    }

    private Map<String, Object> defaultResponse() {
        Map<String, Object> result = new HashMap<>();
        result.put("nivel", null);
        result.put("confianza", null);
        return result;
    }
}
