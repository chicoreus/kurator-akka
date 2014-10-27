package org.kurator.akka.actors;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.concurrent.TimeoutException;

import org.kurator.akka.ActorBuilder;
import org.kurator.akka.WorkflowBuilder;
import org.kurator.akka.messages.EndOfStream;

import junit.framework.TestCase;
import akka.actor.ActorRef;

public class TestIntegerStreamMerger_TwoInputStreams extends TestCase {

    private WorkflowBuilder wfb;
    private OutputStream outputBuffer;
    private ActorRef repeaterA;
    private ActorRef repeaterB;

     @Override
     public void setUp() {
    
         outputBuffer = new ByteArrayOutputStream();
         PrintStream printStream = new PrintStream(outputBuffer);
        
         wfb = new WorkflowBuilder();
    
         ActorBuilder repeaterABuilder = wfb.createActorBuilder()
                 .actorClass(Repeater.class);
    
         ActorBuilder repeaterBBuilder = wfb.createActorBuilder()
                 .actorClass(Repeater.class);

         ActorBuilder merge = wfb.createActorBuilder()
                 .actorClass(IntegerStreamMerger.class)
                 .parameter("streamCount", 2)
                 .listensTo(repeaterABuilder)
                 .listensTo(repeaterBBuilder);
        
         @SuppressWarnings("unused")
         ActorBuilder printer = wfb.createActorBuilder()
                 .actorClass(PrintStreamWriter.class)
                 .parameter("stream", printStream)
                 .parameter("separator", ", ")
                 .listensTo(merge);
        
         wfb.build();
         
         repeaterA = wfb.getActorForConfig(repeaterABuilder);
         repeaterB = wfb.getActorForConfig(repeaterBBuilder);
     }

     public void testIntegerStreamMerger_TwoStreams_NoValues() throws TimeoutException, InterruptedException {
         wfb.startWorkflow();
         repeaterA.tell(new EndOfStream(), wfb.root());
         repeaterB.tell(new EndOfStream(), wfb.root());
         wfb.awaitWorkflow();
         assertEquals("", outputBuffer.toString());
     }
    
     public void testIntegerStreamMerger_TwoStreams_OneEmpty_DistinctValues() throws TimeoutException, InterruptedException
     {
         wfb.startWorkflow();
         repeaterA.tell(new Integer(1), wfb.root());
         repeaterA.tell(new Integer(2), wfb.root());
         repeaterA.tell(new Integer(3), wfb.root());
         repeaterA.tell(new Integer(4), wfb.root());
         repeaterA.tell(new EndOfStream(), wfb.root());
         repeaterB.tell(new EndOfStream(), wfb.root());
         wfb.awaitWorkflow();
         assertEquals("1, 2, 3, 4", outputBuffer.toString());
     }
    
     public void testIntegerStreamMerger_TwoStreams_OneEmpty_IdenticalValues() throws TimeoutException, InterruptedException
     {
         wfb.startWorkflow();
         repeaterA.tell(7, wfb.root());
         repeaterA.tell(7, wfb.root());
         repeaterA.tell(7, wfb.root());
         repeaterA.tell(7, wfb.root());
         repeaterA.tell(new EndOfStream(), wfb.root());
         repeaterB.tell(new EndOfStream(), wfb.root());
         wfb.awaitWorkflow();
         assertEquals("7", outputBuffer.toString());
     }
    
     public void testIntegerStreamMerger_TwoStreams_OneEmpty_ValuesWithDuplicates() throws TimeoutException, InterruptedException {
         wfb.startWorkflow();
         repeaterA.tell(new Integer(1), wfb.root());
         repeaterA.tell(new Integer(2), wfb.root());
         repeaterA.tell(new Integer(2), wfb.root());
         repeaterA.tell(new Integer(3), wfb.root());
         repeaterA.tell(new Integer(3), wfb.root());
         repeaterA.tell(new Integer(4), wfb.root());
         repeaterA.tell(new Integer(5), wfb.root());
         repeaterA.tell(new Integer(5), wfb.root());
         repeaterA.tell(new EndOfStream(), wfb.root());
         repeaterB.tell(new EndOfStream(), wfb.root());
         wfb.awaitWorkflow();
         assertEquals("1, 2, 3, 4, 5", outputBuffer.toString());
     }
    
