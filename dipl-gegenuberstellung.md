# SOA vs Microservices

| services focus on technical integration issues | services focus on implementing clear business capabilities | Bro16 |
| ---------------------------------------- | :--------------------------------------- | :---: |
| very fine grained technical APIs         | larger grained APIs                      | Bro16 |
| deployment: monolith, deployed multiple times | deployment: serveral small applications implementing part of the whole | Bro16 |
| explosion of standards and options for implementation | strictly limit types of network connectivity that a service can implement to achieve maximum simplicity | Bro16 |
|                                          | more choices via polyglot programming / polyglot persistence | Bro16 |
|                                          | avoid thight coupling via implicit communication through databas | Bro16 |
|                                          | all communication from service to service must be through the service API | Bro16 |
| monolith: different aspects of the system were forced to all be released at the speed of the slowest- moving part of the system | per service CI/CD: different services evolve at different rates; allows that evolution to proceed at is own natural pace | Bro16 |
| scaling all the services in the monolith at the same level often led to overutilization of some servers and underutilization of others – or even worse, starvation of some services by others when they monopolized all of the available shared resources such as thread pools | horizontal scaling of single services    | Bro16 |
|                                          |                                          |       |

# Fault tolerance (MS vs Distr Objects/etc)

|      | physical communication media fault | Spe90 |
| ---- | ---------------------------------- | ----- |
|      | partner termination                |       |
|      | channel disconnection              |       |
|      |                                    |       |
|      |                                    |       |

# Consistency check

|      | NERECO: consistency analysis among the declarations of processes | Spe90 |
| ---- | ---------------------------------------- | ----- |
|      |                                          |       |
|      |                                          |       |
|      |                                          |       |
|      |                                          |       |

# Concurrency Abstractions (MS vs Andere)

|      | UNIX processes communicating by lower level inter-process communication | Spe90 |
| ---- | ---------------------------------------- | ----- |
|      |                                          |       |
|      |                                          |       |
|      |                                          |       |
|      |                                          |       |

Concurrent and Disrtributed Programming Mechanism

* Rechts: Mechanismus die irgendwelche Sprachen/modelle/sonst was anbieten


* Links: Wie das in MS ausschaut

  ​

  |      | Location transparency | Spe90 |
  | ---- | --------------------- | ----- |
  |      | Fault handling        | Spe90 |
  |      | Modularity            | Spe90 |
  |      | Reliability           | Spe90 |
  |      |                       |       |

   