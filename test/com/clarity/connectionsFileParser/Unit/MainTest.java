// Copyright 2020, David Cattle, All rights reserved.

package com.clarity.connectionsFileParser.Unit;

import com.clarity.connectionsFileParser.Main;
import com.clarity.connectionsFileParser.src.AutoDeletingTempFile;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Tests robustness of tool from command line.
 */
public class MainTest {

    @Test(expected = IllegalArgumentException.class)
    public void mainFailsWithoutCorrectNumberOfArgumentsTest() throws FileNotFoundException {
        Main.main(new String[]{});
    }

    @Test(expected = IllegalArgumentException.class)
    public void mainFailsWithoutCorrectNumberOfArguments2Test() throws FileNotFoundException {
        Main.main(new String[]{"dummy.txt", "startTime", "endTime", "hostName",
                "extra"
                ,});
    }

    @Test(expected = IllegalArgumentException.class)
    public void mainFailsWithoutCorrectNumberOfArguments3Test() {
        try (AutoDeletingTempFile f = new AutoDeletingTempFile("testFile",
                ".txt", "/tmp")) {
            Main.main(new String[]{f.getAbsolutePath(), "-f", "endTime", "hostName"});
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void mainFailsWithoutCorrectNumberOfArguments4Test() {
        try (AutoDeletingTempFile f = new AutoDeletingTempFile("testFile",
                ".txt", "/tmp")) {
            Main.main(new String[]{f.getAbsolutePath(), "0", "1"});
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test(expected = FileNotFoundException.class)
    public void mainFailsWithInvalidFilenameTest() throws FileNotFoundException {
        Main.main(new String[]{"nonExistentFile.txt", "-f", "Claudiu"});
    }

    @Test(expected = FileNotFoundException.class)
    public void mainFailsWithInvalidFilename2Test() throws FileNotFoundException {
        Main.main(new String[]{"nonExistentFile.txt", "0", "100", "Claudiu"});
    }

    @Test(expected = NumberFormatException.class)
    public void mainFailsWithInvalidTimeArgumentsTest() {
        try (AutoDeletingTempFile f = new AutoDeletingTempFile("testFile",
                ".txt", "/tmp")) {
            Main.main(new String[]{f.getAbsolutePath(), "s", "100", "Claudiu"});
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test(expected = NumberFormatException.class)
    public void mainFailsWithInvalidTimeArguments2Test() {
        try (AutoDeletingTempFile f = new AutoDeletingTempFile("testFile",
                ".txt", "/tmp")) {
            Main.main(new String[]{f.getAbsolutePath(), "1", "o", "Claudiu"});
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void mainFailsWithInvalidOptionsTest() {
        try (AutoDeletingTempFile f = new AutoDeletingTempFile("testFile",
                ".txt", "/tmp")) {
            Main.main(new String[]{f.getAbsolutePath(), "garbage"});
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }
}