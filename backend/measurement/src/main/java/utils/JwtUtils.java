package utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.Base64;

@Component
public class JwtUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String getRoleFromToken(String token) {
        try {
            String[] splitToken = token.split("\\.");
            if (splitToken.length < 2) return null;

            // Decodifica a parte do payload do token (Base64)
            String payloadJson = new String(Base64.getDecoder().decode(splitToken[1]));

            // Converte JSON para objeto e extrai a role
            JsonNode payloadNode = objectMapper.readTree(payloadJson);
            var role =payloadNode.has("role") ? payloadNode.get("role").asText() : null;
            System.out.println("ROLE!!!!!!!!" + role);
            return role;


        } catch (Exception e) {
            return null; // Se houver erro, retorna null
        }
    }
}
