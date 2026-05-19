# PlanaBite

Måltidsplanlegger som hjelper deg å velge sunne middager basert på kostholdstype og mål, og viser deg den billigste handlekurven fra norske dagligvarebutikker via Kassalapp-API-et.

---

## Krav – installer dette først

Du trenger tre ting på maskinen din:

### 1. Java Development Kit (JDK) 17 eller nyere

Last ned fra [https://adoptium.net](https://adoptium.net) → velg **JDK 17**, riktig OS og arkitektur → kjør installasjonsprogrammet.

Sjekk at det fungerer:
```bash
java -version
```
Du skal se noe med `17.x.x` eller høyere.

#### IDE (valgfritt, men anbefalt)

Du kan bruke hvilken som helst Java-kompatibel IDE til å åpne og kjøre backenden. De mest brukte er:

- **IntelliJ IDEA** – [https://www.jetbrains.com/idea/download](https://www.jetbrains.com/idea/download) (Community-utgaven er gratis og fungerer fint)
- **Visual Studio Code** – [https://code.visualstudio.com](https://code.visualstudio.com) – installer i tillegg utvidelsen [Extension Pack for Java](https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-java-pack)
- Eclipse, NetBeans eller andre Java-IDE-er fungerer også

Med en IDE kan du åpne mappen `backend/planabite-backend` direkte og starte applikasjonen derfra i stedet for å bruke terminalen.

---

### 2. Node.js (inkluderer npm)

Last ned fra [https://nodejs.org](https://nodejs.org) → velg **LTS-versjonen** → kjør installasjonsprogrammet.

Sjekk at det fungerer:
```bash
node -v
npm -v
```

---

### 3. PostgreSQL 14 eller nyere

Last ned fra [https://www.postgresql.org/download](https://www.postgresql.org/download) → velg ditt OS → kjør installasjonsprogrammet.

Under installasjonen:
- Velg et brukernavn og passord for `postgres`-brukeren – husk disse, du trenger dem i neste steg
- La porten stå på `5432` (standard)

Sjekk at det fungerer (åpne en terminal og kjør):
```bash
psql -U postgres -c "\l"
```
Du blir bedt om passordet du valgte under installasjonen.

---

## Konfigurer application.properties

Før du starter backenden må du sette inn dine egne innloggingsdetaljer i konfigurasjonsfilen:

```
backend/planabite-backend/src/main/resources/application.properties
```

Filen ser slik ut:

```properties
spring.application.name=planabite-backend
kassalapp.api.key=DIN_KASSALAPP_API_NØKKEL

spring.datasource.url=jdbc:postgresql://localhost:5432/planabite
spring.datasource.username=DITT_POSTGRES_BRUKERNAVN
spring.datasource.password=DITT_POSTGRES_PASSORD
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
```

### PostgreSQL-brukernavn og passord

Bytt ut `DITT_POSTGRES_BRUKERNAVN` og `DITT_POSTGRES_PASSORD` med det du valgte da du installerte PostgreSQL.

### Kassalapp API-nøkkel

Kassalapp-API-et brukes til å hente priser fra norske dagligvarebutikker. Du må opprette en gratis konto og generere en API-nøkkel:

1. Gå til [https://kassal.app](https://kassal.app) og opprett en konto
2. Logg inn og gå til **API** i menyen
3. Generer en ny API-nøkkel
4. Lim inn nøkkelen i `application.properties` på linjen `kassalapp.api.key=`

---

## Oppsett av databasen

Åpne en terminal og kjør følgende kommandoer **én gang** for å opprette databasen og fylle den med data:

```bash
psql -U postgres -c "CREATE DATABASE planabite;"
psql -U postgres -d planabite -f backend/SQL-script/planabite_script.sql
```

Skriv inn passordet ditt når du blir spurt.

---

## Start backend (Spring Boot)

Åpne en **ny terminal** og naviger til backend-mappen:

```bash
cd backend/planabite-backend
```

**Mac/Linux:**
```bash
./mvnw spring-boot:run
```

**Windows:**
```bash
mvnw.cmd spring-boot:run
```

> Første gang vil Maven laste ned avhengigheter – dette tar litt tid. Vent til du ser `Started` i loggen.

Backenden kjører nå på `http://localhost:8080`.

---

## Start frontend (React)

Åpne en **ny terminal** (backenden skal fortsatt kjøre i den første) og naviger til frontend-mappen:

```bash
cd frontend
```

Installer avhengigheter (kun nødvendig første gang):
```bash
npm install
```

Start utviklingsserveren:
```bash
npm run dev
```

Åpne nettleseren og gå til adressen som vises i terminalen, vanligvis:

```
http://localhost:5173
```

---

## Prosjektstruktur

```
USN-BOP3000/
├── backend/
│   ├── planabite-backend/   # Spring Boot API
│   └── SQL-script/          # Databaseoppsett og testdata
└── frontend/                # React + Vite + Tailwind CSS
```

---

## Feilsøking

| Problem | Løsning |
|---|---|
| `java: command not found` | JDK er ikke installert eller ikke lagt til i PATH – prøv å starte terminalen på nytt etter installasjon |
| `psql: command not found` | PostgreSQL er ikke i PATH – sjekk at du valgte å legge til verktøy i PATH under installasjonen, eller restart terminalen |
| `Connection refused` på port `5432` | PostgreSQL-tjenesten kjører ikke – start den via tjenestepanelet (Windows) eller `brew services start postgresql` (Mac med Homebrew) |
| Backenden starter ikke | Sjekk at databasen `planabite` eksisterer og at passordet i `application.properties` stemmer |
| `npm: command not found` | Node.js er ikke installert – se steg 2 |
