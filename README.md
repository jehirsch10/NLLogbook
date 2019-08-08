# NLLogbook
Natural Language Logbook


Project to explore using SpringBoot, Markdown, and light natural language processing
to improve olog-es and thus to improve olog-service.


**Current Project Status:**
. . .

PUT operation to create a logbook works correctly.
It takes an id number in the uri and markdown(commonmark)source code in the body and creates a logbook.
Currently if the id of the logbook to be created is already in use, the operation will replace the old logbook with the new one. 

GET operation to search for a logbook is **NOT** implemented.

GET operation to find a logbook by ID works correctly.
It takes an id number in the uri and returns the log entry or a 404 error.

*Note that authentication/authorization is not required.
