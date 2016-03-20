package org.kurator.akka.metadata;

import org.kurator.akka.KuratorActor;
import org.kurator.akka.messages.WrappedMessage;

public class BroadcastEventCounter implements MetadataWriter {

    int count = 0;
    
    @Override
    public void writeMetadata(KuratorActor actor, WrappedMessage wrappedMessage) {
        MetadataItem m = new MessageSendEvent(actor, ++count);
        wrappedMessage.addMetadata(m);
//        System.out.println(count);
    }
}
