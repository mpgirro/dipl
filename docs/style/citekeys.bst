% BibTeX bibliography style `citekeys 
% by David Kotz dfk@cs.dartmouth.edu
% March 1994
% modified (distantly) from
  %   BibTeX standard bibliography style `alpha'
	% version 0.99a for BibTeX versions 0.99a or later, LaTeX version 2.09.
	% Copyright (C) 1985, all rights reserved.
	% Copying of this file is authorized only if either
	% (1) you make absolutely no changes to your copy, including name, or
	% (2) if you do make changes, you name it something other than
	% btxbst.doc, plain.bst, unsrt.bst, alpha.bst, and abbrv.bst.
	% This restriction helps ensure that all standard styles are identical.
	% The file btxbst.doc has the documentation for this style.

ENTRY { author } {} {}

FUNCTION {article} {}
FUNCTION {book} {}
FUNCTION {booklet} {}
FUNCTION {inbook} {}
FUNCTION {incollection} {}
FUNCTION {inproceedings} {}
FUNCTION {conference} {}
FUNCTION {manual} {}
FUNCTION {mastersthesis} {}
FUNCTION {misc} {}
FUNCTION {phdthesis} {}
FUNCTION {proceedings} {}
FUNCTION {techreport} {}
FUNCTION {unpublished} {}
FUNCTION {default.type} { misc }

MACRO {jan} {"January"}
MACRO {feb} {"February"}
MACRO {mar} {"March"}
MACRO {apr} {"April"}
MACRO {may} {"May"}
MACRO {jun} {"June"}
MACRO {jul} {"July"}
MACRO {aug} {"August"}
MACRO {sep} {"September"}
MACRO {oct} {"October"}
MACRO {nov} {"November"}
MACRO {dec} {"December"}

READ

FUNCTION {presort}
{ cite$
  #1 entry.max$ substring$
  'sort.key$ :=
}

ITERATE {presort}

SORT

FUNCTION {cite.key.only} { cite$ write$ newline$ }

ITERATE {cite.key.only}
