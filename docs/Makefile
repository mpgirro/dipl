
# This uses Madoko (https://madoko.codeplex.com/)
proposal:
	madoko --pdf proposal.mdk

thesis:
	madoko --pdf thesis.mdk

references:
	madoko --pdf references.mdk

journal:
	madoko --pdf journal.mdk

slides:
	pdflatex -halt-on-error -output-directory out slides1.tex

all: proposal thesis references journal

clean:
	rm -r out/
