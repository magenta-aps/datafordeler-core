.. _command:

Kommandointerface
=================

Datafordeleren indeholder et simpelt HTTP-interface til eksekvering af kommandoer,
såsom at sætte en dataimport i gang. Det er hensigten at dette interface skal anvendes
af et adminstrationssystem, men med værktøjer som f.eks. `Postman <https://www.getpostman.com/>`_
eller `curl <https://curl.haxx.se/>`_ er det muligt at anvende dette interface direkte.

Der er adgangskontrol på dette interface, så en gyldig SAML-token for en bruger med adgang til
at udføre kommandoer skal sendes med.

Kommandointerfacet lytter på adressen ``https://<server>/command/{id}``, hvor følgende muligheder er tilgængelige:


POST
  Eksempel: POST /command/pull

  Starter en kommando. For øjeblikket er kun kommandoerne "pull" og "dump" tilgængelige,
  hvor "dump" er beregnet til intern testing. Kommandoen vil forvente parametre, som
  angives i forespørgslens body, f.eks. ``{"plugin":"cvr"}`` starter et pull for CVR-pluginet.
  Hvis kommandoen accepteres, sender servicen en status tilbage, som bl.a. indeholder et id.


GET
  Eksempel: GET /command/7

  Henter status for en kommando, så det kan kontrolleres om kørslen er gået godt, stadig kører, eller er fejlet.
  Svaret er indkapslet i et JSON-objekt, med følgende felter:

  - received: Hvornår kommandoen blev modtaget.
  - handled: Hvis kommandoen er kørt færdig, blev afbrudt, eller der opstod en fejl, indsættes tidspunktet her.
  - errorMessage: Hvis der er opstået en fejl, indsættes fejlbeskeden her.
  - id: Kommandoens identifikation.
  - status: Kørslens status; en af ["queued", "running", "successful", "failure", "cancelled"].
  - commandName: Kommandoens navn, f.eks. "pull".


DELETE
  Eksempel: DELETE /command/7

  Afbryder en kørende kommando og returnerer status som i GET
