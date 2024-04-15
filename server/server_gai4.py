from typing import Optional

import fire

from llama import Llama
from flask import Flask, request, jsonify
import subprocess
import base64
from cryptography.hazmat.primitives.ciphers import Cipher, algorithms, modes
from cryptography.hazmat.primitives import padding
from cryptography.hazmat.backends import default_backend
import secrets
import sys
import re

app = Flask (__name__)
ckpt_dir = "/media/lab/L/llama2repo/codellama/CodeLlama-7b-Instruct/"
tokenizer_path = "/media/lab/L/llama2repo/codellama/CodeLlama-7b-Instruct/tokenizer.model"
prompt_file = "/media/lab/L/llama2repo/codellama/GAI4TDD.py"

#AES_KEY = secrets.token_urlsafe(12)
AES_KEY = "lolkeylongenough"
print(f"Key: {AES_KEY}")

generator = Llama.build(
        ckpt_dir=ckpt_dir,
        tokenizer_path=tokenizer_path,
        max_seq_len=5120,
        max_batch_size=1,
    )


@app.route("/green", methods = ['POST'])
def green_phase():
    try:
        data = request.get_json()
        base64_param = data['base64']
        aes_key = AES_KEY
        #if aes_key is None or aes_key != AES_KEY:
            #return jsonify({'error':'Key is missing'}), 500
        #command = f"python3 -m torch.distributed.run --nproc_per_node 1 {prompt_file} " \
        #                  f"--ckpt_dir {ckpt_dir} --aes {aes_key} --datasf {base64_param} --tokenizer_path {tokenizer_path} " \
        #                  f"--max_seq_len 4096 --max_batch_size 1"
        #print(command)

        if base64_param is None or base64_param == "":
            return jsonify({'error':'ERRORE'}), 500 
        decoded_data = base64.b64decode(base64_param)

        key = aes_key.encode('utf-8')
        cipher = Cipher(algorithms.AES(key), modes.ECB(), backend=default_backend())
        decryptor = cipher.decryptor()
       
        decoded_data = base64.b64decode(base64_param)
        decrypted_data = decryptor.update(decoded_data) + decryptor.finalize()
        unpadder = padding.PKCS7(128).unpadder()
        unpadded_data = unpadder.update(decrypted_data) + unpadder.finalize()
        instructions = [
            [
                {
                    "role": "system",
                    "content": "You will be provided with a piece of Python code, and your task is to find and fix bugs to pass the test suite. Return the full class with all import and fixs. You can't modify test cases but you will get the error message of last test case. You will write the bare minimum code to pass the test. Reply only with the full class\n\n",
                },
                {
                    "role": "user",
                    "content": unpadded_data.decode('utf-8') + ""
                }
            ],
        ]
        #value = result.stdout
        results = generator.chat_completion(
            instructions,  # type: ignore
            max_gen_len=None,
            temperature=0.1,
            top_p=0.95,
        )
        value = ""
        for result in results:
            value = result['generation']['content']
        match = re.search(r'```+(python)?([\s\S]*?)```+', value)
        if match:
            code = match.group(2)
        else:
            code = value
        print(code)
            
        if code == "ERRORE":
            return jsonify({'error':'ERRORE'}), 500 
        else:
           padder = padding.PKCS7(128).padder()
           data_padded = padder.update(code.encode('utf-8'))+ padder.finalize()
           key = AES_KEY.encode('utf-8')
           cipher = Cipher(algorithms.AES(key), modes.ECB(), backend=default_backend())
           encryptor = cipher.encryptor()
           encrypted_data = encryptor.update(data_padded) + encryptor.finalize()    
           datasf = base64.b64encode(encrypted_data).decode('utf-8')
           return jsonify({"datasf": datasf}), 200
           #print(datasf.decode('utf-8'))
        
        #print(code)
    except Exception as e:
        return jsonify({'error':str(e)}), 500
        
        
        
if __name__ == '__main__':
    app.run(host='0.0.0.0',debug=False)

#datasf = "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"
#aes_key = "lolkeylongenough"






#if result.stderr:
#    print("error:", result.stderr, file=sys.stderr)
