# Echo: Podcast Search Engine

* [echo-actors](echo-actors/)
    : The Scala backed-implementation of Echo, based on a distributed actors architecture using Akka
* [echo-microservices](echo-microservices/)
    : The Java backed-implementation of Echo, based on a microservice architecture
* [echo-common](echo-common/)
    : The library for common Echo tasks (feed-parsing, tokenization, etc), used by echo-actors and echo-microservices
* [echo-frontend](echo-frontend/)
    : The Angular frontend for the Echo, used by both echo-actors and echo-microservices