import unittest
import os
import sys
import argparse
from openai import OpenAI


import re

        

if __name__ == "__main__":
    # Sostituisci il percorso del file con il percorso assoluto del tuo file di test
    parser = argparse.ArgumentParser(description='GPT TDD Assistant Script')

    # Aggiungi i tre parametri in input (-p1, -p2, -p3)
    parser.add_argument('-c', '--clas', type=str, help='Class to complete',required=True,nargs='?',const=r'')
    parser.add_argument('-tc', '--testclass', type=str, help='Test Class',required=True,nargs='?',const=r'')
    parser.add_argument('-k', '--key', type=str, help='API key for GPT',required=True)
    parser.add_argument('-p','--project',type=str, help='Project dir',required=True,nargs='?',const=r'')
    parser.add_argument('-m', '--model', type=str,help='Model to use',required=True)
    args = parser.parse_args()

    # Esegui i test


    testClassText = ""
    classText = ""
    client = OpenAI(api_key=args.key)
    #pathTestClass = args.testclass.replace('"','')
    pathClass = args.clas.replace('"','')
    #testResult = run_tests(pathTestClass,args.project.replace('"',''))
    model = args.model
    '''
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

    #f = open(pathTestClass, "r")

    #testClassText = f.read()

    #f.close()

    f = open(pathClass, "r")

    classText = f.read()

    f.close()
    #userText = r"Fix the hint based on last test case\n\n\nThe test class:\n\n" + testClassText + "\n\n\nThe hint is:\n\n" + classText + "\n\n"
    #systemText = r"You will be provided with a piece of Python code, and your task is to find and fix bugs to pass the last test case, writing the bare minimum code.\n\n"

    # print(r'{"messages": [{"role": "system", "content": "'+systemText+'"}, {"role": "user", "content": "'+userText.replace('\n', '\\n').replace('\t','\\t')+'"}, {"role": "assistant", "content": "'+rightClassText.replace('\n', '\\n').replace('\t','\\t')+'"}]}')

    #print("Request sent")
    #print(
    #    "Complete the hint to pass all tests\n\n\nThe test class to pass is:\n\n" + testClassText + "\n\n\nThe hint is:\n\n" + classText + "")
    response = client.chat.completions.create(model=model,
    messages=[
        {
            "role": "system",
            "content": "You will be given Python code and your job is to improve the quality of the code and refactor it. Return the complete code without explanation, but add comments where necessary. Do not change the code logic"
        },
        {
            "role": "user",
            "content": classText
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
