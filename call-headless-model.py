#!/usr/bin/env python3

import numpy as np
from py4j.java_gateway import JavaGateway

# connect to the JVM
gateway = JavaGateway()

# get the HeadlessModel instance
headlessModel = gateway.entry_point

# call the static method exec(), which runs the simulation
result = gateway.jvm.housing.Model.exec(1.5)

# result is a py4j.java_collections.JavaArray, whose elements are floats
print(type(result))

for x in result:
    print(x)
