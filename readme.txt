# ⚔️ Advance Wars (Java Remake)

Tento projekt je implementáciou ťahovej strategickej hry inšpirovanej populárnou hernou sériou **Advance Wars**. Hra bola vytvorená ako tímový projekt pre predmet **IJA (Semestrálny projekt Java)** v akademickom roku 2025/2026.

**Autori tímu (xpruzir00):**
* **Andrej Bližnák** (`xblizna00`): Grafické rozhranie (GUI), Logika botov (Dummy/Gemini), PathFinder, Asset Management.
* **Roman Pružinský** (`xpruzir00`): Návrh OOP architektúry, Systém logovania (JSON IO), Parser, Herná slučka a stav hry.

---

## 🚀 Rýchle spustenie (Compilation & Run)

Projekt na zostavenie a správu závislostí využíva nástroj **Maven**. Aplikácia bola úspešne testovaná na systéme **Linux Debian 12**.

* **Kompilácia projektu:**
    ```bash
    mvn compile
    ```
* **Spustenie hry:**
    ```bash
    mvn javafx:run -Djavafx.mainClass=gui.App
    ```
* **Zabalenie do distribuovateľného JAR:**
    ```bash
    mvn package
    ```
* **Vygenerovanie Javadoc dokumentácie:**
    ```bash
    mvn javadoc:javadoc
    ```
* **Vyčistenie build adresára:**
    ```bash
    mvn clean
    ```

---

## 🗺️ Implementované herné mechaniky

Hra plne rešpektuje a implementuje pravidlá definované v zadaní:

### 1. Mapa, Terén a Ekonomika
Hrací plán je reprezentovaný mriežkou $M \times N$, kde každé políčko obsahuje špecifický terén definovaný podľa konfiguračného súboru `terrain.tsv`:
* **Plain (Rovina):** Základná schodná lúka.
* **Forest (Les):** Poskytuje zvýšený obranný bonus pre jednotky.
* **Mountain (Hory):** Nepriechodný terén pre vozidlá (Tank, Delostrelectvo), schodný len pre pechotu.
* **Water (Voda):** Absolútne nepriechodná pre všetky typy pozemných jednotiek.
* **City (Mesto):** Generuje pasívny príjem **$1000/tah** pre vlastníka a na začiatku ťahu lieči zranenú jednotku o $+20\text{ HP}$ (každých $+10\text{ HP}$ stojí $10\%$ z pôvodnej ceny jednotky).
* **Factory (Továreň):** Spawn point, v ktorom si vlastník môže za nazbierané financie kupovať nové jednotky.
* **HQ (Veliteľstvo):** Hlavná budova hráča. Jej obsadením nepriateľom hra okamžite končí víťazstvom útočníka.

### 2. Jednotky a Deterministický Súboj
Každá jednotka disponuje životmi v rozmedzí $1$ až $100\text{ HP}$. Útoky sú plne deterministické a riadia sa exaktne podľa matematického vzorca zo zadania:

$$\text{Poškodenie} = \text{ZákladnéPoškodenie} \times \frac{\text{HP Útočníka}}{100} \times (1 - \text{BonusTerénu} \times 0,1)$$

* `ZákladnéPoškodenie` sa dedukuje zo súboru `units-damage.tsv` pre konkrétnu dvojicu útočník-obranca.
* `BonusTerénu` je obranné číslo z `terrain.tsv` prislúchajúce políčku obrancu.
* Vypočítané poškodenie sa zaokrúhľuje **nadol** a okamžite odčíta.
* **Mechanika protiútoku (Counter-attack):** Ak obranca útok prežije a útočník sa nachádza v jeho dostrele, obranca okamžite vracia úder, avšak už kalkuluje so svojím **novým (zníženým) HP**.

