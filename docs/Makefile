
# This uses Madoko (https://madoko.codeplex.com/)
proposal:
	madoko --pdf proposal.mdk

thesis:
	madoko --pdf thesis.mdk

references:
	madoko --pdf references.mdk

slides1:
	pdflatex -halt-on-error -output-directory out slides1.tex

slides: slides1

poster: ensure-outdir
	pdflatex -halt-on-error -output-directory out poster.tex

all: proposal thesis references slides poster

clean:
	rm -r out/

ensure-outdir:
	mkdir -p out