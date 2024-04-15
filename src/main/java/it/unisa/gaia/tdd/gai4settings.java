package it.unisa.gaia.tdd;

import com.intellij.openapi.components.*;
import org.jetbrains.annotations.Nullable;

//Questo codice gestisce la persistenza

@State(
        name = "gai4settings",
        storages = {@Storage(value = "gai4settings.xml")} // Specifica solo il nome del file
)
public class gai4settings implements PersistentStateComponent<gai4settings.State> {
    private State state = new State();

    public static gai4settings getInstance() {
        return ServiceManager.getService(gai4settings.class);
    }

    @Nullable
    @Override
    public State getState() {
        return state;
    }

    @Override
    public void loadState(State state) {
        this.state = state;
    }

    public String getApiKey() {
        return state.apiKey;
    }

    public void setApiKey(String apiKey) {
        state.apiKey = apiKey;
    }
    
    public String getAssistantModel() {
        return state.assistantModel;
    }

    public void setAssistantModel(String assistantModel) {
        state.assistantModel = assistantModel;
    }

    public String getServer(){return this.state.server;}

    public void setServer(String server){this.state.server = server;}

    public String getServerKey(){return this.state.serverKey;}

    public void setServerKey(String serverKey){this.state.serverKey = serverKey;}

    public static class State {
        public String apiKey = "";
        public String assistantModel = "";
        public String server = "";
        public String serverKey = "";
    }
}
