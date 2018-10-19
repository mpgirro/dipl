# Concurrent Programming with Actors and Microservices


Master Thesis, Maximilian Irro, September 2018, TU Wien.


## TOC


* [Repository Structure](#repository-structure)
* [Thesis Document](#thesis-document)
    * [Abstract (EN)](#abstract-en)
    * [Kurzfassung (DE)](#kurzfassung-de)
* [Implementations](#implementations)
    * [Core Library](#core-library)
    * [Actor Architecture](#actor-architecture)
    * [Microservice Architecture](#microservice-architecture)
    * [Web UI](#web-ui)
* [Evaluation](#evaluation)


## Repository Structure


This repository is structured as follows:

* :open_file_folder: [/docs/](docs/) &ndash; document source files (thesis, poster, slides, graphics, etc.)
* :open_file_folder: [/eval/](eval/) &ndash; evaluation material (measurement data, R scripts)
* :open_file_folder: [/src/](src/) &ndash; program artifact source code (actor architecture, microservice architecture, domain library, web interface) for the Echo search engine implementations

The subdirectories have separate `README.md` files with detailed information.


## Thesis Document


This master project originated a thesis document. It is available in the following file versions:

* :scroll: [PDF](https://max.irro.at/pub/dipl/thesis.pdf) (optimized for printing)
* :page_facing_up: [HTML](https://max.irro.at/pub/dipl/thesis.html) (optimized for screens)

If you want to cite the original thesis, please use the following [BibTeX entry](https://max.irro.at/pub/dipl/thesis.bib):

    @mastersthesis{Irro18,
        author    = {Maximilian Irro},
        title     = {{Concurrent Programming with Actors and Microservices}},
        school    = {Compilers and Languages Group, TU Wien},
        type      = {Master Thesis},
        year      = {2018},
        month     = {September},
        keywords  = {concurrent programming, actor model, microservice architecture},
        timestamp = {20180930},
        url       = {https://max.irro.at/pub/dipl/},
        pdf       = {https://max.irro.at/pub/dipl/thesis.pdf}
    }

For a lists errors found in the submitted version of the thesis, see the [errata](/docs/errata.md) file. 


### Abstract (EN)


Common problems require applications to manage multiple concerns simultaneously. A convenient approach is the concept of *concurrent programming*. In this thesis, we investigate two different models for introducing concurrent computational units into software architectures. One approach is the *actor model* that defines theoretically well-known constructs supporting concurrent, parallel and distributed execution in a transparent way. The other approach is an architectural style based on *microservices*, a recent trend that gained academic and industrial popularity. Microservices facilitate many principles of the old Unix philosophy by composing complex functionality through small, independent, highly cohesive and loosely coupled executables. These programs interoperate via lightweight, technology-heterogeneous messaging channels. The deployment modality of microservices conceives concurrent execution through the operating system scheduler. This thesis compares the programming of concurrent computation through actors and microservices with respect to a non-trivial concurrent system scenario. We argue that both approaches share many conceptual similarities and show few but significant differences. Both models have the same expressive capabilities regarding concurrent programming concerns like communication and scalability, but are subject to different trade-offs. We provide implementations of the system scenario based on actor and microservice architectures. Benchmark results of these implementations suggest that actors provide better system efficiency through a smaller codebase. Microservice architectures consume significantly more system resources and suffer especially from purely synchronous communication mechanisms.


### Kurzfassung (DE)


Applikationen benötigen häufig eine simultane Bearbeitung mehrerer Aufgaben. *Nebenläufige Programmierung* ist hierfür ein verbreitetes Konzept. Diese Arbeit beschäftigt sich mit zwei Modellen zur Definition nebenläufiger Programmeinheiten innerhalb von Softwarearchitekturen. Eines dieser Modelle ist das *Actor Model*. Es definiert theoretisch wohlfundierte Konstrukte, welche transparent eine nebenläufige, parallele und verteilte Ausführung ermöglichen. Bei dem anderen Modell handelt es sich um einen relativ neuen Architekturstil unter Verwendung von *Microservices*, welche sich kürzlich im akademischen und industriellen Umfeld großer Beliebtheit erfreuen. Microservices bauen auf viele Prinzipien der alten Unix-Philosophie, indem sie komplexe Funktionalität durch den Zusammenschluss kleiner, unabhängiger, kohäsiver und lose gekoppelter Programme konzipieren. Diese Programme interagieren über leichtgewichtige, auf Nachrichtenaustausch basierende, technologisch heterogene Kommunikationskanäle. Microservices unterliegen einer implizit nebenläufigen Ausführungsmodalität durch den Prozess-Scheduler des Betriebssystems. Diese Arbeit vergleicht die Programmierung von nebenläufiger Ausführung mittels Actors und Microservices relativ zu einem nichttrivialen Fallbeispiel eines nebenläufigen Systems. Wir argumentieren, dass beide Ansätze viele Gemeinsamkeiten und wenige aber wichtige konzeptionelle Unterschiede besitzen. Beide Modelle haben gleichwertige Möglichkeiten um typische Anliegen der nebenläufigen Programmierung wie Kommunikation und Skalierbarkeit auszudrücken. Jedoch unterliegen die Modelle unterschiedlichen Trade-offs. Wir stellen Implementierungen des Fallbeispiels bereit, welche jeweils auf Actors bzw. Microservices basieren. Die Resultate eines Benchmarkings dieser Implementierungen legen nahe, dass Actors eine bessere Systemeffizienz verbunden mit einer kleineren Codebasis ermöglichen. Microservice-Architekturen hingegen konsumieren erheblich mehr Systemresourcen und leiden vor allem unter den Auswirkungen rein synchroner Kommunikationsmechanismen.


## Implementations


This thesis uses a non-trivial system scenario. The scenario is a podcast search engine called *Echo*. Several implementation artifacts for Echo were produced in the course of this project. The four main parts are:

### Core Library


:open_file_folder: [/src/echo-core/](src/echo-core/)

A core library for domain-specific functionality.


### Actor Architecture


:open_file_folder: [/src/echo-actors/](src/echo-actors/) 

An implementation of Echo's backend based on an architecture that uses the [actor model](https://en.wikipedia.org/wiki/Actor_model). This system implements the whole search engine within a single executable artifact (monolith). The programming language is Scala. The actor library is [Akka](https://akka.io).


### Microservice Architecture


:open_file_folder: [/src/echo-microservices/](src/echo-microservices/)

An implementation of Echo's backend based on a [microservice architecture](https://en.wikipedia.org/wiki/Microservices). This system is composed of several executable artifacts (the microservices). These distinct programs communicate via network mechanism, either [REST](https://en.wikipedia.org/wiki/Representational_state_transfer) or [RabbitMQ](https://www.rabbitmq.com) (an [AMQP](https://en.wikipedia.org/wiki/Advanced_Message_Queuing_Protocol)-compatible messaging system). All microservices build on [Spring Boot](https://spring.io/projects/spring-boot) as the application foundation and various components of [Spring Cloud](http://projects.spring.io/spring-cloud/).

The microservice applications are:

* [app-catalog](src/echo-microservices/app-catalog/)
* [app-cli](src/echo-microservices/app-cli/)
* [app-crawler](src/echo-microservices/app-crawler/)
* [app-gateway](src/echo-microservices/app-gateway/)
* [app-index](src/echo-microservices/app-index/)
* [app-parser](src/echo-microservices/app-parser/)
* [app-registry](src/echo-microservices/app-registry/)
* [app-searcher](src/echo-microservices/app-searcher/)
* [app-updater](src/echo-microservices/app-updater/)


### Web UI


:open_file_folder: [/src/echo-web/](src/echo-web/)

The web-frontend for the Echo search engine. This web app builds on [Angular](https://angular.io).


## Evaluation


__TODO__