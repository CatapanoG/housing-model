#!/usr/bin/env python3.6

from py4j.java_gateway import JavaGateway

# connect to the JVM
gateway = JavaGateway()

# get the HeadlessModel instance
headlessModel = gateway.entry_point

# test: call the greetMe() method
greeting = headlessModel.greetMe()
print(greeting)

# test: call the static method printGreet(). It will output in the java server
# window.
headlessModel.printGreet()

# call the static method exec(), which runs the simulation
gateway.jvm.housing.Model.exec()
