package it.unisa.gaia.tdd.model.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.intellij.openapi.diagnostic.Logger;
import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OpenAIClient implements LLMClient {
    private static final Logger LOGGER = Logger.getInstance(OpenAIClient.class);

    private final String apiKey;
    private final String model;
    private final OkHttpClient client;
    private final ObjectMapper mapper;

    public OpenAIClient(String apiKey, String model) {
        this.apiKey = apiKey;
        this.model = model;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .build();
        this.mapper = new ObjectMapper();
    }

    private String callGpt(String system, String user) throws IOException {
        LOGGER.warn("GAI4-TDD Prompt:\n[SYSTEM]: " + system + "\n[USER]: " + user);

        ObjectNode requestBody = mapper.createObjectNode();
        requestBody.put("model", model);
        requestBody.put("temperature", 0.1);

        ArrayNode messages = requestBody.putArray("messages");
        messages.addObject().put("role", "system").put("content", system);
        messages.addObject().put("role", "user").put("content", user);

        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .addHeader("Authorization", "Bearer " + apiKey)
                .post(RequestBody.create(requestBody.toString(), MediaType.get("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("OpenAI Error: " + response.code() + " " + (response.body() != null ? response.body().string() : ""));
            }
            JsonNode root = mapper.readTree(response.body().string());
            String content = root.path("choices").get(0).path("message").path("content").asText();
            return extractCode(content);
        }
    }

    @Override
    public String ask(String code, String testCode, String errorMessage) throws IOException {
        String systemPrompt = "You will be provided with a piece of Python code, and your task is to find and fix bugs to pass the last test case, return the full class. You can't modify test cases but you will get the error message of last test case. You will write the bare minimum code to pass the test.\n\n";
        String userPrompt = "Write the bare minimum code to pass the tests.\n\n\nThe test class:\n\n" + testCode + "\n\n\nThe hint is:\n\n" + code + "\n\n" + "The error message is:\n\n"+ errorMessage;
        return callGpt(systemPrompt, userPrompt);
    }

    // Implemented missing method
    @Override
    public String refactor(String code, String testCode) throws IOException {
        String systemPrompt = "You are an expert Refactorer. You will be provided with a Python code. Refactor it to be cleaner and more efficient without breaking tests. Return the full class.";
        String userPrompt = "Code:\n" + code + "\n\nTest Context:\n" + testCode;
        return callGpt(systemPrompt, userPrompt);
    }

    private String extractCode(String text) {
        Pattern pattern = Pattern.compile("```(?:python)?([\\s\\S]*?)```");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return text.trim();
    }
}