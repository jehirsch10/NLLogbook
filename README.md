# NLLogbook
Natural Language Logbook


Project to explore using SpringBoot, Markdown, and light natural language processing
to improve olog-es and thus to improve olog-service.

*The curl command to set up the index properly is included in the mappind_definitions.sh file.*

*Note that authentication/authorization is not required.


**Current Project Status:**
---

PUT operation to create a logbook works correctly.
It takes an id number in the uri and markdown(commonmark)source code in the body and creates a logbook.
Currently if the id of the logbook to be created is already in use, the operation will replace the old logbook with the new one. 

Example body(using 1 in uri): 
```
{
    "id": 1,
    "src": "This is *Sparta*!",
    "description": "This is Sparta!"
}
```

GET operation to find a logbook by ID works correctly.
It takes an id number in the uri and returns the log entry or a 404 error.

#### GET operation to search for a logbook using a more-like-this query is now implemented(/moreLikeThis).
Using the previously mentioned Sparta example, some queries(in body) that will find it include:
- is
- This
- fdfsdf is dfd dfdf fdsd
- Sparta*!
- sparta

And some queries that will not find it include:
- tis
- fdf fdsf tis fdsfsd
- i s

#### GET operation to search for a logbook using fuzzy searching is now implemented(/fuzzy).
Using the previously mentioned Sparta example, some queries(in body) that will find it include:
- is
- parta
- isn 
- sparta

And some queries that will not find it include:
- part
- This is
- is s
- \*Sparta*!

#### GET operation to search for a logbook using a match query and fuzzy searching is now implemented(/search).
Using the previously mentioned Sparta example, some queries that(in body) will find it include:
- is
- thas xfr
- isn 
- \*Sporta*!

And some queries that will not find it include:
- part
- fdfdf fdfd

#### GET operation to search for a logbook using a multi-match query and fuzzy searching is now implemented(/multisearch/{other_field}).
This operation cannot be well tested using the current curl index set-up, as it is meant for searching through multiple fields rather than just description.