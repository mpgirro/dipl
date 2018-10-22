# Thesis Documents


This directory contains all documents related to this master thesis project. The relevant documents are:

* [Thesis](thesis.mdk) is the central document for the whole submission document
* [Proposal](proposal.mdk) for working on this thesis
* [Bibtex database](dipl.bib) file with all literature that the thesis uses. The database holds additional literature that seemed relevant enough at some stage.
* Various slides for given presentations
* [Scientific poster](poster.tex) for the poster session that is part of the [EPILOG](http://www.informatik.tuwien.ac.at/studium/studierende/epilog) of the Faculty of Informatics. The poster is available at the digital poster session. 
* [Errata](errata.md) document


## Document Processor


Most documents are written using the [Madoko](http://madoko.org/reference.html) scholarly markdown flavor (`.mdk` file extension). Special thanks to [Daan Leijen](https://www.microsoft.com/en-us/research/people/daan/) for creating Madoko. A web editor for Madoko is available at [www.madoko.net](https://www.madoko.net) or it can be installed locally with:

    npm install madoko -g

Madoko is able to generate sophisticated HTML versions of scientific documents as well as standard PDF versions through LaTeX. Other files, like the slides and the poster, use LaTeX directly. The [Makefile](Makefile) wraps the respective call to the right tool for each target document. Madoko files are generated with:

    madoko --pdf thesis.mdk


## Graphics


DOT files:

* Graphics are written in the DOT graph description language
* Translated with Graphviz https://graphviz.gitlab.io
* Use `dot2graphiz` to translate graph files to a more LaTeX look and feel https://dot2tex.readthedocs.io/en/latest/index.html

```
dot -Txdot system.dot | dot2tex -f pst > system.tex
dot -Tps task-units.dot > task-units.ps
dot -Tsvg task-units.dot > task-units.svg
dot -Tpdf task-units.dot > task-units.pdf
```

PDF/PostScript output of `dot` produces a lot of whitespace margin. Remove with `pdfcrop` CLI (produces a new file with `-crop` appended to filename):

```
pdfcrop task-units.pdf
```

Online editor: http://www.webgraphviz.com

XML files:

* Create and edited with: [draw.io](https://www.draw.io)
