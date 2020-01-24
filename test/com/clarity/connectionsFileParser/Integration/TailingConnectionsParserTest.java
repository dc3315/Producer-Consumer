// Copyright 2020, David Cattle, All rights reserved.

package com.clarity.connectionsFileParser.Integration;

import com.clarity.connectionsFileParser.TailingConnectionsParser;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * I believe it is fair to assume integration tests are out of this scope
 * for this exercise because they take a long time to instrument.
 * But here is a playback of the log file with the TailingConnectionsParser
 * which displays how it would work in real time as well.
 */
public class TailingConnectionsParserTest {

    public static void main(String[] args) {
        File connectionsFile = new File(
                "./test/com/clarity/connectionsFileParser" +
                        "/testFiles/input-file-10000.txt");

        Thread consumer =
                new Thread(new TailingConnectionsParser(0, // Do not wait
                        // little since we are post-processing.
                        TimeUnit.HOURS.toMillis(1), connectionsFile,
                        "Morrigan"));

        consumer.start();
    }
}
