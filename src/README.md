# Echo: Podcast Search Engine

* [echo-actors](echo-actors/)
    : The Scala implementation of the Echo engine, based on a distributed actors architecture using Akka
* [echo-microservices](echo-microservices/)
    : The Java implementation of the Echo engine, based on a microservice architecture
* [echo-core](echo-core/)
    : The library for core Echo tasks (feed-parsing, searching, etc), used by echo-actors and echo-microservices
* [echo-web](echo-web/)
    : The web-frontend for the Echo, used by both [echo-actors](echo-actors/) and [echo-microservices](echo-microservices/)
