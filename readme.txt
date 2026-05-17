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

- **Compile project**: `mvn compile`
- **Run game**: `mvn javafx:run -Djavafx.mainClass=gui.App`
- **Create distributable**: `mvn package`
- **Generate docs**: `mvn javadoc:javadoc`
- **Clean**: `mvn clean`
