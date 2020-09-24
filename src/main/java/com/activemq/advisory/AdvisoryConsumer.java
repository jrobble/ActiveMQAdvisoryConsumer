package com.activemq.advisory;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQMessage;
import org.apache.activemq.command.DataStructure;

import javax.jms.*;
import java.io.IOException;
import java.util.Map;

public class AdvisoryConsumer implements MessageListener {

    private final String connectionUri = "tcp://localhost:61616";
    private ActiveMQConnectionFactory connectionFactory;
    private Connection connection;
    private Session session;
    private Destination destination;
    private MessageConsumer advisoryConsumer;

    public void before() throws Exception {
        connectionFactory = new ActiveMQConnectionFactory(connectionUri);
        connection = connectionFactory.createConnection();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        destination = session.createTopic(">"); // subscribe to all topics

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
        Thread.sleep(Long.MAX_VALUE);
    }

    public static void main(String[] args) {
        AdvisoryConsumer example = new AdvisoryConsumer();
        System.out.println("Starting Advisory Consumer example now...\n");
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