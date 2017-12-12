
# This uses Madoko (https://madoko.codeplex.com/)
proposal:
	madoko --pdf proposal.mdk

thesis:
	madoko --pdf thesis.mdk

references:
	madoko --pdf references.mdk

journal:
	madoko --pdf journal.mdk

literature:
	madoko notes-literature.mdk

slides1:
	pdflatex -halt-on-error -output-directory out slides1.tex

slides: slides1

all: proposal thesis references journal slides

clean:
	rm -r out/