     public void testIntegerStreamMerger_TwoStreams_DistinctValues_RoundRobin() throws TimeoutException, InterruptedException {
         wfb.startWorkflow();
         repeaterA.tell(new Integer(1), wfb.root());
         repeaterB.tell(new Integer(2), wfb.root());
         repeaterA.tell(new Integer(3), wfb.root());
         repeaterB.tell(new Integer(4), wfb.root());
         repeaterA.tell(new EndOfStream(), wfb.root());
         repeaterB.tell(new EndOfStream(), wfb.root());
         wfb.awaitWorkflow();
         assertEquals("1, 2, 3, 4", outputBuffer.toString());
     }
    
     public void testIntegerStreamMerger_TwoStreams_DistinctValues_OneStreamFirst() throws TimeoutException, InterruptedException {
         wfb.startWorkflow();
         repeaterA.tell(new Integer(1), wfb.root());
         repeaterA.tell(new Integer(3), wfb.root());
         repeaterA.tell(new Integer(5), wfb.root());
         repeaterB.tell(new Integer(2), wfb.root());
         repeaterB.tell(new Integer(4), wfb.root());
         repeaterB.tell(new Integer(6), wfb.root());
         repeaterA.tell(new EndOfStream(),  wfb.root());
         repeaterB.tell(new EndOfStream(),  wfb.root());
         wfb.awaitWorkflow();
         assertEquals("1, 2, 3, 4, 5, 6", outputBuffer.toString());
     }
    
     public void testIntegerStreamMerger_TwoStreams_IdenticalValues_RoundRobin() throws TimeoutException, InterruptedException {
         wfb.startWorkflow();
         repeaterA.tell(new Integer(7), wfb.root());
         repeaterB.tell(new Integer(7), wfb.root());
         repeaterA.tell(new Integer(7), wfb.root());
         repeaterB.tell(new Integer(7), wfb.root());
         repeaterA.tell(new EndOfStream(), wfb.root());
         repeaterB.tell(new EndOfStream(), wfb.root());
         wfb.awaitWorkflow();
         assertEquals("7", outputBuffer.toString());
     }
    
     public void testIntegerStreamMerger_TwoStreams_IdenticalValues_OneStreamFirst() throws TimeoutException, InterruptedException {
         wfb.startWorkflow();
         repeaterA.tell(new Integer(7), wfb.root());
         repeaterA.tell(new Integer(7), wfb.root());
         repeaterA.tell(new Integer(7), wfb.root());
         repeaterB.tell(new Integer(7), wfb.root());
         repeaterB.tell(new Integer(7), wfb.root());
         repeaterB.tell(new Integer(7), wfb.root());
         repeaterA.tell(new EndOfStream(), wfb.root());
         repeaterB.tell(new EndOfStream(), wfb.root());
         wfb.awaitWorkflow();
         assertEquals("7", outputBuffer.toString());
     }
    
     public void testIntegerStreamMerger_TwoStreams_ValuesWithDuplicates_RoundRobin() throws TimeoutException, InterruptedException {
         wfb.startWorkflow();
         repeaterA.tell(new Integer(1), wfb.root());
         repeaterB.tell(new Integer(2), wfb.root());
         repeaterA.tell(new Integer(2), wfb.root());
         repeaterB.tell(new Integer(3), wfb.root());
         repeaterA.tell(new Integer(3), wfb.root());
         repeaterB.tell(new Integer(4), wfb.root());
         repeaterA.tell(new Integer(5), wfb.root());
         repeaterB.tell(new Integer(5), wfb.root());
         repeaterA.tell(new EndOfStream(), wfb.root());
         repeaterB.tell(new EndOfStream(), wfb.root());
         wfb.awaitWorkflow();
         assertEquals("1, 2, 3, 4, 5", outputBuffer.toString());
     }
    
     public void testIntegerStreamMerger_TwoStreams_ValuesWithDuplicates_OneStreamFirst() throws TimeoutException, InterruptedException {
         wfb.startWorkflow();
         repeaterA.tell(new Integer(1), wfb.root());
         repeaterA.tell(new Integer(2), wfb.root());
         repeaterA.tell(new Integer(2), wfb.root());
         repeaterA.tell(new Integer(3), wfb.root());
         repeaterB.tell(new Integer(3), wfb.root());
         repeaterB.tell(new Integer(4), wfb.root());
         repeaterB.tell(new Integer(5), wfb.root());
         repeaterB.tell(new Integer(5), wfb.root());
         repeaterA.tell(new EndOfStream(), wfb.root());
         repeaterB.tell(new EndOfStream(), wfb.root());
         wfb.awaitWorkflow();
         assertEquals("1, 2, 3, 4, 5", outputBuffer.toString());
     }

}
