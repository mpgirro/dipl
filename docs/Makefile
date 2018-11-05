
.PHONY: default
default: thesis ;

# This uses Madoko (https://github.com/koka-lang/madoko)
proposal:
	madoko --pdf proposal.mdk

thesis:
	madoko --pdf thesis.mdk

references:
	madoko --pdf references.mdk

slides-seminar1: ensure-outdir
	pdflatex -halt-on-error -output-directory out slides-seminar1.tex

slides-seminar2: ensure-outdir
	pdflatex -halt-on-error -output-directory out slides-seminar2.tex

slides-exam: ensure-outdir
	pdflatex -halt-on-error -output-directory out slides-exam.tex

slides: ensure-outdir slides-seminar1 slides-seminar2 slides-exam

poster: ensure-outdir
	pdflatex -halt-on-error -output-directory out poster.tex

all: proposal thesis references slides poster

clean:
	rm -r out/

ensure-outdir:
	mkdir -p out