# Echo: Podcast Search Engine

* [echo-actors](echo-actors/)
    : The Scala backend-implementation of Echo, based on a distributed actors architecture using Akka
* [echo-microservices](echo-microservices/)
    : The Java backend-implementation of Echo, based on a microservice architecture
* [echo-core](echo-core/)
    : The library for core Echo tasks (feed-parsing, tokenization, etc), used by echo-actors and echo-microservices
* [echo-frontend](echo-frontend/)
    : The web-frontend for the Echo, used by both echo-actors and echo-microservices
