import unittest
import os
import sys
import argparse
from cryptography.hazmat.primitives.ciphers import Cipher, algorithms, modes
from cryptography.hazmat.primitives import padding
from cryptography.hazmat.backends import default_backend
import base64
import secrets
import requests
import json



import re


def run_tests(test_file_path,project):
    # Aggiungi il percorso del file al sys.path
    file_directory = os.path.dirname(test_file_path)
    sys.path.append(file_directory)
    proj_directory = os.path.dirname(project)
    sys.path.append(proj_directory)
    #print(sys.path)
    # Ottieni il nome del file senza estensione
    file_name = os.path.splitext(os.path.basename(test_file_path))[0]

    try:
        # Carica la suite di test dal modulo specificato
        loader = unittest.TestLoader()
        suite = loader.loadTestsFromName(file_name)

        # Esegui i test
        runner = unittest.TextTestRunner(verbosity=0)
        result = runner.run(suite)

        # Restituisci il risultato
        return result
    finally:
        # Rimuovi il percorso del file da sys.path indipendentemente dall'esito dei test
        sys.path.remove(proj_directory)
        sys.path.remove(file_directory)



if __name__ == "__main__":
    # Sostituisci il percorso del file con il percorso assoluto del tuo file di test
    parser = argparse.ArgumentParser(description='GPT TDD Assistant Script')

    # Aggiungi i tre parametri in input (-p1, -p2, -p3)
    parser.add_argument('-c', '--clas', type=str, help='Class to complete',required=True,nargs='?',const=r'')
    parser.add_argument('-tc', '--testclass', type=str, help='Test Class',required=True,nargs='?',const=r'')
    parser.add_argument('-k', '--key', type=str, help='AES key of the server',required=True)
    parser.add_argument('-s','--server',type=str, help='Server ip and port',required=True,nargs='?',const=r'')
    parser.add_argument('-p','--project',type=str, help='Project dir',required=True,nargs='?',const=r'')
    args = parser.parse_args()

    # Esegui i test


    testClassText = ""
    classText = ""
    serverKey = args.key
    server = args.server
    pathTestClass = args.testclass.replace('"','')
    pathClass = args.clas.replace('"','')
    testResult = run_tests(pathTestClass,args.project.replace('"',''))
    if len(testResult.errors) > 0:
        last_error = testResult.errors[-1]
        error_message = last_error[1]
        lines = error_message.splitlines()
        ultima_riga = lines[-1]

        #print("Ultimo messaggio di errore:", ultima_riga)

    elif len(testResult.failures) > 0:
        last_fail = testResult.failures[-1]
        error_message = last_fail[1]
        lines = error_message.splitlines()
        ultima_riga = lines[-1]
    else:
        ultima_riga = ""

    #print("MESSAGGIO" + ultima_riga)

    f = open(pathTestClass, "r")

    testClassText = f.read()

    f.close()

    f = open(pathClass, "r")

    classText = f.read()

    f.close()
    #userText = r"Fix the hint based on last test case\n\n\nThe test class:\n\n" + testClassText + "\n\n\nThe hint is:\n\n" + classText + "\n\n"

    #systemText = r"You will be provided with a piece of Python code, and your task is to find and fix bugs to pass the last test case, writing the bare minimum code.\n\n"

    # print(r'{"messages": [{"role": "system", "content": "'+systemText+'"}, {"role": "user", "content": "'+userText.replace('\n', '\\n').replace('\t','\\t')+'"}, {"role": "assistant", "content": "'+rightClassText.replace('\n', '\\n').replace('\t','\\t')+'"}]}')

    #print("Request sent")
    #print(
    #    "Complete the hint to pass all tests\n\n\nThe test class to pass is:\n\n" + testClassText + "\n\n\nThe hint is:\n\n" + classText + "")
    #response = client.chat.completions.create(model="gpt-4",
    #messages=[
    #{
    #   "role": "system",
    #   "content": "You will be provided with a piece of Python code, and your task is to find and fix bugs to pass the last test case, return the full class. You can't modify test cases but you will get the error message of last test case. You will write the bare minimum code to pass the test.\n\n"
    #},
    #{
    #   "role": "user",
    plaintext = "Write the bare minimum code into the hint to pass the test suite.\n\n\nThe test suite:\n\n" + testClassText + "\n\n\nThe hint is:\n\n" + classText + "\n\n" + "The error message is:\n\n" + ultima_riga
    #print(plaintext)
    padder = padding.PKCS7(128).padder()
    data_padded = padder.update(plaintext.encode('utf-8'))+ padder.finalize()
    cipher = Cipher(algorithms.AES(serverKey.encode('utf-8')), modes.ECB(), backend=default_backend())
    encryptor = cipher.encryptor()
    encrypted_data = encryptor.update(data_padded) + encryptor.finalize()
    base64_data = base64.b64encode(encrypted_data).decode('utf-8')
    url = "http://"+server+"/green"
    data = {
        "base64": base64_data
    }
    response = requests.post(url,json=data)
    if response.status_code == 200:
        response_data = response.json()
        try:
            datasf = response_data['datasf']
        except Exception as e:
            print("Error")
        decoded_data = base64.b64decode(datasf)
        key = serverKey.encode('utf-8')
        cipher = Cipher(algorithms.AES(key), modes.ECB(), backend=default_backend())
        decryptor = cipher.decryptor()
        decoded_data = base64.b64decode(datasf)
        decrypted_data = decryptor.update(decoded_data) + decryptor.finalize()
        unpadder = padding.PKCS7(128).unpadder()
        unpadded_data = unpadder.update(decrypted_data) + unpadder.finalize()
        result = unpadded_data.decode('utf-8')
    #    }
    #],
    #temperature=0.1,
    #max_tokens=2500,
    #top_p=1,
    #frequency_penalty=0,
    #presence_penalty=0)
    #print("Gotcha! Writing time!")
    #print(response)
    #code = response.choices[0].message.content
    #match = re.search(r'```+(python)?([\s\S]*?)```+', code)
    #if match:
    #    result = match.group(2)
    #else:
    #   result = code
    print(result)
    #f = open(pathClass, "w")
    #f.write(result)
    #f.close()
    #print("Work Done")

    #print("Show time")

    # Stampa il risultato dei test
    '''
    print("Risultato dei test:")
    print(f"Test passati: {test_result.wasSuccessful()}")
    print(f"Errori: {len(test_result.errors)}")
    print(f"Failure: {len(test_result.failures)}")'''
