package org.kurator.akka.actors;

public class PrintStreamWriter extends AkkaActor {

    public String separator = System.lineSeparator();    
    private boolean isFirst = true;
    
    @Override
    public void handleData(Object value) throws Exception {
        
        if (isFirst) {
            isFirst = false;
        } else {
            outStream.print(separator);
        }
        outStream.print(value);
    }
}
