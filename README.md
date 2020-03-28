# QuickLinkFx
JavaFx Hyperlink &amp; Password Tool

## Building

### Mac

A `makefile` is set up to automate the build.  From the main directory, simply run `make`.
```shell script
> make
```

#### Details, if you're interested...

`QuickLink.icns` must be created before packaging.  To do this:
```shell script
> cd src/main/resources
> ./makemacicon
> cd -
```

Packaging `QuickLink.app`.
```shell script
> mvn -P platform-mac package
```

An improved Java launcher was created to automatically detect the location of the local JDK.
This launcher must be copied into the bundle each time after it is refreshed:
```shell script
> cd JALauncher
> xcodebuild build
> cp build/Release/JALauncher\
  ../target/QuickLinkFx-1.0-SNAPSHOT/QuickLink.app/Contents/MacOS/JavaAppLauncher
```

#### Troubleshooting
If you have problems launching the Mac version, fire up `Console` and search for 
`PROCESS: JavaAppLauncher`.

#### Resources used during development
[appbundle-maven-plugin](https://github.com/federkasten/appbundle-maven-plugin)

[Calling C code from Swift](https://theswiftdev.com/how-to-call-c-code-from-swift/)

[Creating a Mac OS Installer for  a JAR](https://centerkey.com/mac/java/)