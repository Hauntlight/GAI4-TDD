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

public class OpenAIGPT51CodexClient implements LLMClient {
    private static final Logger LOGGER = Logger.getInstance(OpenAIGPT51CodexClient.class);

    private final String apiKey;
    private final String model;
    private final OkHttpClient client;
    private final ObjectMapper mapper;

    public OpenAIGPT51CodexClient(String apiKey, String model) {
        this.apiKey = apiKey;
        this.model = model;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .build();
        this.mapper = new ObjectMapper();
    }

    private String callCodex(String system, String user) throws IOException {
        LOGGER.warn("GAI4-TDD GPT-5.1-Codex Prompt:\n[SYSTEM]: " + system + "\n[USER]: " + user);

        ObjectNode requestBody = mapper.createObjectNode();
        requestBody.put("model", model);

        // Build 'input' array
        ArrayNode inputArray = requestBody.putArray("input");

        // Message 1: User role with System instructions (simulated as input_text)
        ObjectNode msg1 = inputArray.addObject();
        msg1.put("role", "user");
        ArrayNode content1 = msg1.putArray("content");
        ObjectNode textObj1 = content1.addObject();
        textObj1.put("type", "input_text");
        textObj1.put("text", system);

        // Message 2: User role with actual Prompt
        ObjectNode msg2 = inputArray.addObject();
        msg2.put("role", "user");
        ArrayNode content2 = msg2.putArray("content");
        ObjectNode textObj2 = content2.addObject();
        textObj2.put("type", "input_text");
        textObj2.put("text", user);

        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/responses")
                .addHeader("Authorization", "Bearer " + apiKey)
                .post(RequestBody.create(requestBody.toString(), MediaType.get("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String err = response.body() != null ? response.body().string() : "";
                throw new IOException("Codex API Error: " + response.code() + " " + err);
            }

            String responseString = response.body().string();
            JsonNode root = mapper.readTree(responseString);

            // Explicit Traversal based on the provided JSON log
            if (root.has("output") && root.get("output").isArray()) {
                ArrayNode outputArray = (ArrayNode) root.get("output");
                for (JsonNode item : outputArray) {
                    // Look for the item with type "message" (skipping "reasoning")
                    if (item.has("type") && "message".equals(item.get("type").asText())) {
                        if (item.has("content") && item.get("content").isArray()) {
                            ArrayNode contentArray = (ArrayNode) item.get("content");
                            for (JsonNode contentItem : contentArray) {
                                // Look for the content item with type "output_text"
                                if (contentItem.has("type") && "output_text".equals(contentItem.get("type").asText())) {
                                    if (contentItem.has("text")) {
                                        String rawText = contentItem.get("text").asText();
                                        return extractCode(rawText);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            throw new IOException("Could not find 'output_text' in response: " + responseString);
        }
    }

    @Override
    public String ask(String code, String testCode, String errorMessage) throws IOException {
        String systemPrompt = "You will be provided with a piece of Python code, and your task is to find and fix bugs to pass the last test case, return the full class. You can't modify test cases but you will get the error message of last test case. You will write the bare minimum code to pass the test.";
        String userPrompt = "Write the bare minimum code to pass the tests.\n\n\nThe test class:\n\n" + testCode + "\n\n\nThe hint is:\n\n" + code + "\n\n" + "The error message is:\n\n"+ errorMessage;
        return callCodex(systemPrompt, userPrompt);
    }

    @Override
    public String refactor(String code, String testCode) throws IOException {
        throw new IOException("Refactor is not supported for model " + model);
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