# AI Audit Log - Tým xpruzir00

**Datum poslední aktualizace:** [DD. MM. 2026]

---

## 1. Maven
* **Nástroj:** Gemini 3
* **Datum:** 05. 03. 2026
* **Prompt (nebo způsob použití):**
  > "Vyznám sa v Gradle, robím Android appky, ale na javu teraz potrebujem maven podľa tohoto zadania (špecifikoval som parametre knižníc), pomôž mi navrhnúť takýto pom.xml"
* **Úprava studentem:**
  AI navrhlo štruktúru, ktorú som si upravil a dotvoril podľa toho aby sa mi páčila. (napr. zmena groupId, sourceDirectory) Následne som podľa nej vytvoril Makefile na zjednodušenie príkazov.
* **Míra generování:** 60%

---

## 2. Dogenerovanie návrhu podľa predlohy
* **Nástroj:** Gemini 3
* **Datum:** 20. 04. 2026
* **Prompt:**
  > fill terrainTypeName with values from terrain.tsv, use same style as UnitTypeName (also Consts etc)
  
  > Now generate TerrainType (similarly to UnitType) with parameters from terrain.tsv, create inheritance (with english naming from file) with predefined v alues from terrain.tsv

  > try to keep as close coding style as unit/type/
* **Úprava studentem:**
  Už som vytvoril OOP návrh pre celú časť unit/, teraz som nechal vygenerovať podľa mojej predlohy terrainType
* **Míra generování:** 40%

---

## 3. Json IO - map
* **Nástroj:** Gemini 3
* **Datum:** 21. 04. 2026
* **Prompt:**
  > I need to read these data (inserted generated json for map) into GameBoard constructor. Implement it into GameBoardLoader and JsonSimples blank methods
* **Úprava studentem:**
  Vytvoril som hlavičky funkcií a prispôsobil konštanty a vygenerovanú logiku
* **Míra generování:** 40%

---

## 4. Json IO - logs
* **Nástroj:** Gemini 3
* **Datum:** 27. 04. 2026
* **Prompt:**
  > Write and read jsons for events logging, use JsonSimples if possible, implement into prepared LogFiler, for Player/Unit storing, as it cannot be stored in json, use adapters
* **Úprava studentem:**
  Vytvoril som hlavičky funkcií a prispôsobil konštanty a vygenerovanú logiku
* **Míra generování:** 40%

---

## 5. Game/Session splitting
* **Nástroj:** Claude Sonnet 4.6
* **Datum:** 27. 04. 2026
* **Prompt:**
  > I have Game logic, but need to track steps logs etc, and Game.java keeps getting larger, how to split it?
* **Úprava studentem:**
  Logika bola vytvorená, AI iba navrhlo rozdeliť to na Game a Session (vyššia vrstva), s tým že Game bude iba "hlúpe API" a Session but volať Game z Controllera
* **Míra generování:** 10%

---

## 6. Zlepšovanie
* **Nástroj:** Gemini 3 / Claude Sonnet 4.6 / GPT-5.4-mini
* **Datum:** 29. 04. 2026
* **Prompt:**
  > (Pasted my project files) Check these files and say what can be optimalized/betterized/shortened/simplified, also focus on dead code and reusable methods
* **Úprava studentem:**
  Na konci som nechal skontrolovať celý projekt rôzymi AI a implementoval návrhy ktoré odporúčali (väčšinou sa to týkalo zjednodušenia cyklov či podmienok)
* **Míra generování:** 5%

---

## 7. Gemini Bot
* **Nástroj:** Gemini 3
* **Datum:** 05. 05. 2026
* **Prompt:**
  > (Attached ASSIGNMENT.pdf) Generate a basic AI bot for the game that can perform valid moves (movement, capture), mainly for automated testing of UI and game mechanics
* **Úprava studentem:**
  Vznikol halvne kvoli testovaniu UI a inych hernych mechanik. Povodna verzia obsahovala iba obsadzovanie zakladni, avsak nebola schopna nejakeho utoku ci taktickeho nakupu, neskor som pouzil tuto zakladnu kostru a pridal k nej dalsie poziadavky ktore boli nutne ako logika utoku a vyberu cielov, rozhodovanie pri nakupe jendnotiek a taktiez heuristicky system v metode evaluatePosition.
* **Míra generování:** 30-40%

---

## 8. GUI and ASSETS
* **Nástroj:** Gemini 3 / CanvaAI / GPT-5.2-Codex-Xhigh
* **Datum:** 03. 05. 2026
* **Prompt:**
  Neslo o jednorazove upravy ale skor mensie prompty pre rozne upravy... napr.:

    > simplify this method 
    > fix rendering 
    > adjust layout 
    > improve styling
    > create small icon of tank, infantry and cannon, use resolution between 60x60 to 200x200 pixels, make it look like from same machinery
* **Úprava studentem:**
  GUI bolo implementovane manualne, AI bola vyuzita primarne ako pomocnik pri upravach kodu, navrhu priecinka resources a taktiez navrh vyberu spravneho fontu.
  Ostatne graficke prvky boli vytvarane v Canve/photopea ci basic skicar.

* **Míra generování:** 15–25%

---

## 9. Javadoc
* **Nástroj:** Ckaude Sonnet 4.6
* **Datum:** 06. 05. 2026
* **Prompt:**
  > (Vložil som môj pom.xml aj Makefile) Nejde mi vygenerovať dokumentáciu, čo mi chýba?
* **Úprava studentem:**
  Bolo potrebné iba vložiť 1 plugin (maven-javadoc-plugin)
* **Míra generování:** 50%

---
