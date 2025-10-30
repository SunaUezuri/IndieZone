package br.com.lunix.util;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.client.RestTemplate;
import java.util.List;

/**
 * Ferramenta utilitária para explorar e listar todos os dados mestres da API da RAWG.
 * Execute o método main() para obter uma lista completa de todos os gêneros.
 * Isso ajuda a construir mappers completos e robustos.
 */
public class ApiDataExplorer {

    private static final String API_KEY = "SUA-CHAVE-API";
    private static final String BASE_URL = "https://api.rawg.io/api";
    private static final RestTemplate restTemplate = new RestTemplate();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // DTOs locais para esta ferramenta
    @JsonIgnoreProperties(ignoreUnknown = true)
    private record MasterDataDto(String slug, String name) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ApiResponse(String next, List<MasterDataDto> results) {}

    public static void main(String[] args) throws JsonProcessingException {
        System.out.println("--- Buscando todos os Gêneros da RAWG ---");
        fetchAll("genres");

        System.out.println("===================================================================");

         System.out.println("\n--- Buscando todas as Plataformas da RAWG ---");
         fetchAll("platforms");
    }

    private static void fetchAll(String resource) throws JsonProcessingException {
        String nextUrl = BASE_URL + "/" + resource + "?key=" + API_KEY;

        while (nextUrl != null) {
            String jsonResponse = restTemplate.getForObject(nextUrl, String.class);
            ApiResponse response = objectMapper.readValue(jsonResponse, ApiResponse.class);

            for (MasterDataDto item : response.results()) {
                System.out.printf("Slug: %-30s | Name: %s\n", item.slug(), item.name());
            }

            nextUrl = response.next();
        }
    }
}