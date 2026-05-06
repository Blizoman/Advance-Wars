# IJA projekt 2026

> Game inspired by the game series **Advance Wars**

## Authors:

- **Roman Pružinský** (xpruzir00): OOP design, logging, parsing
- **Andrej Bližnák** (xblizna00): GUI, Bot, PathFinder

## Used technologies:

- **Build system**: maven
- **Program language**: Java 21
- **GUI**: JavaFX
- **Logs, maps**: JSON

> Tested on **Linux Debian 12**

## Compilation

> Mostly reusing **maven** commands

- **Compile project**: `make compile`
- **Run game**: `make run`
- **Create distributable**: `make exe`
- **Generate docs**: `make docs`

---

- _All required_: `make all` ( == `compile + docs + exe`)
- **Clean**: `make clean`
- **Zip**: `make zip`
