version=QuickLinkFx-1.0-SNAPSHOT

target/$(version)/QuickLink.app : target/$(version).jar JALauncher/build/Release/JALauncher
	mvn -P platform-mac package
	cp JALauncher/build/Release/JALauncher target/$(version)/QuickLink.app/Contents/MacOS/JavaAppLauncher

target/$(version).jar : pom.xml $(wildcard *.java) src/main/resources/QuickLink.icns
	mvn package

JALauncher/build/Release/JALauncher : JALauncher/JALauncher.xcodeproj JALauncher/JALauncher-Bridging-Header.h\
 JALauncher/JALauncher/launcher.* JALauncher/JALauncher/main.swift
	cd JALauncher && xcodebuild

src/main/resources/QuickLink.icns: $(wildcard src/main/resources/*.png)
	cd src/main/resources && ./makemacicon

clean :
	mvn clean
