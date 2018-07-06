package org.keedio.flume.sink.gelf.sink;

import org.apache.flume.*;
import org.apache.flume.channel.MemoryChannel;
import org.apache.flume.conf.Configurables;
import org.apache.flume.event.EventBuilder;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.fail;

public class GelfSinkTest {
  @Test
  public void testDefaultBehavior() {
    GelfSink sink = new GelfSink();
    Context context = new Context();
    Configurables.configure(sink, context);
    Channel memoryChannel = new MemoryChannel();
    Configurables.configure(memoryChannel, context);
    sink.setChannel(memoryChannel);
    sink.start();

    String msg = "this is a message";
    Transaction tx = memoryChannel.getTransaction();
    tx.begin();
    Event event = EventBuilder.withBody(msg.getBytes());
    memoryChannel.put(event);
    tx.commit();
    tx.close();
    Sink.Status status = sink.process();
    if (status == Sink.Status.BACKOFF) {
      fail("Error Occurrred");
    }

    String fetchedMsg = new String(event.getBody());
    Assert.assertEquals(msg, fetchedMsg);

  }
}
