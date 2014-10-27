package org.kurator.akka;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.concurrent.TimeoutException;

import junit.framework.TestCase;

public class TestHammingJava extends TestCase {

    private OutputStream outputStream;
    private PrintStream printStream;

    @Override
    public void setUp() {
        outputStream = new ByteArrayOutputStream();
        printStream = new PrintStream(outputStream);
    }

    public void testHamming_Max1() throws TimeoutException,
            InterruptedException {

        Hamming wf = new Hamming(1, printStream, ", ");
        wf.run();
        assertEquals("1", outputStream.toString());
    }

    public void testHamming_Max10() throws TimeoutException,
            InterruptedException {
        Hamming wf = new Hamming(10, printStream, ", ");
        wf.run();
        assertEquals("1, 2, 3, 4, 5, 6, 8, 9, 10", outputStream.toString());
    }

    public void testHamming_Max30() throws TimeoutException,
            InterruptedException {
        Hamming wf = new Hamming(30, printStream, ", ");
        wf.run();
        assertEquals("1, 2, 3, 4, 5, 6, 8, 9, 10, "
                + "12, 15, 16, 18, 20, 24, 25, 27, 30", outputStream.toString());
    }

    public void testHamming_Max100() throws TimeoutException,
            InterruptedException {
        Hamming wf = new Hamming(100, printStream, ", ");
        wf.run();
        assertEquals( "1, 2, 3, 4, 5, 6, 8, 9, 10, "
                    + "12, 15, 16, 18, 20, 24, 25, "
                    + "27, 30, 32, 36, 40, 45, 48, "
                    + "50, 54, 60, 64, 72, 75, 80, "
                    + "81, 90, 96, 100", outputStream.toString());
    }

}
