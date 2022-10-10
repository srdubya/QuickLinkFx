version=QuickLinkFx-1.0-SNAPSHOT

QuickLinkFx.app : target/$(version).jar
	jpackage --input target/ --name QuickLinkFx --main-jar $(version).jar --main-class com.srdubya.QuickLink.MainHack --type app-image --icon src/main/resources/QuickLink.icns

target/$(version).jar : pom.xml $(wildcard *.java) src/main/resources/QuickLink.icns
	mvn package
	rm -rf QuickLinkFx.app

src/main/resources/QuickLink.icns: $(wildcard src/main/resources/*.png)
	cd src/main/resources && ./makemacicon

clean :
	mvn clean

spotless : clean
	rm -rf QuickLinkFx.app