#### Typy jednotiek:
* 🏃 **Infantry (Pechota):** Vysoká mobilita v horách, nízke poškodenie. Ako jediná dokáže obsadzovať budovy (akcia *Capture* znižuje body dobytia budovy o $10\%$ z aktuálneho HP pechoty zaokrúhlene nadol; budova sa podvolí pri dosiahnutí $20$ bodov).
* 🚜 **Tank:** Vysoká palebná sila, obmedzený pohyb v horskom teréne.
* 🚀 **Artillery (Delostrelectvo):** Útok na diaľku ($2\text{--}3$ polia). Nedokáže sa pohnúť a strieľať v tom istom ťahu (pohyb o $>0$ polí zablokuje možnosť útočiť). Nemá schopnosť opätovať palbu na tesnú blízkosť (vzdialenosť $1$).

---

## 🏗️ Architektúra a Návrhové vzory

Projekt striktne dodržiava princípy objektovo orientovaného návrhu a implementuje stanovené architektonické vzory:

* **MVC (Model-View-Controller):** Herný engine (`Session`, `Game`, `GameBoard`) je kompletne izolovaný od grafického renderovania v JavaFX (`GameView`, `Renderer`). Komunikáciu, vstupy z myši a prepínanie stavov sprostredkováva riadiaci `GameController`.
* **Command Pattern (Vzor Príkaz):** Všetky kľúčové herné akcie (pohyb, útok, nákup, obsadzovanie) sú zapuzdrené do objektov udalostí. To nám umožňuje uchovávať detailný chronologický log ťahov.
* **Factory Pattern (Vzor Továreň):** Výroba a spawn nových jednotiek v továrňach prebieha prostredníctvom centralizovanej továrne na objekty, ktorá inicializuje atribúty na základe definícií z `units.tsv`.

---

## 🗄️ Logovanie a Replay Systém

Hra disponuje robustným systémom perzistencie dát cez formát **JSON** (`LogFiler.java`):
1.  **Logovanie ťahov:** Každá udalosť (pohyb, útok, nákup, koniec ťahu) sa ukladá do logovacieho súboru.
2.  **Krokovanie zápasu:** Používateľ môže hotový log spätne načítať a pomocou tlačidiel `◀ Step Back` a `Step Forward ▶` prechádzať hru krok po kroku (dopredu aj dozadu).
3.  **Hot-swap do hry:** Kedykoľvek počas prehrávania záznamu môže hráč kliknúť na hraciu plochu, prebrať nad hrou kontrolu a začať reálne hrať od aktuálneho stavu (pôvodný zvyšok logu sa odstrihne).

---

## 🤖 Programové rozhranie AI (Boti)

Hra podporuje plne automatizované hranie bez simulácie klikania myšou. Boti vykonávajú úkony sekvenčne – jeden úkon na jeden tik hernej slučky (využitím `Iterator<Unit>`), čím **predchádzame zamŕzaniu JavaFX UI vlákna**.

V projekte sú integrované dve obtiažnosti:
1.  🤖 **Dummy Bot (Easy):** Základný oponent. Nakupuje jednotky podľa lineárnej priority a posúva ich k najbližšiemu globálnemu cieľu (pechota k budovám, vozidlá k najbližšiemu nepriateľovi).
2.  🧠 **Gemini Bot (Medium / Heuristický):** Pokročilé AI. Na začiatku ťahu vygeneruje **Threat Map** (mapu hrozieb nepriateľského dostrelu). Používa metódu `evaluatePosition`, kde matematicky boduje políčka (prioritizuje HQ, liečenie, vyhýba sa nebezpečenstvu pre zranené jednotky a kalkuluje s najefektívnejším poškodením). Taktiež nakupuje jednotky dynamicky ako protizbraň (counter-pick) na základe zloženia nepriateľskej armády.

Umožňuje taktiež spustiť režim **Bot vs. Bot** a sledovať plne autonómny zápas.

---

## 📝 Použitie GenAI a Dokumentácia
V súlade s pravidlami predmetu bol pri vývoji využitý parťák v podobe Generatívnej AI. Kompletný zoznam promptov, miera generovania kódu a spôsob overenia výstupov študentmi sú detailne zdokumentované v priloženom súbore:
📄 **`ai_audit.md`**

História verzií a príspevky jednotlivých autorov sú vyexportované priamo z Gitu v koreňovom adresári:
📄 **`git_history.txt`**
