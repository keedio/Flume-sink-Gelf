package org.keedio.flume.sink.gelf.sink;

import org.graylog2.gelfclient.GelfMessage;
import org.graylog2.gelfclient.GelfMessageBuilder;
import org.graylog2.gelfclient.transport.GelfTransport;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class AggregatorController implements AutoCloseable {
  private final Logger LOG = LoggerFactory.getLogger(getClass());
  private ScheduledExecutorService executorService;
  private GelfMessageBuilder gelfMessageBuilder;
  private GelfTransport gelfTransport;

  AggregatorController(GelfMessageBuilder gelfMessageBuilder, GelfTransport gelfTransport, int windowSizeSeconds) {
    executorService = Executors.newSingleThreadScheduledExecutor();
    this.gelfMessageBuilder = gelfMessageBuilder;
    this.gelfTransport = gelfTransport;
    LOG.info("Starting AggregatorController thread");
    executorService.scheduleAtFixedRate(new Aggregator(), 1, windowSizeSeconds, TimeUnit.SECONDS);
  }


  @Override
  public void close() {
    LOG.info("Interrupting AggregatorController thread");
    executorService.shutdown();
  }

  class Aggregator implements Runnable {

    @Override
    public void run() {
      Map<String, AtomicLong> toBeSerialized = Counters.instance().copyCounters();

      if (toBeSerialized != null && !toBeSerialized.isEmpty()) {
        String eventBody = new JSONObject(toBeSerialized).toString();
        LOG.info("Sending to GrayLog: " + eventBody);
        final GelfMessage gelfMessage = gelfMessageBuilder.message(eventBody).build();
        gelfTransport.trySend(gelfMessage);
      }
    }
  }
}
