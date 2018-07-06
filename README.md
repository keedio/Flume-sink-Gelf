# Flume-sink-Gelf

**Flume-sink-Gelf**  is a custom Flume sink component for sending events via GELF protocol.
The sink's process dispatches GEL messages using [GELF Client](https://github.com/Graylog2/gelfclient).


## Compilation and packaging
1.**Clone the project:**
```
git clone https://github.com/keedio/Flume-sink-Gelf.gti
```

2.**Build with Maven:**
```
mvn clean package
```

### Deployment and launching ###

1. **[Create plugins.d directory](https://flume.apache.org/FlumeUserGuide.html#the-plugins-d-directory).**
2. **[Directory layout for plugins](https://flume.apache.org/FlumeUserGuide.html#directory-layout-for-plugins):**

    ```
    $ cd plugins.d
    $ mkdir flume-sink-gelf
    $ cd flume-sink-gelf
    $ mkdir lib
    $ cp flume-sink-gelf.jar /lib
     ```

3. **Create a config file, agent example**
````
# www.keedio.com

#ACTIVE LIST
#   agent.sinks = k1
#   agent.sinks.k1.type = org.keedio.flume.sink.gelf.sink.GelfSink
#   agent.sinks.k1.host.name = 192.168.1.100
#   agent.sinks.k1.host.port = 12201

##end of sink configuration for Agent 'agent'
````


### Configurable parameters

|Parameter|Description|mandatory|default|observations|
|------|-----------|---|----|---|
|host.name|ip server |yes|-|Graylog server|
|host.port| port to use for connection|no|12201|-|
|transport.protocol|protocol transport layer|no|UDP|TCP or UDP|
|queue.size|queue size |no|512|-|
|connect.timeout|timeout for connection|no|5000|milisencods|
|reconnect.delay|wait for delay to reconnect|no|1000|milisencods|
|tcp.nodelay|socket option for sending data as soon as it's avalaible|no|true|Enabling TCP_NODELAY forces a socket to send the data in its buffer, whatever the packet size.|
|send.buffer.size| buffer size |no| 32768|-|
|gelf.message.level|informational level |no| INFO|TRACE, DEBUG,...|
|window.size.seconds|seconds for each aggregation window |no| 20|seconds|
|json.field.name|name of the key containing source name |no| extraData|String|

### Version history #####
- 0.1.1
    + Fix bug: Flume agent runs out of memory due to multiple connections. Reuse of GelfTransport and GelfMessageBuilder and initialize on sink start.
    + Improve: create GelfMessage with String of event body instead of array of bytes.

- 0.1.0:
    + First stable release.




