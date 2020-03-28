//
//  main.swift
//  JALauncher
//
//  Created by Steve Wiley on 3/22/20.
//  Copyright © 2020 Steve Wiley. All rights reserved.
//

import Foundation
import os

func getJdkPath() -> String? {
    let task = Process()
    task.executableURL = URL(fileURLWithPath: "/usr/libexec/java_home")
    
    let pipe = Pipe()
    task.standardOutput = pipe
    
    do {
    try task.run()
    task.waitUntilExit()
    
    let inputFileHandle = pipe.fileHandleForReading
    let dataBuffer = inputFileHandle.readDataToEndOfFile()
    let output = String(data: dataBuffer, encoding: .utf8)
        os_log("JAAppLauncher -- JVM location = %{public}s", output!)
        return output
    } catch {
        os_log("JAAppLauncher -- ERROR running /usr/libexec/java_home")
    }
    return nil
}

os_log("Starting JAAppLauncher")

let arguments = CommandLine.arguments
os_log("args = %{public}s", CommandLine.arguments.debugDescription)

let mainBundle = Bundle.main
let bundlePath = mainBundle.bundlePath
os_log("bundlePath = %{public}s", bundlePath)

let infoDictionary = mainBundle.infoDictionary
let workingDirectory = infoDictionary?["LauncherWorkingDirectory"] as! String
os_log("workingDirectory = %{public}s", workingDirectory)

let fileManager = FileManager.default
os_log("cwd = %{public}s", fileManager.currentDirectoryPath)
fileManager.changeCurrentDirectoryPath(workingDirectory.replacingOccurrences(of: "$APP_ROOT", with: bundlePath))
os_log("cwd = %{public}s", fileManager.currentDirectoryPath)

let jdkPath = getJdkPath()
if jdkPath == nil {
    os_log("JDK not found, exiting...")
    exit(-1)
}
os_log("JDK = '%{public}s'", jdkPath!.trimmingCharacters(in: .whitespacesAndNewlines))

let libPath = jdkPath!.trimmingCharacters(in: .whitespacesAndNewlines) + "/lib/libjli.dylib"
if !fileManager.fileExists(atPath: libPath) {
    os_log("libjli.dylib not found at %{public}s, exiting...", libPath)
    exit(-2)
}
os_log("Found libjli.dylib at %{public}s", libPath)

let mainClassname = infoDictionary?["JVMMainClassName"] as! String
os_log("main class = '%{public}s'", mainClassname)

let javaPath = bundlePath + "/Contents/Java/"
var classPath = "-Djava.class.path="
var separator = ""
os_log("bulding class path, '%{public}s'", classPath)
for path in infoDictionary?["JVMClassPaths"] as! [String] {
    if fileManager.fileExists(atPath: javaPath + path) {
        classPath += separator + javaPath + path
        separator = ":"
        os_log("    + '%{public}s'", javaPath + path)
    }
}
os_log("class path arg = '%{public}s'", classPath)

let libraryPath = "-Djava.library.path=" + bundlePath + "/Contents/MacOS"
os_log("library path = '%{public}s'", libraryPath)

let vmOptions = infoDictionary?["JVMOptions"] as! [String]
os_log("vm options = '%{public}s'", vmOptions.debugDescription)

let vmArgs = infoDictionary?["JVMArguments"] as! [String]
os_log("vm args = '%{public}s'", vmArgs.debugDescription)

var launchArgs = [String]()
launchArgs.append(arguments[0])
launchArgs.append(classPath)
launchArgs.append(libraryPath)
launchArgs.append(contentsOf: vmOptions)
launchArgs.append(mainClassname)
launchArgs.append(contentsOf: vmArgs)
if arguments.count > 1 {
    for i in 1 ..< arguments.count {
        launchArgs.append(arguments[i])
    }
}

var libPathAsCString = strdup(libPath)

var launchArgsAsCStrings = launchArgs.map { strdup($0) }
launchArgsAsCStrings.append(nil)

let ret = launch(
    Int32(launchArgs.count),
    &launchArgsAsCStrings,
    libPathAsCString
)

launchArgsAsCStrings.forEach { free($0) }
free(libPathAsCString)
