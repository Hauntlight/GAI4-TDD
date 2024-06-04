import unittest
import os
import sys
import argparse
from openai import OpenAI


import re


def run_tests(test_file_path,project):
    # Aggiungi il percorso del file al sys.path
    file_directory = os.path.dirname(test_file_path)
    sys.path.append(file_directory)
    proj_directory = os.path.dirname(project)
    sys.path.append(proj_directory)

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
    parser.add_argument('-c', '--clas', type=str, help='Class to complete',required=False,nargs='?',const=r'')
    parser.add_argument('-t', '--txt', type=str, help='Class to complete',required=False,nargs='?',const=r'')
    parser.add_argument('-tc', '--testclass', type=str, help='Test Class',required=True,nargs='?',const=r'')
    parser.add_argument('-k', '--key', type=str, help='API key for GPT',required=True)
    parser.add_argument('-p','--project',type=str, help='Project dir',required=True,nargs='?',const=r'')
    parser.add_argument('-m', '--model', type=str,help='Model to use',required=True)
    args = parser.parse_args()

    # Esegui i test


    testClassText = ""
    classText = ""
    client = OpenAI(api_key=args.key)
    pathTestClass = args.testclass.replace('"','')

    if args.clas:
        pathClass = args.clas.replace('"','')
        f = open(pathClass, "r")

        classText = f.read()

        f.close()
    else:
        classText = args.txt

    model = args.model
    '''
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
'''
    #print("MESSAGGIO" + ultima_riga)

    f = open(pathTestClass, "r")

    testClassText = f.read()

    f.close()

    userText = r"Fix the hint based on last test case\n\n\nThe test class:\n\n" + testClassText + "\n\n\nThe hint is:\n\n" + classText + "\n\n"
    systemText = r"You will be provided with a piece of Python code, and your task is to find and fix bugs to pass the last test case, writing the bare minimum code.\n\n"

    # print(r'{"messages": [{"role": "system", "content": "'+systemText+'"}, {"role": "user", "content": "'+userText.replace('\n', '\\n').replace('\t','\\t')+'"}, {"role": "assistant", "content": "'+rightClassText.replace('\n', '\\n').replace('\t','\\t')+'"}]}')

    #print("Request sent")
    #print(
    #    "Complete the hint to pass all tests\n\n\nThe test class to pass is:\n\n" + testClassText + "\n\n\nThe hint is:\n\n" + classText + "")
    response = client.chat.completions.create(model=model,
    messages=[
        {
            "role": "system",
            "content": "You will be provided with a test suite class and a class under test in python. Your task is to remove from the class under test the code that is not tested, but ONLY in methods called in the test suite. Your answer should be only code.\n\nThe procedure you should follow is this one:\nFor each test in test suite check the function of the class under test that is called\nNow check if the tests in the test suite check all the possible cases of the function\nIf not, fix the method under test, to be fully covered by the test cases"
        },
        {
            "role": "user",
            "content": "The test suite:\n\n" + testClassText + "\n\n\nClass under test:\n\n" + classText + "\n\n"
        }
    ],
    temperature=0.1,
    max_tokens=2500,
    top_p=1,
    frequency_penalty=0,
    presence_penalty=0)
    #print("Gotcha! Writing time!")
    #print(response)
    code = response.choices[0].message.content
    match = re.search(r'```+(python)?([\s\S]*?)```+', code)
    if match:
        result = match.group(2)
    else:
        result = code
    print(result)






    #f = open(pathClass, "w")
    #f.write(result)
    #f.close()
    #print("Work Done")

    #print("Show time")

    # Stampa il risultato dei test
    ''' print("Risultato dei test:")
    print(f"Test passati: {test_result.wasSuccessful()}")
    print(f"Errori: {len(test_result.errors)}")
    print(f"Failure: {len(test_result.failures)}")'''
