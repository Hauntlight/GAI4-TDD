import unittest
import sys
import os
import json
import traceback

# Increase recursion limit as per original code
sys.setrecursionlimit(2000)

def run_tests(test_file_path, project_path):
    # Setup paths
    file_directory = os.path.dirname(test_file_path)
    if project_path not in sys.path:
        sys.path.append(project_path)
    if file_directory not in sys.path:
        sys.path.append(file_directory)

    file_name = os.path.splitext(os.path.basename(test_file_path))[0]

    result_data = {
        "success": False,
        "error_message": "",
        "output": ""
    }

    try:
        loader = unittest.TestLoader()
        suite = loader.loadTestsFromName(file_name)

        # Capture stdout/stderr to avoid pollution
        from io import StringIO
        stream = StringIO()
        runner = unittest.TextTestRunner(stream=stream, verbosity=2)
        result = runner.run(suite)

        result_data["output"] = stream.getvalue()

        if result.wasSuccessful():
            result_data["success"] = True
            result_data["error_message"] = "TestsuiteOK"
        else:
            # Extract the last error
            error_msg = ""
            if result.errors:
                error_msg = result.errors[-1][1]
            elif result.failures:
                error_msg = result.failures[-1][1]

            # Get last line of the error
            lines = error_msg.strip().splitlines()
            result_data["error_message"] = lines[-1] if lines else "Unknown Error"
            result_data["full_trace"] = error_msg

    except Exception as e:
        result_data["success"] = False
        result_data["error_message"] = str(e)
        result_data["full_trace"] = traceback.format_exc()

    return result_data

if __name__ == "__main__":
    # Arguments: 1=TestFilePath, 2=ProjectPath
    if len(sys.argv) < 3:
        print(json.dumps({"success": False, "error_message": "Invalid Arguments"}))
        sys.exit(1)

    test_path = sys.argv[1]
    proj_path = sys.argv[2]

    results = run_tests(test_path, proj_path)
    print(json.dumps(results))