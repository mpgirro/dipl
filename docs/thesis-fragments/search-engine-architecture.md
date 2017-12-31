
# Crawler(s)

- allg siehe https://cs.nyu.edu/courses/spring16/CSCI-GA.2580-001/crawler.html
- allg siehe https://web.njit.edu/~alexg/courses/cs345/OLD/F15/solutions/f2345f15.pdf

- bekommen URLs (entweder neue oder aus einem URL-repository) um diese herunterzuladen
- URLs sind entweder RSS/Atom-Feed oder die Homepages von Podcasts (allgemein) oder von Episoden (diese können zusatzinfos enthalten die in den Feeds nicht enthalten sind)
- Crawlers produzieren Arbeitspakete (für Indexer) mit den Informationen die sie heruntergeladen haben
- Brauchen eine Refresh-Strategie mit der sie die URLs erneut besuchen
    - sollen sie diese selbst errechnen (ja, da sie sonst kaum Arbeit haben) oder in einen eigenen Actors/MS ausgelagert werden
- halten sich an Robots Exclusion Standard (robots.txt) etc
- normalisieren URLs 
- haben "policies":
    - selection policy: "only a fraction of the web-pages on the Web will be accessed, retrieved and subsequently processed/parsed and indexed. That fraction would depend on file extensions, multimedia handling, languages and encoding, compression and other protocols supported" --> nur Podcast relevante sachen
    - visit policy: "are variants of the two fundamental graph search operations: Breadth-First Search (BFS) and Depth-First Search (DFS): the former uses a queue to maintain a list of to be visited sites, and the latter a stack. Between the two the latter (DFS) is more often used in crawling"
        - Hyperlinked Web-pages: sollen zB in shownotes verlinkte seiten auch besucht und indexiert werden? --> an sich interessante informationen
    - observance policy: robots.txt beachten, nicht zu oft besuchen um dem server zu überlasten
    - parallelization/coordination policy: mehrere crawler, daher synchronisation welcher gerade welche web-page bearbeitet (synchronisierte queue/stack?)

- siehe "Mercator: A scalable, extensible Web crawler" https://dl.acm.org/citation.cfm?id=598733
- siehe "Effective page refresh policies for Web crawlers" https://dl.acm.org/citation.cfm?id=958945

# Indexer(s)

- Verarbeiten die Arbeitspakete die Crawler produzieren
- nimmt die web-pages und erzeugt den index (= effiziente datenstruktur die schnelles suchen erlaubt)
- in der literatur kommunizieren Crawler und Indexer durch ein gemeinsames Repository miteinander
    - da MS keine gemeinsamen DBs teilen, und Actors das bei mir auch nicht sollen, wie löse ich das in beiden Fällen in der Architektur?
- vorerst auszulassende Verbesserungen (da für meine Dipl nicht relevant)
    - Case-folding
    - Datums-homogenisierung
    - Hyphenization
    - Accents and punctuation marks
    - Stemming
    - Synonyms
    - Acronyms
    - etc.





# Result Cache

- abspeichern der Results
- neuberechnung des Caches oder invalidieren?