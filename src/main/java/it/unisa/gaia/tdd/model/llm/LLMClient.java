package it.unisa.gaia.tdd.model.llm;

import java.io.IOException;

public interface LLMClient {
    String ask(String code, String testCode, String errorMessage) throws IOException;
    String refactor(String code, String testCode) throws IOException;

}