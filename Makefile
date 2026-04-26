LOGIN = xpruzir00

.PHONY: c compile run exe clean docs clean gh zip

c: clean compile
compile:
	mvn compile

run:
	mvn javafx:run -Djavafx.mainClass=gui.App

exe:
	mvn package

docs:
	mvn javadoc:javadoc

clean:
	clear
	rm -f git_history.txt
	mvn clean

gh:
	git log --all --date=short --pretty=format:"%ad %an: %s" > git_history.txt

zip: clean gh
	mkdir -p $(LOGIN)
	cp -r src data lib readme.txt pom.xml ai_audit.md git_history.txt $(LOGIN)/ 
	zip -r $(LOGIN).zip $(LOGIN)
	rm -rf $(LOGIN)

copyJavas:
	rm -rf ~/Downloads/IJA-projSrcs
	mkdir -p ~/Downloads/IJA-projSrcs
	find src/main/java/ -name "*.java" -exec cp {} ~/Downloads/IJA-projSrcs \;

# assignment: 	https://moodle.vut.cz/mod/folder/view.php?id=667385
# github:		https://github.com/RomanPruzinsky/IJA-proj