# Thesis Documents

...

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
