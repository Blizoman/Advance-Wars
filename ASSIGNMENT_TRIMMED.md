# Specifikace projektu: Tahová strategie

## 1. Specifikace základních požadavků

### 1. Mapa a prostředí
Hrací plocha se skládá z mřížky M x N. Každá dlaždice obsahuje přesně jeden typ terénu:
* **Rovina**
* **Les**
* **Hory:** Nepřekonatelné pro vozidla.
* **Město:** Generuje příjem ($1000/tah) pro majitele. Léčí jednotku, která je v něm umístěná.
* **Voda:** Nepřekonatelné pro veškeré jednotky.
* **Továrna:** Slouží jako spawn point pro nákup nových jednotek.
* **Velitelství (HQ):** Dobytím nepřátelského velitelství vyhráváte hru.

*Parametry jednotlivých polí jsou uvedeny v souboru `terrain.tsv`.*

### 2. Jednotky a souboj
Jednotky mají zdravotní body (HP) v rozmezí od 1 do 100. Pokud HP klesne na 0, jednotka je zničena.

**Typy jednotek:**
* **Pěchota:** Může dobývat města a velitelství. Vysoká pohyblivost, nízké poškození.
* **Tank (vozidlo):** Vysoké poškození, pohyb omezen horami.
* **Dělostřelectvo (vozidlo):** Útoky na dálku (může zasáhnout cíle vzdálené 2–3 pole), ale nemůže se pohybovat a střílet ve stejném tahu.

**Mechanika boje:**
Boj je zcela deterministický. Poškození musí být vypočítáno pomocí matematického vzorce, který se odvíjí od aktuálního HP útočníka a je sníženo bonusem terénu obránce. 
* **Vzorec pro výpočet:** `Poškození = ZákladníPoškození * (Aktuální_HP_Útočníka / 100) * (1 - BonusTerénu * 0.1)`
    * *ZákladníPoškození* = hodnota ze souboru `units-damage.tsv` pro danou dvojici útočník-obránce.
    * *BonusTerénu* = obranné číslo ze souboru `terrain.tsv` pro políčko, na kterém stojí obránce.
* Vypočítané poškození se zaokrouhlí (určete jak, např. dolů) a okamžitě se odečte od aktuálního HP obránce.
* **Protiofenzíva:** Pokud obránce přežije útok a vzdálenost to dovolí, automaticky odpoví útokem s použitím svého nového, sníženého HP.

*Parametry jednotlivých jednotek jsou v souboru `units.tsv` a body poškození v `units-damage.tsv`.*

**Budovy a destrukce:**
Budovy (Město, Velitelství, Továrna) nelze zničit útokem zbraní a nemají vlastní body zdraví (HP). Ostatní jednotky (Tank, Dělostřelectvo) nemohou na budovy útočit, mohou je pouze využívat k přesunu nebo blokování.

### 3. Herní smyčka a ovládání
* **Inicializace:** Hra na začátku vygeneruje nebo načte předdefinovanou mapu.
    * Pro uložení mapy a stavu hry doporučujeme využít formát JSON (snadná serializace a deserializace). Formát by měl obsahovat metadata mapy, definici mřížky a případné počáteční entity (viz např. soubor `game_stats.json`).
* **Struktura tahu:**
    1.  **Fáze příjmů:** Hráč obdrží finanční prostředky na základě dobytých měst.
    2.  **Fáze akce:** Hráč může přesouvat jednotky, útočit, dobývat území nebo nakupovat nové jednotky ve své továrně.
* **Ovládání:** Kliknutím na jednotku zobrazíte její platný dosah pohybu (zvýrazněný na mřížce). Kliknutím na cíl provedete přesun. Zobrazí se kontextové menu (*Útok / Zabrat / Čekat*).
* **Viditelnost:** Vizuálně rozlišujte jednotky a budovy patřící hráči 1, hráči 2 a neutrální vlastnictví.

### 4. Logování a simulace (architektonický požadavek)
* Musí být možné logovat průběh hry (každý tah, útok a nákup) do souboru.
* Hru lze načíst z tohoto souboru a postupovat vpřed a vzad tah po tahu.
* V kterémkoli okamžiku během přehrávání může uživatel přepnout do „režimu hry“ a pokračovat ve hře od daného stavu (původní protokol se poté vymaže).

### 5. Programové rozhraní hráče
* **Architektura:** Herní engine musí být zcela oddělen od grafického uživatelského rozhraní (architektura MVC).
* **Test „Dummy Bot“:** * Všechny týmy musí implementovat alespoň jednoho jednoduchého „AI“ protivníka (např. bota, který pouze nakupuje náhodné jednotky a pohybuje s nimi v náhodném platném směru). 
    * Engine musí být schopen požádat tohoto bota o tah a provést jej čistě pomocí kódu, bez simulace kliknutí myší.
    * *Ověření:* Musíte být schopni spustit zápas „Bot vs. Bot“ a sledovat, jak se hra hraje sama automaticky.

---

## 5. Doplňující informace a mechaniky

