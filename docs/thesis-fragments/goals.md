
# Goal of the thesis



# Design Goal of the implementation

- Clear separation between search engine and domain specific tasks (writing to index and search, fetching and parsing feeds, etc) and the communication and coordiantion specific parts (actors architecture and microservice architecture). Therefore a core library was developed for the first, and for the second "only" backends utilizing this lib was needed. It allows easy analysis/argumentation about the concurrent programming vs MSA aspect of the thesis, because all test scenario specific parts are part of core-lib, and could therefore be replaced by other domain specific code or non-search engine code

Important that core-lib does not introduce any own concurrent aspect, all concurrency should be done via actors/microservices

* core-lib is for demonstration
* actor-backend and microservice-backend are for scientific work