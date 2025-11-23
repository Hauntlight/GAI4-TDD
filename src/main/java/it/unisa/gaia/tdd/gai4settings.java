package it.unisa.gaia.tdd;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(
        name = "gai4settings",
        storages = {@Storage("gai4settings.xml")}
)
public class gai4settings implements PersistentStateComponent<gai4settings.State> {
    private State state = new State();

    public static gai4settings getInstance() {
        return ApplicationManager.getApplication().getService(gai4settings.class);
    }

    @Nullable
    @Override
    public State getState() {
        return state;
    }

    @Override
    public void loadState(@NotNull State state) {
        this.state = state;
    }

    public String getApiKey() { return state.apiKey; }
    public void setApiKey(String apiKey) { state.apiKey = apiKey; }

    public String getAssistantModel() { return state.assistantModel; }
    public void setAssistantModel(String assistantModel) { state.assistantModel = assistantModel; }

    public String getServer() { return state.server; }
    public void setServer(String server) { state.server = server; }

    public String getServerKey() { return state.serverKey; }
    public void setServerKey(String serverKey) { state.serverKey = serverKey; }

    public static class State {
        public String apiKey = "";
        public String assistantModel = "gpt-4";
        public String server = "";
        public String serverKey = "";
    }
}