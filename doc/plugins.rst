.. _plugins:

Plugin-opbygning
================

Et plugin er en separat Jar-fil med hierarki af klasser til håndtering af en afgrænset datakilde, f.eks. CPR.
Størstedelen af disse klasser nedarver fra superklasser i core, og implementerer den funktionalitet som forventes deraf.
Hvert plugin har desuden en implementation af klassen Plugin, som har til opgave at definere og udstille de øvrige komponenter i pluginet.

Et plugins væsentligste komponenter er:

RegisterManager
  Klasse til håndtering af EntityManagers, med opslag af disse, samt metoder til at udføre Pull på datakilden.
  Som standard vil en RegisterManager uddelegere Pull-opgaven til sine EntityManagers,
  men afhængigt af kildens interface er der fleksibilitet til at dette kan implementeres anderledes, f.eks.
  hvis kilden ikke stiller separate urls til rådighed for de forskellige entitetstyper.

Configuration
  Klasse til at lagre konfigurationen for pluginet i databasen. Hvert plugin har ét konfigurationsobjekt,
  som består af én tabel med én række. Plugins kan implementere sin konfiguration individuelt,
  med forskellige felter (=tabelkolonner).
  En konfiguration skabes normalt med standardværdier i de forskellige felter hvis der ikke kan findes nogen
  eksisterende konfiguration i databasen.

ConfigurationManager
  Klasse til at hente og gemme konfigurationen for et givet Plugin.
  Hvis der ikke findes en Configuration i databasen ved opstart, skabes og gemmes der en med standardværdier.

Dataklasser
  For hver entitetstype findes klasser til lagring af entiteten og dens dataobjeketer (se `Bitemporalitet <bitemporality.rst>`_),
  samt tynde implementationer af bitemporalitetsklasserne.

EntityManager
  Klasse til at håndtere en bestemt Entitetstype, f.eks. "Person".
  EntityManageren sørger for at hente data fra kilden, fortolke indkommende data,
  sende kvitteringer til kilden, samt andre relevante opgaver relateret til den enkelte entitetstype.

Service
  Udstiller data for omverdenen med et webinterface.
  Størstedelen at funktionaliteteten er implementeret i core i en abstrakt superklasse,
  hvor der findes services til opslag med søgning ud fra parametre og søgning ud fra objekt-UUID, med REST og SOAP.
  I plugins befinder sig en tynd subklasse heraf for hver entitetstype, som således udstiller webservicen i praksis.

Query
  For hver entitetstype eksisterer en Query-klasse,
  som beskriver hvilke felter der kan foretages opslag med, og hvordan disse felter indgår i konstruktionen af et databaseopslag.

