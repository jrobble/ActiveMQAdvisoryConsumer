package com.activemq.advisory;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQMessage;
import org.apache.commons.cli.*;

import javax.jms.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AdvisoryConsumer implements MessageListener {

    private static final String DEFAULT_BROKER_URI = "tcp://localhost:61616";

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss,SSS");

    private String brokerUri;
    private List<String> queues;
    private boolean verbose;

    private ActiveMQConnectionFactory connectionFactory;
    private Connection connection;
    private Session session;
    private Destination destination;
    private MessageConsumer advisoryConsumer;

    public AdvisoryConsumer(String brokerUri, List<String> queues, boolean verbose) {
        this.brokerUri = brokerUri;
        this.queues = queues;
        this.verbose = verbose;
    }

    public void before() throws Exception {
        connectionFactory = new ActiveMQConnectionFactory(brokerUri);
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
                String dest = mqMessage.getDestination().getQualifiedName();

                if (!queues.isEmpty()) {
                    if (!queues.stream().anyMatch(q -> dest.endsWith(q))) {
                        return; // ignore message
                    }
                }

                System.out.println(dateTimeFormatter.format(LocalDateTime.now()));
                System.out.println(dest);
                System.out.println("\tproducerId: " + mqMessage.getProducerId());

                if (verbose) {
                    /*
                    DataStructure data = mqMessage.getDataStructure();
                    if (data != null) {
                        System.out.println("\t dataStruct: " + data);
                    }

                    Map props = mqMessage.getProperties();
                    if (props != null) {
                        System.out.println("\t properties: " + props);
                    }
                    */
                    System.out.println("\t" + message);
                }

                System.out.println();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void run() throws Exception {
        Thread.sleep(Long.MAX_VALUE);
    }

    public static void main(String[] args) {
        Options options = new Options();

        Option brokerOpt = new Option("b", "broker", true, "ActiveMQ broker URI. Default: " + DEFAULT_BROKER_URI);
        brokerOpt.setRequired(false);
        options.addOption(brokerOpt);

        Option queueOpt = new Option("q", "queue", true, "queue to monitor");
        queueOpt.setRequired(false);
        options.addOption(queueOpt);

        Option verboseOpt = new Option("v", "verbose", false, "be verbose");
        verboseOpt.setRequired(false);
        options.addOption(verboseOpt);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("ActiveMQAdvisoryConsumer", options);
            System.exit(1);
        }

        System.out.println("Command line options:");

        String brokerUri = cmd.getOptionValue("b", DEFAULT_BROKER_URI);
        System.out.println("\tbroker URI = " + brokerUri);

        List queues = new ArrayList<String>();
        if (cmd.hasOption("q")) {
            queues = Arrays.asList(cmd.getOptionValues("q"));
            System.out.println("\tqueues = " + queues);
        } else {
            System.out.println("\tqueues = <all>");
        }

        boolean verbose = cmd.hasOption("v");
        System.out.println("\tverbose = " + verbose);

        AdvisoryConsumer example = new AdvisoryConsumer(brokerUri, queues, verbose);
        System.out.println("\nStarting Advisory Consumer example now...\n");
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