package com.activemq.advisory;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQMessage;
import org.apache.activemq.command.DataStructure;

import javax.jms.*;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class AdvisoryConsumer implements MessageListener {

    private final String connectionUri = "tcp://localhost:61616";
    private ActiveMQConnectionFactory connectionFactory;
    private Connection connection;
    private Session session;
    private Destination destination;
    private MessageConsumer advisoryConsumer;
    private Destination monitored;

    public void before() throws Exception {
        connectionFactory = new ActiveMQConnectionFactory(connectionUri);
        connection = connectionFactory.createConnection();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        /*
        monitored = session.createQueue("JCG_QUEUE");
        destination = session.createTopic(
                AdvisorySupport.getConsumerAdvisoryTopic(monitored).getPhysicalName() + "," +
                AdvisorySupport.getProducerAdvisoryTopic(monitored).getPhysicalName());
        */
        destination = session.createTopic(">"); // DEBUG

        advisoryConsumer = session.createConsumer(destination);
        advisoryConsumer.setMessageListener(this);
        connection.start();
    }

    public void after() throws Exception {
        if (connection != null) {
            connection.close();
        }
    }

    public void onMessage(Message message) {
        /*
        try {
            Destination source = message.getJMSDestination();
            if (source.equals(AdvisorySupport.getConsumerAdvisoryTopic(monitored))) {
                int consumerCount = message.getIntProperty("consumerCount");
                System.out.println("New Consumer Advisory, Consumer Count: " + consumerCount);
            } else if (source.equals(AdvisorySupport.getProducerAdvisoryTopic(monitored))) {
                int producerCount = message.getIntProperty("producerCount");
                System.out.println("New Producer Advisory, Producer Count: " + producerCount);
            }
        } catch (JMSException e) {
        }
        */

        if (message instanceof ActiveMQMessage) {
            try {
                ActiveMQMessage mqMessage = (ActiveMQMessage) message;
                System.out.println(mqMessage.getDestination());
                System.out.println("\t producerId: " + mqMessage.getProducerId());

                DataStructure data = mqMessage.getDataStructure();
                if (data != null) {
                    System.out.println("\t dataStruct: " + data);
                }

                Map props = mqMessage.getProperties();
                if (props != null) {
                    System.out.println("\t properties: " + props);
                }

                // System.out.println("\t" + message);
                System.out.println();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void run() throws Exception {
        TimeUnit.MINUTES.sleep(10);
    }

    public static void main(String[] args) {
        AdvisoryConsumer example = new AdvisoryConsumer();
        System.out.println("Starting Advisory Consumer example now...");
        try {
            example.before();
            example.run();
            example.after();
        } catch (Exception e) {
            System.out.println("Caught an exception during the example: " + e.getMessage());
        }
        System.out.println("Finished running the Advisory Consumer example.");
    }
}