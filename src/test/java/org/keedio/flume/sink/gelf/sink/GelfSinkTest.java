package org.keedio.flume.sink.gelf.sink;

import com.google.common.base.Charsets;
import org.apache.flume.*;
import org.apache.flume.channel.MemoryChannel;
import org.apache.flume.channel.PseudoTxnMemoryChannel;
import org.apache.flume.conf.Configurables;
import org.apache.flume.event.EventBuilder;
import org.apache.flume.interceptor.Interceptor;
import org.apache.flume.interceptor.InterceptorBuilderFactory;
import org.apache.flume.interceptor.InterceptorType;
import org.apache.flume.lifecycle.LifecycleException;
import org.apache.flume.lifecycle.LifecycleState;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.fail;

public class GelfSinkTest {

  private GelfSink sink;

  @Before
  public void setup(){
    sink = new GelfSink();
  }

  /**
   * Lack of exception test.
   */
  @Test
  public void testAppend() throws InterruptedException, LifecycleException,
          EventDeliveryException {

    Channel channel = new PseudoTxnMemoryChannel();
    Context context = new Context();
    Configurables.configure(channel, context);
    Configurables.configure(sink, context);

    sink.setChannel(channel);
    sink.start();
    Assert.assertEquals(LifecycleState.START, sink.getLifecycleState());

    for (int i = 0; i < 10; i++) {
      Event event = EventBuilder.withBody(("Test " + i).getBytes());
      channel.put(event);
      Sink.Status status = sink.process();
      Assert.assertEquals(status, Sink.Status.READY);
    }

    sink.stop();
    Assert.assertEquals(LifecycleState.STOP, sink.getLifecycleState());
  }


}
