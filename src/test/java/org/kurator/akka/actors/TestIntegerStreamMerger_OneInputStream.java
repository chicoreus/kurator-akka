package org.kurator.akka.actors;

import org.kurator.akka.ActorBuilder;
import org.kurator.akka.KuratorAkkaTestCase;
import org.kurator.akka.WorkflowRunner;
import org.kurator.akka.messages.EndOfStream;

public class TestIntegerStreamMerger_OneInputStream extends KuratorAkkaTestCase {

    private WorkflowRunner wfb;

     @Override
     public void setUp() throws Exception {

         super.setUp();
         
         wfb = new WorkflowRunner()
             .outputStream(stdoutStream)
             .errorStream(stderrStream);
    
         ActorBuilder repeater = wfb.createActorBuilder()
                 .actorClass(Repeater.class);
    
         ActorBuilder merge = wfb.createActorBuilder()
                 .actorClass(IntegerStreamMerger.class)
                 .parameter("streamCount", 1)
                 .listensTo(repeater);
        
         @SuppressWarnings("unused")
         ActorBuilder printer = wfb.createActorBuilder()
                 .actorClass(PrintStreamWriter.class)
                 .parameter("separator", ", ")
                 .listensTo(merge);
        
         wfb.inputActor(repeater);
         
         wfb.build();
     }
     
    public void testIntegerStreamMerger_NoValues() throws Exception {
        wfb.start();
        wfb.tellWorkflow(new EndOfStream());
        wfb.await();
        assertEquals("", stdoutBuffer.toString());
        assertEquals("", stderrBuffer.toString());
    }
     
     public void testIntegerStreamMerger_DistinctValues() throws Exception {
         wfb.start();
         wfb.tellWorkflow(1);
         wfb.tellWorkflow(2);
         wfb.tellWorkflow(3);
         wfb.tellWorkflow(4);
         wfb.tellWorkflow(new EndOfStream());
         wfb.await();
         assertEquals("1, 2, 3, 4", stdoutBuffer.toString());
         assertEquals("", stderrBuffer.toString());
     }
    
     public void testIntegerStreamMerger_IdenticalValues() throws Exception {
         wfb.start();
         wfb.tellWorkflow(7);
         wfb.tellWorkflow(7);
         wfb.tellWorkflow(7);
         wfb.tellWorkflow(7);
         wfb.tellWorkflow(new EndOfStream());
         wfb.await();
         assertEquals("7", stdoutBuffer.toString());
         assertEquals("", stderrBuffer.toString());
     }
    
     public void testIntegerStreamMerger_ValuesWithDuplicates() throws Exception {
         wfb.start();
         wfb.tellWorkflow(1);
         wfb.tellWorkflow(2);
         wfb.tellWorkflow(2);
         wfb.tellWorkflow(3);
         wfb.tellWorkflow(3);
         wfb.tellWorkflow(4);
         wfb.tellWorkflow(5);
         wfb.tellWorkflow(5);
         wfb.tellWorkflow(new EndOfStream());
         wfb.await();
         assertEquals("1, 2, 3, 4, 5", stdoutBuffer.toString());
         assertEquals("", stderrBuffer.toString());
     }
}
