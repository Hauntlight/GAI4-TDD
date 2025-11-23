package it.unisa.gaia.tdd.model.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.intellij.openapi.diagnostic.Logger;
import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class OpenAIGPT5Client implements LLMClient {
    private static final Logger LOGGER = Logger.getInstance(OpenAIGPT5Client.class);

    private final String apiKey;
    private final String model;
    private final OkHttpClient client;
    private final ObjectMapper mapper;

    public OpenAIGPT5Client(String apiKey, String model) {
        this.apiKey = apiKey;
        this.model = model;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(120, TimeUnit.SECONDS) // GPT-5 might take longer with reasoning
                .readTimeout(180, TimeUnit.SECONDS)
                .build();
        this.mapper = new ObjectMapper();
    }

    private String callGpt5(String developerContent, String userContent) throws IOException {
        LOGGER.warn("GAI4-TDD GPT-5 Prompt:\n[DEVELOPER]: " + developerContent + "\n[USER]: " + userContent);

        ObjectNode requestBody = mapper.createObjectNode();
        requestBody.put("model", model);
        requestBody.put("reasoning_effort", "medium");
        // requestBody.put("verbosity", "medium"); // Optional based on your API docs, usually for reasoning steps output

        // 1. Build Messages
        ArrayNode messages = requestBody.putArray("messages");

        // Developer Message
        ObjectNode devMsg = messages.addObject();
        devMsg.put("role", "developer");
        ArrayNode devContentArray = devMsg.putArray("content");
        ObjectNode devTextObj = devContentArray.addObject();
        devTextObj.put("type", "text");
        devTextObj.put("text", developerContent);

        // User Message
        ObjectNode userMsg = messages.addObject();
        userMsg.put("role", "user");
        ArrayNode userContentArray = userMsg.putArray("content");
        ObjectNode userTextObj = userContentArray.addObject();
        userTextObj.put("type", "text");
        userTextObj.put("text", userContent);

        // 2. Build Response Format (JSON Schema)
        ObjectNode responseFormat = requestBody.putObject("response_format");
        responseFormat.put("type", "json_schema");

        ObjectNode jsonSchema = responseFormat.putObject("json_schema");
        jsonSchema.put("name", "code_solution");
        jsonSchema.put("strict", true);

        ObjectNode schema = jsonSchema.putObject("schema");
        schema.put("type", "object");
        schema.put("additionalProperties", false);

        ArrayNode required = schema.putArray("required");
        required.add("code");

        ObjectNode properties = schema.putObject("properties");
        ObjectNode codeProp = properties.putObject("code");
        codeProp.put("type", "string");
        codeProp.put("description", "The fully functional Python class code that satisfies the provided test suite.");

        // 3. Send Request
        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .addHeader("Authorization", "Bearer " + apiKey)
                .post(RequestBody.create(requestBody.toString(), MediaType.get("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "";
                throw new IOException("GPT-5 API Error: " + response.code() + " " + errorBody);
            }

            String responseString = response.body().string();
            JsonNode root = mapper.readTree(responseString);

            // 4. Parse Response
            // The 'content' field is now a JSON String because of Structured Outputs
            String innerJsonString = root.path("choices").get(0).path("message").path("content").asText();

            // Parse the inner JSON to get the actual code
            JsonNode innerJson = mapper.readTree(innerJsonString);
            return innerJson.path("code").asText();
        }
    }

    @Override
    public String ask(String code, String testCode, String errorMessage) throws IOException {
        String developerPrompt = "You will be provided with a piece of Python code, and your task is to find and fix bugs to pass the last test case, return the full class. You can't modify test cases but you will get the error message of last test case. You will write the bare minimum code to pass the test.\n\n";
        String userPrompt = "Write the bare minimum code to pass the tests.\n\n\nThe test class:\n\n" + testCode + "\n\n\nThe hint is:\n\n" + code + "\n\n" + "The error message is:\n\n"+ errorMessage;
        return callGpt5(developerPrompt, userPrompt);
    }

    @Override
    public String refactor(String code, String testCode) throws IOException {
        String developerPrompt = "You are an expert Python Refactorer. Refactor the code to be cleaner, efficient, and Pythonic without breaking the tests. Return ONLY the code.";
        String userPrompt = "Refactor this code:\n" + code + "\n\nContext (Test Suite):\n" + testCode;
        return callGpt5(developerPrompt, userPrompt);
    }
}