package org.keedio.flume.sink.gelf.sink;

import com.jayway.jsonpath.JsonPath;
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
 * Luis Lazaro
 * lalazaro@keedio.com
 * Jun - 2018
 */
public class GelfSink extends AbstractSink implements Configurable {

    private Logger LOG = LoggerFactory.getLogger(GelfSink.class);

    private GelfConfiguration gelfConfiguration;
    private String sinkName = this.getName();
    private GelfTransport gelfTransport = null;
    private String gelfMessageLevel = null;
    private int windowSizeSeconds;
    private String jsonFieldName;
    private String headerFiledName;
    private boolean isAggregatorEnabled;
    private GelfMessageBuilder gelfMessageBuilder = null;
    private String tagCounter;

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
     * @param context context
     */
    @Override
    public void configure(Context context) {
        String hostName = context.getString("host.name", "127.0.0.1");
        int hostPort = context.getInteger("host.port", 12201);
        String transportProtocol = context.getString("transport.protocol", "UDP");
        int queueSize = context.getInteger("queue.size", 512);
        int connectTimeout = context.getInteger("connect.timeout", 5000);
        int reconnectDelay = context.getInteger("reconnect.delay", 1000);
        boolean tcpNodelay = context.getBoolean("tcp.nodelay", true);
        int sendBufferSize = context.getInteger("send.buffer.size", 32768);
        windowSizeSeconds = context.getInteger("window.size.seconds", 20);
        jsonFieldName = context.getString("json.field.name", null);
        headerFiledName= context.getString("header.field.name", null);

        gelfMessageLevel = context.getString("gelf.message.level", "INFO");
        isAggregatorEnabled = context.getBoolean("aggregator.enabled", false);

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
        gelfTransport = GelfTransports.create(gelfConfiguration);
        gelfMessageBuilder = new GelfMessageBuilder("", gelfMessageLevel)
                .level(GelfMessageLevel.valueOf(gelfMessageLevel))
                .additionalField("sinkName", sinkName);

        if (isAggregatorEnabled) {
            new AggregatorController(gelfMessageBuilder, gelfTransport, windowSizeSeconds);
        }



        super.start();
    }

    @Override
    public synchronized void stop() {
        super.stop();
        gelfTransport.stop();
    }

    /**
     * <p>Requests the sink to attempt to consume data from attached channel</p>
     * <p><strong>Note</strong>: This method should be consuming from the channel
     * within the bounds of a Transaction. On successful delivery, the transaction
     * should be committed, and on failure it should be rolled back.
     *
     * @return READY if 1 or more Events were successfully delivered, BACKOFF if
     * no data could be retrieved from the channel feeding this sink
     */
    @Override
    public Status process() {
        Status status = Status.READY;
        Channel ch = getChannel();
        Transaction txn = ch.getTransaction();
        Event event;

        try {
            txn.begin();
            event = ch.take();

            String eventBody = new String(event.getBody());
            if (isAggregatorEnabled) {
                if ( jsonFieldName != null ) {
                    tagCounter = JsonPath.read(eventBody, "$." + jsonFieldName);
                } else if (headerFiledName != null) {
                    tagCounter = event.getHeaders().get(headerFiledName);
                } else {
                    LOG.error("No tag for counter was set, but aggregator is set to true.");
                }
                Counters.instance().incrementCounter(tagCounter);
            } else {
                Map<String, Object> headers = new HashMap<String, Object>();
                headers.putAll(event.getHeaders());
                final GelfMessage gelfMessage = gelfMessageBuilder.message(eventBody)
                        .additionalFields(headers)
                        .build();
                gelfTransport.trySend(gelfMessage);
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
