package org.keedio.flume.sink.gelf.sink;

import org.apache.flume.*;
import org.apache.flume.conf.Configurable;
import org.apache.flume.sink.AbstractSink;
import org.graylog2.gelfclient.*;
import org.graylog2.gelfclient.transport.GelfTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * Hello world!
 */
public class GelfSink extends AbstractSink implements Configurable {

  private Logger LOG = LoggerFactory.getLogger(GelfSink.class);

  private GelfConfiguration gelfConfiguration;
  private String sinkName = this.getName();
  private Context context;

  /**
   * <p>
   * Request the implementing class to (re)configure itself.
   * </p>
   * <p>
   * When configuration parameters are changed, they must be
   * reflected by the component asap.
   * </p>
   * <p>
   * There are no thread safety guarantees on when configure might be called.
   * </p>
   *
   * @param context
   */
  @Override
  public void configure(Context context) {
    this.context = context;
    String hostName = context.getString("host.name", "127.0.0.1");
    int hostPort = context.getInteger("host.port", 12201);
    String transportProtocol = context.getString("transport.protocol", "UDP");
    int queueSize = context.getInteger("queue.size", 512);
    int connectTimeout = context.getInteger("connect.timeout", 5000);
    int reconnectDelay = context.getInteger("reconnect.delay", 1000);
    boolean tcpNodelay = context.getBoolean("tcp.nodelay", true);
    int sendBufferSize = context.getInteger("send.buffer.size", 32768);

    gelfConfiguration = new GelfConfiguration(new InetSocketAddress(hostName, hostPort))
      .transport(GelfTransports.valueOf(transportProtocol))
      .queueSize(queueSize)
      .connectTimeout(connectTimeout)
      .reconnectDelay(reconnectDelay)
      .tcpNoDelay(tcpNodelay)
      .sendBufferSize(sendBufferSize);

  }

  @Override
  public synchronized void start() {
    super.start();
  }

  @Override
  public synchronized void stop() {
    super.stop();
  }

  /**
   * <p>Requests the sink to attempt to consume data from attached channel</p>
   * <p><strong>Note</strong>: This method should be consuming from the channel
   * within the bounds of a Transaction. On successful delivery, the transaction
   * should be committed, and on failure it should be rolled back.
   *
   * @return READY if 1 or more Events were successfully delivered, BACKOFF if
   * no data could be retrieved from the channel feeding this sink
   * @throws EventDeliveryException In case of any kind of failure to
   *                                deliver data to the next hop destination.
   */
  @Override
  public Status process() throws EventDeliveryException {
    String gelfMessageLevel = context.getString("gelf.message.level", "INFO");
    Status status = null;
    final GelfTransport gelfTransport = GelfTransports.create(gelfConfiguration);
    final GelfMessageBuilder gelfMessageBuilder = new GelfMessageBuilder("", gelfMessageLevel)
      .level(GelfMessageLevel.valueOf(gelfMessageLevel))
      .additionalField("sinkName", this.getName());

    boolean blocking = false;

    Channel ch = getChannel();
    Transaction txn = ch.getTransaction();
    txn.begin();
    try {
      Event event = ch.take();
      byte[] payload = event.getBody();
      Map<String,Object> headers = new HashMap<String, Object>();
      headers.putAll(event.getHeaders());
      final GelfMessage gelfMessage = gelfMessageBuilder.message(new String(payload))
        .additionalFields(headers)
        .build();
      if (blocking) {
        // Blocks until there is capacity in the queue
        gelfTransport.send(gelfMessage);
      } else {
        // Returns false if there isn't enough room in the queue
        boolean enqueued = gelfTransport.trySend(gelfMessage);
      }
      status = Status.READY;
      txn.commit();

    } catch (Throwable t) {
      txn.rollback();
      // Log exception, handle individual exceptions as needed
      status = Status.BACKOFF;

    } finally {
      if (txn != null) {
        txn.close();
      }
    }

    return status;
  }

}
