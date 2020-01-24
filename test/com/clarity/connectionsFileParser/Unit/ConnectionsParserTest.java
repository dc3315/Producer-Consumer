// Copyright 2020, David Cattle, All rights reserved.

package com.clarity.connectionsFileParser.Unit;

import com.clarity.connectionsFileParser.ConnectionsParser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import static org.junit.Assert.*;

public class ConnectionsParserTest {

    public static final long START_TIME = 1565647204351L;
    public static final long END_TIME = 1565647246869L;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    @Before
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @After
    public void restoreStreams() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }


    @Test
    public void EmptyFileConnectedClientsTest() {
        File emptyFile = new File("test/com/clarity/connectionsFileParser" +
                "/testFiles/input-file-0.txt");

        ConnectionsParser tcp =
                new ConnectionsParser(emptyFile, START_TIME, END_TIME,
                        "Rehgan");
        try {
            tcp.connectedClients();
            assertEquals("", outContent.toString());
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void ConnectedClientsTest1() {
        File emptyFile = new File("test/com/clarity/connectionsFileParser" +
                "/testFiles/input-file-5.txt");

        ConnectionsParser tcp =
                new ConnectionsParser(emptyFile, START_TIME
                        , END_TIME,
                        "Rehgan");
        try {
            tcp.connectedClients();
            assertEquals("Tyreonna Heera ", outContent.toString());
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void ConsidersOutOfOrderConnectionsInWindowTest() {
        File emptyFile = new File("test/com/clarity/connectionsFileParser" +
                "/testFiles/input-file-7.txt");

        ConnectionsParser tcp =
                new ConnectionsParser(emptyFile, START_TIME
                        , END_TIME,
                        "Rehgan");
        try {
            tcp.connectedClients();
            assertTrue(outContent.toString().contains("John"));
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void IgnoresLateConnectionsAfterExceedingWindowTest() {
        File emptyFile = new File("test/com/clarity/connectionsFileParser" +
                "/testFiles/input-file-7.txt");

        ConnectionsParser tcp =
                new ConnectionsParser(emptyFile, START_TIME
                        , END_TIME,
                        "Rehgan");
        try {
            tcp.connectedClients();
            assertFalse(outContent.toString().contains("Ronald"));
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }
}
