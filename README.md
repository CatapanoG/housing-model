[![Build Status](https://travis-ci.org/EconomicSL/housing-model.svg?branch=master)](https://travis-ci.org/EconomicSL/housing-model)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/a740a85350b54e49b49dd84157f30fac)](https://www.codacy.com/app/EconomicSL/housing-model?utm_source=github.com&utm_medium=referral&utm_content=EconomicSL/housing-model&utm_campaign=badger)

Agent-Based Model of the UK Housing Market
==========================================

This is an agent-based model of the UK housing market written by the Institute of New Economic Thinking at the
University of Oxford, in collaboration with the Bank of England. It is intended for use as a tool for informing central
bank regulation policy.

The model incorporates owner-occupiers, renters, buy-to-let investors, a housing market, a rental market, banks, a
central bank and a government. A more detailed description of the model can be found at this
[Bank of England Working Paper](http://www.bankofengland.co.uk/research/Pages/workingpapers/2016/swp619.aspx) and at the
ModelDescriptionFeb16.pdf file.

# Requirements
- `JDK` (tested with java 1.8)
- `Apache Maven` (tested with Maven 3.6.0)
- `Python 3.6` (when running in mixed mode)

On Ubuntu **19.04** you can install them with:
```
sudo apt install -y openjdk-8-jdk-headless maven
```

For installing python3.6, please refer to your distribution documentation, or use `pyenv`.

# Runtime modes
The project can be run in two modes:
1. as a **standalone Java** program. This is the original execution mode from the upstream project.
2. in **mixed python-java** mode

## Building & running as Java standalone (on linux)
**Build** the project:
```bash
cd <project_directory>
mvn package
```
This will create `<project_directory>/target/housing-model-1.0-SNAPSHOT-jar-with-dependencies.jar`.

**Run** the Housing model from command line:

```bash
cd <project_directory>
java -cp target/housing-model-1.0-SNAPSHOT-jar-with-dependencies.jar housing.Model
```

The configuration parameters will be read from the configuration file.

## Run the housing model in mixed Python-Java mode (linux)

Build the Java project just like the standalone case (`mvn package`).

**Create the python virtualenv**:
```
./recreate-virtualenv.sh
```

Now **open two terminal windows**:

1. In terminal 1, start the java server:
   ```bash
   ./start-java-server.sh
   ```

2. In terminal 2, run the python program:
   ```bash
   venv/bin/python call-headless-model.py
   ```

The python program will invoke the Housing Model on the Java side, passing
custom parameters to Model.exec() and getting back the results.
