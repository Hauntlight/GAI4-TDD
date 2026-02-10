package it.unisa.gaia.tdd.model.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import okhttp3.*;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExternalClient implements LLMClient {
    private final String serverUrl;
    private final String serverKey;
    private final OkHttpClient client;
    private final ObjectMapper mapper;

    public ExternalClient(String serverUrl, String serverKey) {
        this.serverUrl = serverUrl.startsWith("http") ? serverUrl : "http://" + serverUrl;
        this.serverKey = serverKey;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .build();
        this.mapper = new ObjectMapper();
    }

    @Override
    public String ask(String code, String testCode, String errorMessage) throws IOException {
        String systemPrompt = "You will be provided with a piece of Python code, and your task is to find and fix bugs to pass the last test case, return the full class. You can't modify test cases but you will get the error message of last test case. You will write the bare minimum code to pass the test.\n\n";
        String userPrompt = "Write the bare minimum code to pass the tests.\n\n\nThe test class:\n\n" + testCode + "\n\n\nThe hint is:\n\n" + code + "\n\n" + "The error message is:\n\n"+ errorMessage;
        return sendEncryptedRequest(systemPrompt, userPrompt);
    }

    // Implemented missing method
    @Override
    public String refactor(String code, String testCode) throws IOException {
        String systemPrompt = "You will be provided with a piece of Python code, and your task is to TBD\n\n";
        String userPrompt = "Refactor this Code:\n" + code;
        return sendEncryptedRequest(systemPrompt, userPrompt);
    }

    private String sendEncryptedRequest(String system, String user) throws IOException {
        try {
            // Create a JSON object for the prompts
            ObjectNode prompts = mapper.createObjectNode();
            prompts.put("system", system);
            prompts.put("user", user);
            String jsonString = mapper.writeValueAsString(prompts);

            String encrypted = encrypt(jsonString, serverKey);
            ObjectNode jsonBody = mapper.createObjectNode();
            jsonBody.put("base64", encrypted);

            Request request = new Request.Builder()
                    .url(serverUrl + "/green")
                    .post(RequestBody.create(jsonBody.toString(), MediaType.get("application/json")))
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) throw new IOException("Server Error: " + response.code());

                JsonNode root = mapper.readTree(response.body().string());
                if(root.has("error")) throw new IOException("Remote Error: " + root.get("error").asText());

                String responseEncrypted = root.get("datasf").asText();
                String rawResponse = decrypt(responseEncrypted, serverKey);
                return extractCode(rawResponse);
            }
        } catch (Exception e) {
            throw new IOException("External Server Error: " + e.getMessage(), e);
        }
    }

    private String extractCode(String text) {
        Pattern pattern = Pattern.compile("```(?:python)?([\\s\\S]*?)```");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return text.trim();
    }

    private String encrypt(String plainText, String keyStr) throws Exception {
        byte[] keyBytes = keyStr.getBytes(StandardCharsets.UTF_8);
        SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");

        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key);

        byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encrypted);
    }

    private String decrypt(String b64Text, String keyStr) throws Exception {
        byte[] keyBytes = keyStr.getBytes(StandardCharsets.UTF_8);
        SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");

        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, key);

        byte[] decoded = Base64.getDecoder().decode(b64Text);
        byte[] original = cipher.doFinal(decoded);
        return new String(original, StandardCharsets.UTF_8);
    }
}