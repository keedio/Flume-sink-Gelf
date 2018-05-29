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
#   agent.sinks.k2.type = org.keedio.flume.sink.gelf.sink.GelfSink
#   agent.sinks.k2.host.name = 192.168.1.100
#   agent.sinks.k2.host.port = 12201

##end of sink configuration for Agent 'agent'
````


### Configurable parameters

|Parameter|Description|mandatory|default|observations|
|------|-----------|---|----|---|
|host.name|ip server |yes|-|Graylog server|
|host.port| port to use for connection|no|12201|-|
