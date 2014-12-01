package org.kurator.akka.samples;

import org.kurator.akka.KuratorAkka;
import org.kurator.akka.KuratorAkkaTestCase;

public class TestIntegerFilter extends KuratorAkkaTestCase {
    
    @Override
    public void setUp() {
        super.setUp();
    }
    
    public void testIntegerFilter_DefaultMax() throws Exception {
        String[] args = { "-f", "classpath:/org/kurator/akka/samples/integer_filter.yaml",
                          "-p", "values=[5, 7, 254, -18, 55, 100, 99, 101]" };
        KuratorAkka.runWorkflowForArgs(args, stdoutStream, stderrStream);
        assertEquals("5, 7, -18, 55, 100, 99", stdoutBuffer.toString());
        assertEquals("", stderrBuffer.toString());
    }

    public void testIntegerFilter_CustomMax() throws Exception {
        String[] args = { "-f", "classpath:/org/kurator/akka/samples/integer_filter.yaml",
                          "-p", "values=[5, 7, 254, -18, 55, 100, 99, 101]" ,
                          "-p", "max=60"};
        KuratorAkka.runWorkflowForArgs(args, stdoutStream, stderrStream);
        assertEquals("5, 7, -18, 55", stdoutBuffer.toString());
        assertEquals("", stderrBuffer.toString());
    }

    public void testIntegerFilter_NoValues() throws Exception {
        String[] args = { "-f", "classpath:/org/kurator/akka/samples/integer_filter.yaml" };
        KuratorAkka.runWorkflowForArgs(args, stdoutStream, stderrStream);
        assertEquals("", stdoutBuffer.toString());
        assertEquals("", stderrBuffer.toString());
    }

}
