package org.kurator.akka.actors;

import org.kurator.akka.AkkaActor;

public class PrintStreamWriter extends AkkaActor {

    public String separator = System.lineSeparator();
    public boolean endWithSeparator = false;
    private boolean isFirst = true;
    
    @Override
    public void onData(Object value) throws Exception {
        
        if (isFirst) {
            isFirst = false;
        } else {
            outStream.print(separator);
        }
        outStream.print(value);
    }
    
    @Override
    protected void onEnd() throws Exception {
        if (endWithSeparator) {
            outStream.print(separator);
        }
    }
}
