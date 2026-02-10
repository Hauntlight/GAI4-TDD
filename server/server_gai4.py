import base64
import json
import os
import re
from cryptography.hazmat.backends import default_backend
from cryptography.hazmat.primitives import padding
from cryptography.hazmat.primitives.ciphers import Cipher, algorithms, modes
from flask import Flask, jsonify, request
from llama_cpp import Llama

app = Flask(__name__)

try:
    with open("config-server.json", "r") as f:
        config = json.load(f)

    REPO_ID = config.get("repo_id")
    FILENAME = config.get("filename")
    N_CTX = config.get("n_ctx", 8192)
    AES_KEY = config.get("key")
    HF_HOME = config.get("hf_home")

    if not all([REPO_ID, FILENAME, AES_KEY, HF_HOME]):
        raise ValueError(
            "Required key missing in config-server.json (repo_id, filename, key, hf_home)"
        )

    # Set the Hugging Face cache directory
    os.environ['HF_HOME'] = HF_HOME

except FileNotFoundError:
    raise RuntimeError("Configuration file 'config-server.json' not found.")
except Exception as e:
    print(f"Error loading configuration: {e}")
    exit(1)


# --- Llama.cpp Model Initialization ---
print(f"Loading model: {FILENAME} from {REPO_ID}...")
try:
    llm = Llama.from_pretrained(
        repo_id=REPO_ID,
        filename=FILENAME,
        verbose=False,
        n_ctx=N_CTX,
        n_gpu_layers=-1,  # Offload all layers to GPU if available
    )
    print("Model loaded successfully.")
except Exception as e:
    print(f"Failed to load GGUF model: {e}")
    exit(1)


@app.route("/green", methods=["POST"])
def green_phase():
    try:
        data = request.get_json()
        base64_param = data.get("base64")

        if not base64_param:
            return jsonify({"error": "Missing 'base64' parameter"}), 400

        # --- Decryption ---
        key = AES_KEY.encode("utf-8")
        cipher = Cipher(algorithms.AES(key), modes.ECB(), backend=default_backend())
        decryptor = cipher.decryptor()

        decoded_data = base64.b64decode(base64_param)
        decrypted_data = decryptor.update(decoded_data) + decryptor.finalize()

        unpadder = padding.PKCS7(128).unpadder()
        unpadded_data = unpadder.update(decrypted_data) + unpadder.finalize()

        # --- Prompt Processing ---
        prompts = json.loads(unpadded_data.decode("utf-8"))
        system_prompt = prompts.get("system")
        user_prompt = prompts.get("user")

        if not system_prompt or not user_prompt:
            return jsonify({"error": "Missing 'system' or 'user' prompt"}), 400

        # --- Code Generation ---
        response = llm.create_chat_completion(
            messages=[
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": user_prompt},
            ],
            temperature=0.1,
            top_p=0.95,
        )

        raw_content = response["choices"][0]["message"]["content"]

        # --- Code Extraction ---
        match = re.search(r"```(?:python)?([\s\S]*?)```", raw_content)
        if match:
            code = match.group(1).strip()
        else:
            code = raw_content.strip()

        print("--- Generated Code ---")
        print(code)
        print("----------------------")

        if not code:
            return jsonify({"error": "Model returned an empty response"}), 500

        # --- Encryption ---
        padder = padding.PKCS7(128).padder()
        data_padded = padder.update(code.encode("utf-8")) + padder.finalize()
        encryptor = cipher.encryptor()
        encrypted_data = encryptor.update(data_padded) + encryptor.finalize()
        datasf = base64.b64encode(encrypted_data).decode("utf-8")

        return jsonify({"datasf": datasf}), 200

    except Exception as e:
        print(f"An error occurred: {e}")
        return jsonify({"error": str(e)}), 500


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000, debug=False)