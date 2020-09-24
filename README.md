# Configure ActiveMQ

Refer to http://activemq.apache.org/advisory-message.html.

Here's an example of enabling advisory topics for queues. This is an excerpt from `/opt/activemq/conf/activemq.xml`. Note the addition of the attributes starting with `advisoryForConsumed="true"`.

```
        <broker xmlns="http://activemq.apache.org/schema/core" brokerName="localhost" dataDirectory="${activemq.data}">
            <destinationPolicy>
                <policyMap>
                  <policyEntries>
                    <policyEntry queue=">"
                      prioritizedMessages="true" useCache="false" expireMessagesPeriod="0" queuePrefetch="1"
                      advisoryForConsumed="true"
                      advisoryForDelivery="true"
                      advisoryForDiscardingMessages="true"
                      advisoryWhenFull="true"
                      sendAdvisoryIfNoConsumers="true">
```

# Build and run

```
cd <path>/ActiveMQAdvisoryConsumer
mvn install
java -jar target/mpf-activemq-advisory-consumer-0.0.1-jar-with-dependencies.jar
```

# Usage

```
usage: ActiveMQAdvisoryConsumer
 -b,--broker <arg>   ActiveMQ broker URI. Default: tcp://localhost:61616
 -q,--queue <arg>    queue to monitor
 -v,--verbose        be verbose
```

By default, the tool subscribes to all topics. To subscribe to topics for specific queues use `-q`:

```
java -jar target/mpf-activemq-advisory-consumer-0.0.1-jar-with-dependencies.jar -q MPF.DECTION_FACECV_REQUEST -q MPF.DETECTION_SUBSENSE_REQUEST
```