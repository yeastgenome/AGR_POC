## How to convert *.txt to JSON

1. Make sure you're running this from */js/App directory or cd to that in your terminal
2. Add .txt file you would like to convert in /input folder
3. Modify custom command in package.json to point to your input file and output file
4. Run "npm run custom" command in your terminal
5. You should have json file after the command assuming no error
6. If you're using vscode, you can prettify the output.json file otherwise you can format online if wish to format the file

## How to format disease.json
1. Make sure you're in Scripts/python directory
2. Add the disease.json file to the directory or just replace content of output.json with your disease.json
3. Script runs with python 2.7, so maybe try running in a virtualenv that points to python 2.7 
4. Run "python index.py"
5. Read the script for next steps, still working on the script but it's able to format the disease.json file only that now its printing to console instead of writing to file
