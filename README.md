# Concurrent Programming with Actors and Microservices

Comming soon(ish).

![](https://media2.giphy.com/media/kFgzrTt798d2w/giphy.gif)

* [Repository Structure](#repository-structure)
* [Thesis Document](#thesis-document)
    * [Abstract (EN)](#abstract-en)
    * [Abstract (DE)](#abstract-de)

## Repository Structure

This repository is structured as follows:

* [/docs](docs/) &ndash; documents (thesis, poster, slides, graphics, etc.)
* [/eval](eval/) &ndash; evaluation material (measurement data, R scripts)
* [/src](src/) &ndash; program artifact source code (actor architecture, microservice architecture, domain library, web interface) for the Echo search engine implementations

The subdirectories have separate `README.md` files with detailed information.

## Thesis Document

This master project originated a thesis document. It is available as in [PDF]((https://max.irro.at/pub/dipl/thesis.pdf)) (optimized for printing) and [HTML]((https://max.irro.at/pub/dipl/thesis.html)) (optimized for screens) versions.

If you want to cite the original thesis, please use the following [BibTeX entry](https://max.irro.at/pub/dipl/thesis.bib):

```bibtex
@mastersthesis{Irro18,
  author    = {Maximilian Irro},
  title     = {{Concurrent Programming with Actors and Microservices}},
  school    = {TU Wien},
  type      = {Master Thesis},
  year      = {2018},
  month     = {September},
  keywords  = {concurrent programming, actor model, microservice architecture},
  timestamp = {20180930},
  url       = {https://max.irro.at/pub/dipl/},
  pdf       = {https://max.irro.at/pub/dipl/thesis.pdf}
}
```

### Abstract (EN)

Common problems require applications to manage multiple concerns
simultaneously. A convenient approach is the concept of *concurrent
programming*. In this thesis, we investigate two different models for
introducing concurrent computational units into software architectures.
One approach is the *actor model* that defines theoretically well-known
constructs supporting concurrent, parallel and distributed execution in a
transparent way. The other approach is an architectural style based on
*microservices*, a recent trend that gained academic and industrial
popularity. Microservices facilitate many principles of the old Unix
philosophy by composing complex functionality through small, independent,
highly cohesive and loosely coupled executables. These programs
interoperate via lightweight, technology-heterogeneous messaging
channels. The deployment modality of microservices conceives concurrent
execution through the operating system scheduler. This thesis compares
the programming of concurrent computation through actors and
microservices with respect to a non-trivial concurrent system scenario.
We argue that both approaches share many conceptual similarities and show
few but significant differences. Both models have the same expressive
capabilities regarding concurrent programming concerns like communication
and scalability, but are subject to different trade-offs. We provide
implementations of the system scenario based on actor and microservice
architectures. Benchmark results of these implementations suggest that
actors provide better system efficiency through a smaller codebase.
Microservice architectures consume significantly more system resources
and suffer especially from purely synchronous communication mechanisms.

### Abstract (DE)

Applikationen benötigen häufig eine simultane Bearbeitung mehrerer
Aufgaben. *Nebenläufige Programmierung* ist hierfür ein verbreitetes
Konzept. Diese Arbeit beschäftigt sich mit zwei Modellen zur Definition
nebenläufiger Programmeinheiten innerhalb von Softwarearchitekturen.
Eines dieser Modelle ist das *Actor Model*. Es definiert theoretisch
wohlfundierte Konstrukte, welche transparent eine nebenläufige, parallele
und verteilte Ausführung ermöglichen. Bei dem anderen Modell handelt es
sich um einen relativ neuen Architekturstil unter Verwendung von
*Microservices*, welche sich kürzlich im akademischen und industriellen
Umfeld großer Beliebtheit erfreuen. Microservices bauen auf viele
Prinzipien der alten Unix-Philosophie, indem sie komplexe Funktionalität
durch den Zusammenschluss kleiner, unabhängiger, kohäsiver und lose
gekoppelter Programme konzipieren. Diese Programme interagieren über
leichtgewichtige, auf Nachrichtenaustausch basierende, technologisch
heterogene Kommunikationskanäle. Microservices unterliegen einer implizit
nebenläufigen Ausführungsmodalität durch den Prozess-Scheduler des
Betriebssystems. Diese Arbeit vergleicht die Programmierung von
nebenläufiger Ausführung mittels Actors und Microservices relativ zu
einem nichttrivialen Fallbeispiel eines nebenläufigen Systems. Wir
argumentieren, dass beide Ansätze viele Gemeinsamkeiten und wenige aber
wichtige konzeptionelle Unterschiede besitzen. Beide Modelle haben
gleichwertige Möglichkeiten um typische Anliegen der nebenläufigen
Programmierung wie Kommunikation und Skalierbarkeit auszudrücken. Jedoch
unterliegen die Modelle unterschiedlichen Trade-offs. Wir stellen
Implementierungen des Fallbeispiels bereit, welche jeweils auf Actors
bzw. Microservices basieren. Die Resultate eines Benchmarkings dieser
Implementierungen legen nahe, dass Actors eine bessere Systemeffizienz
verbunden mit einer kleineren Codebasis ermöglichen.
Microservice-Architekturen hingegen konsumieren erheblich mehr
Systemresourcen und leiden vor allem unter den Auswirkungen rein
synchroner Kommunikationsmechanismen.