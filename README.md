# json-value-mapping


## Introduction:

IN microservice application, the application will be split into a set of smaller, interconnected services instead of building a single monolithic application.
Each microservice is a small application that has its own hexagonal architecture consisting of business logic along with various adapters.
So there will be lots service to service call to get the required data for business method.

In normal scenario, the restful API use json format for response.  In case system has lots of downstream call, then tons of fields are to be extracted from various json responses.
If the conventional approach is used it is required to create lots of Java beans to hold the responses and then have mappers to extract relevant data from those.


But with this intuitive library it is possible to extract relevant model data without having tons of Java beans just need the json string or an object.

## library using:

https://github.com/google/gson