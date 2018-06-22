package org.keedio.flume.sink.gelf.sink;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class AggregatorController implements AutoCloseable {
  private final Logger LOG = LoggerFactory.getLogger(getClass());
  private ScheduledExecutorService executorService = null;

  public AggregatorController() {
    executorService = Executors.newSingleThreadScheduledExecutor();
    LOG.info("Starting AggregatorController thread");
    
    // TODO: pasar el delay por configuración
    executorService.scheduleAtFixedRate(new Aggregator(), 1, 60, TimeUnit.SECONDS);
  }

  @Override
  public void close() throws Exception {
    LOG.info("Interrupting AggregatorController thread");
    executorService.shutdown();
  }

  class Aggregator implements Runnable{

    @Override
    public void run() {
      Map<String, AtomicLong> toBeSerialized = Counters.instance().copyCounters();
      
      // TODO
    }
  }
}
