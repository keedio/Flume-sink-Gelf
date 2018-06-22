package org.keedio.flume.sink.gelf.sink;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class Counters {
  private final Object lock = new Object();
  
  private Map<String, AtomicLong> counters = new HashMap<>();

  private static Counters instance;
  
  private Counters() {
  }
  
  public static Counters instance(){
    if (instance == null){
      instance = new Counters();
    }
    
    return instance;
  }

  /**
   * Incrementa un contador.
   * 
   * @param counterName
   */
  public void incrementCounter(String counterName) {
    synchronized (lock){
      if (!counters.containsKey(counterName)){
        counters.put(counterName, new AtomicLong(1));
      } else {
        counters.get(counterName).incrementAndGet();
      }
    }
  }

  /**
   * Recupera el conjunto de contadores listos para ser serializados.
   * 
   * @return
   */
  public Map<String, AtomicLong> copyCounters(){
    synchronized (lock){
      Map<String, AtomicLong> toBeSerialized = counters;
      counters = new HashMap<>();
      
      return toBeSerialized;
    }
  }
}
