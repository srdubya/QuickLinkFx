package com.srdubya;

import java.io.*;
import java.util.LinkedList;

public class App
{
    public static void main( String[] args ) {
        if (args.length != 3) {
            printUsage(System.err);
            System.exit(1);
        }

        var key = args[0].substring(2);
        var value = args[1].substring(2);
        var filename = args[2];

        File file = new File(filename);
        if (!file.isFile()) {
            System.err.format("Cannot find %s%s", filename, System.lineSeparator());
            printUsage(System.err);
            System.exit(2);
        }

        var lines = new LinkedList<String>();
        var foundKey = false;

        try (var br = new BufferedReader(new FileReader(file))) {
            String line;
            String keyLine = String.format("<key>%s</key>", key);

            while ((line = br.readLine()) != null) {
                lines.add(line);
                if (line.contains(keyLine)) {
                    foundKey = true;
                    var nextLine = br.readLine();
                    var prefix = nextLine.substring(0, nextLine.indexOf('>') + 1);
                    var suffix = nextLine.substring(nextLine.indexOf("</"));
                    var newLine = String.format("%s%s%s", prefix, value, suffix);
                    lines.add(newLine);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(2);
        }

        if (!foundKey) {
            System.err.println("No changes made, key not found");
            printUsage(System.err);
            System.exit(3);
        }

        try (var bw = new BufferedWriter(new FileWriter(file, false))) {
            for (var line : lines) {
                bw.write(line);
                bw.write(System.lineSeparator());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void printUsage(PrintStream out) {
        out.println("Command line requirements:");
        out.println("  --<keyname>   ex:  --JVMRuntimePath");
        out.println("  --<value>     ex:  --/Library/Java/JavaVirtualMachines/jdk-11.0.1.jdk");
        out.println("  <file path>");
    }
}
