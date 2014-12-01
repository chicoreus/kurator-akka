package org.kurator.akka;

import java.util.concurrent.TimeoutException;

import org.kurator.akka.ActorBuilder;
import org.kurator.akka.WorkflowBuilder;
import org.kurator.akka.actors.Transformer;
import org.kurator.akka.actors.PrintStreamWriter;
import org.kurator.akka.actors.Repeater;
import org.kurator.akka.messages.EndOfStream;

public class TestWorkflowBuilder_ActorException extends KuratorAkkaTestCase {

    private WorkflowBuilder wfb;

     @Override
     public void setUp() {
    
         super.setUp();
         
         wfb = new WorkflowBuilder()
             .outputStream(outPrintStream)
             .errorStream(errPrintStream);

         ActorBuilder repeater = wfb.createActorBuilder()
                 .actorClass(Repeater.class);
    
         ActorBuilder testActor = wfb.createActorBuilder()
                 .actorClass(TestActor.class)
                 .listensTo(repeater);
        
         @SuppressWarnings("unused")
         ActorBuilder printer = wfb.createActorBuilder()
                 .actorClass(PrintStreamWriter.class)
                 .parameter("separator", ", ")
                 .listensTo(testActor);
        
         wfb.inputActor(repeater);
         
         wfb.build();
     }
     
     public void testWorkflowBuilder_NoActorException() throws TimeoutException, InterruptedException {
         wfb.startWorkflow();
         wfb.tellWorkflow(1);
         wfb.tellWorkflow(2);
         wfb.tellWorkflow(3);
         wfb.tellWorkflow(4);
         wfb.tellWorkflow(5);
         wfb.tellWorkflow(new EndOfStream());
         wfb.awaitWorkflow();

         assertEquals("1, 2, 3, 4, 5", stdOutputBuffer.toString());
         assertEquals("", errOutputBuffer.toString());
     }
     
     public void testWorkflowBuilder_ActorException() throws TimeoutException, InterruptedException {
         wfb.startWorkflow();
         wfb.tellWorkflow(1);
         wfb.tellWorkflow(2);
         wfb.tellWorkflow(3);
         wfb.tellWorkflow(TestActor.exceptionTriggerValue);
         wfb.tellWorkflow(4);
         wfb.tellWorkflow(5);
         wfb.tellWorkflow(new EndOfStream());
         wfb.awaitWorkflow();
         
         assertEquals("1, 2, 3", stdOutputBuffer.toString());
         assertTrue(errOutputBuffer.toString().contains("Exception trigger value was sent to actor"));
     }
     
     public static class TestActor extends Transformer {

         static public final Integer exceptionTriggerValue = Integer.MIN_VALUE;
         
         @Override
         public void handleDataMessage(Object message) throws Exception {

             if (message instanceof Integer) {
                 if (((Integer)message) == exceptionTriggerValue) {
                     throw new Exception("Exception trigger value was sent to actor");
                 }
                 broadcast(message);
             }
         }
     }
}
