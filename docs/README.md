# Thesis Documents

...

## Graphics

* Graphics are written in the DOT graph description language
* Translated with Graphviz https://graphviz.gitlab.io
* Use `dot2graphiz` to translate graph files to a more LaTeX look and feel https://dot2tex.readthedocs.io/en/latest/index.html

```
dot -Txdot system.dot | dot2tex -f pst > system.tex
dot -Tps system-architecture.dot > system-architecture.ps
dot -Tsvg system-architecture.dot > system-architecture.svg
```

Online editor: http://www.webgraphviz.com