### Životní cyklus tahu jedné jednotky
1.  **Výběr (Select):** Hráč klikne na svou jednotku, která v tomto kole ještě nehrála. Engine jí vypočítá dostupné pole pro pohyb (pomocí vhodného algoritmu s ohledem na terén).
2.  **Pohyb (Move):** Hráč klikne na cílové políčko. Jednotka se na něj fyzicky přesune. *(Poznámka: Hráč může kliknout i na políčko, na kterém jednotka právě stojí – tím provede pohyb o 0 polí).*
3.  **Akce (Action):** Teprve po přesunu na cílové políčko engine vyhodnotí, co lze z daného místa dělat, a nabídne hráči menu:
    * **Útok (Attack):** Nabídne se pouze v případě, že se v dosahu útoku od nového cílového políčka nachází nepřítel.
    * **Zabrat (Capture):** Nabídne se pouze v případě, že jde o Pěchotu a stojí na budově, kterou lze zabrat.
    * **Čekat (Wait):** Ukončí tah jednotky bez další akce.
4.  **Konec (Deactivate):** Po provedení akce jednotka ztratí možnost hrát (např. vizuálně zešedne) až do dalšího kola.

### Důležité výjimky a pravidla

**1. Dělostřelectvo (Artillery) a střelba**
* Dělostřelectvo se nemůže hýbat a střílet zároveň.
* Pokud se dělostřelectvo posune, byť jen o jedno políčko, v nabídce "Akce" nesmí mít možnost *Útok*.
* Pokud chce střílet, musí ve fázi pohybu zůstat stát (pohyb o 0 polí). Následně může ze své stávající pozice zaútočit na cíle vzdálené 2 až 3 pole.

**2. Mechanika protiútoku (Counter-attack)**
* Útočník útočí z cílového místa jako první a udělí obránci poškození.
* Pokud to obránce přežije (má > 0 HP) **A ZÁROVEŇ** je útočník v jeho vlastním dostřelu, obránce okamžitě vystřelí zpět v rámci stejné akce.
* **Klíčový detail:** Obránce k protiútoku využívá už své *nové (snížené) HP*. Kdo střílí první, má tedy obrovskou výhodu.
* *Příklad s Dělostřelectvem:* Pokud Tank najede těsně vedle Dělostřelectva (vzdálenost 1) a zaútočí na něj, Dělostřelectvo nemůže opětovat palbu, protože má dostřel 2–3. Tank ho tedy poškodí zcela beztrestně.

**3. Pravidla léčení (Repair Mechanics)**
* Léčení probíhá vždy na začátku tahu hráče (Fáze 1: Příjem a Obnova), ještě předtím, než hráč může s čímkoliv pohnout.
* **Podmínky:**
    * Jednotka musí stát na budově, která patří stejnému hráči (Město, Továrna nebo Velitelství).
    * Jednotka musí být poškozená (má méně než 100 HP).
* Jednotka se vyléčí o **+20 HP**. Zdraví ale nikdy nesmí přesáhnout maximální hodnotu 100 HP.
* Léčení stojí peníze. Cena je **10 % z nákupní ceny** jednotky za každých 10 doplněných HP. Pokud hráč nemá peníze, jednotka se neopraví.

**4. Pohyb a "Průchodnost" (Stacking Rule)**
* **Sousednost:** Pro účely pohybu a určení sousedních polí se uvažuje pouze 4-sousednost (sever, jih, východ, západ). Diagonální pohyb není povolen.
* Celková cena cesty je součtem nákladů na vstup do všech políček na dané trase (výchozí políčko se do ceny nezapočítává).
* Při výpočtu cesty (Pathfinding) se jednotky chovají následovně:
    * **Přátelská jednotka:** Funguje jako "průchozí" políčko. Můžete přes ni projít, ale nesmíte na ní ukončit tah.
    * **Nepřátelská jednotka:** Funguje jako zeď. Nelze přes ni projít (blokuje cestu). To umožňuje vytvářet obranné linie.
* Pokud na políčku Továrny (Factory) stojí jakákoliv jednotka (i vaše vlastní), továrna je zablokovaná.
* Na každém políčku mapy může na konci fáze pohybu stát **maximálně jedna jednotka**. Pokud továrna vyrobí novou jednotku, zabere políčko továrny.

**5. Zabrání města / velitelství**
* Každá neutrální nebo nepřátelská budova vyžaduje **20 bodů** k dobytí. 
* Akce 'Dobýt' sníží tyto body o 10 % aktuálního počtu HP dobývající pěchoty zaokrouhleno dolů na celá čísla (např. pěchota se 100 HP dobytí zvládne za 2 tahy, poškozená na 50 HP za 4 tahy). 
* Pokud pěchota z pole odejde, body dobytí se resetují.

### Průběh hry
1.  **Hráč 1 je na tahu:** Má k dispozici celou svou armádu. Může postupně pohnout všemi svými jednotkami (každou jednou), útočit s nimi a nakupovat nové. Pořadí si určuje sám.
2.  **Soupeř čeká:** Během tahu Hráče 1 nemůže Hráč 2 nic dělat (pouze pasivně sleduje, jak je ničen).
3.  **Konec tahu:** Jakmile Hráč 1 signalizuje "End Turn", předává řízení.
4.  **Hráč 2 je na tahu:** Nyní se obnoví všechny jeho jednotky, dostane příjmy a může pohnout celou svou armádou.