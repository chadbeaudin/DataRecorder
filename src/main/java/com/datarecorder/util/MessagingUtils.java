package com.datarecorder.util;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.Topic;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.util.IndentPrinter;
import org.slf4j.Logger;

import com.datarecorder.LoggingObject;

/**
 * @author Chad Beaudin
 * 
 *         This class provides several convience methods for connecting to a JMS Broker. There is one session and one
 *         connection for each instance of this class. It currently does not support having multiple sessions per
 *         connection, so if you need multiple sessions you will need to create multiple instantiations of this class.
 * 
 */
public class MessagingUtils {

    private Destination destination;
    private boolean transacted = false;
    private int ackMode = Session.AUTO_ACKNOWLEDGE;
    private long timeToLive;
    private String DEFAULT_JMX_SERVICE_URL = "service:jmx:rmi:///jndi/rmi://localhost:1099/jmxrmi";
    private String ACTIVE_MQ_SERVER_URL = "java.naming.provider.url";
    private String ACTIVE_MQ_INIT_CNTXT = "org.apache.activemq.jndi.ActiveMQInitialContextFactory";
    private Session producerSession;
    private Connection connection;
    private InitialContext context;
    private ConnectionFactory factory;
    private ActiveMQConnection activeMqConn;
    private String fullConnectionURL;

    private Logger logger = LoggingObject.getLogger(this.getClass());

    /**
     * This method uses JNDI to connect to an ActiveMQ broker. The jndi.properties file must be in the classpath with
     * the following lines where localhost is the broker you wish to connect to:
     * 
     * java.naming.factory.initial = org.apache.activemq.jndi.ActiveMQInitialContextFactory
     * java.naming.provider.url=tcp://localhost:61616
     */
    public MessagingUtils() {
        try {
            context = new InitialContext();
            factory = (ConnectionFactory) context.lookup("ConnectionFactory");
            connection = factory.createConnection();
            producerSession = connection.createSession(transacted, ackMode);
            connection.start();
            activeMqConn = (ActiveMQConnection) connection;
            logger.debug("Created initial context and connection factory...");
        } catch (final NamingException e) {
            logger.error("Error looking up Connection Factory.", e);
        } catch (final JMSException e) {
            logger.error("Error creating connection.", e);
        }
    }

    /**
     * This method connects to an ActiveMQ broker given the broker's URL. There is no need for the jndi file to use this
     * constructor. The URL must be full, for example, tcp://localhost:61616 or failover://(tcp://localhost:61616)
     * 
     * @param brokerURL
     *            The broker's URL in the format of tcp://host:port
     * @throws JMSException
     *             Thrown if there was a connection problem.
     * @throws NamingException
     */
    public MessagingUtils(final String brokerURL) throws JMSException, NamingException {
        try {
            fullConnectionURL = brokerURL;
            final Hashtable<String, String> env = new Hashtable<String, String>();

            env.put(Context.INITIAL_CONTEXT_FACTORY, ACTIVE_MQ_INIT_CNTXT);
            env.put(ACTIVE_MQ_SERVER_URL, brokerURL);

            context = new InitialContext(env);
            factory = (ConnectionFactory) context.lookup("ConnectionFactory");
            connection = factory.createConnection();
            producerSession = connection.createSession(transacted, ackMode);
            connection.start();
            activeMqConn = (ActiveMQConnection) connection;
            logger.debug("Created initial context and connection factory...");
        } catch (final NamingException e) {
            logger.error("Error looking up Connection Factory.", e);
            throw e;
        } catch (final JMSException e) {
            logger.error("Error creating connection.", e);
            throw e;
        }
    }

    /**
     * This method will create a MessageProducer for you with the destination being the Subject that you hand in. The
     * MessageProducer will be either a TopicProducer or a QueueSender.
     * 
     * @param subject
     *            The name of the Topic/Queue that you want to connect to. The Topic/Queue will be created if it does
     *            not already exist
     * @param isQueue
     *            True if Queue, false if Topic
     * @param durable
     *            True if you want the Topic/Queue to be durable/persistent
     * @return MessageProducer
     */
    public MessageProducer createProducer(final String subject, final boolean isQueue, final boolean durable) {
        // if (subject != null) {
        // this.subject = subject;
        // }

        MessageProducer producer = null;

        try {
            if (isQueue) {
                destination = producerSession.createQueue(subject);
                logger.debug("Creating a producer Queue");
            } else {
                destination = producerSession.createTopic(subject);
                logger.debug("Creating a producer Topic");
            }
            producer = producerSession.createProducer(destination);
            if (durable) {
                producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            } else {
                producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            }
            if (timeToLive != 0)
                producer.setTimeToLive(timeToLive);

        } catch (final JMSException e) {
            logger.error("Error creating destination: ", subject);
            logger.error("Stacktrace.", e);
        }
        return producer;
    }

    /**
     * This method will create a subscription to a topic or queue with the destination being the Subject that you hand
     * in. The MessageConsumer will either be a TopicConsumer or a QueueReceiver
     * 
     * @param subject
     *            Name of the Topic/Queue you want to connect to. The Topic/Queue will be created if it does not already
     *            exist
     * @param messageListener
     *            Instance of MessageListener to be registerd as a listener to the Topic/Queue
     * @param isQueue
     *            True if Queue, false if Topic
     * @param durable
     *            True if you want the Topic/Queue to be durable/persistent
     * @return Session object
     * @throws JMSException
     */
    public Session createConsumer(final String subject, final MessageListener messageListener, final boolean isQueue,
            final boolean durable) throws JMSException {
        logger.debug("Called Create Consumer with the following parameters:" + "\n Subject= " + subject + "\n Queue= "
                + isQueue + "\n Durable= " + durable);
        MessageConsumer consumer = null;

        // Create a new session for every subscriber
        final Session subscriberSession = connection.createSession(transacted, ackMode);
        if (messageListener != null) {
            if (isQueue) {// Queue
                destination = subscriberSession.createQueue(subject);
                consumer = subscriberSession.createConsumer(destination);
                logger.debug("Creating a consumer Queue");
            } else if (durable && !isQueue) {// Durable Topic
                destination = subscriberSession.createTopic(subject);
                consumer = subscriberSession.createDurableSubscriber((Topic) destination, "UniqueSessionName");
                logger.debug("Creating a consumer durable topic");
            } else if (!isQueue) { // Just a Topic
                destination = subscriberSession.createTopic(subject);
                consumer = subscriberSession.createConsumer(destination);
                logger.debug("Creating a consumer topic");
            }

            consumer.setMessageListener(messageListener);
        }

        return subscriberSession;
    }

    /**
     * This method will create a subscription to a topic or queue with the destination being the Subject that you hand
     * in. The MessageConsumer will either be a TopicConsumer or a QueueReceiver. This method provides the ability to
     * specify a selector. Selectors are defined using SQL 92 syntax and typically apply to message headers; whether the
     * standard properties available on a JMS message or custom headers you can add via the JMS code.
     * 
     * @param subject
     *            Name of the Topic/Queue you want to connect to.
     * @param selector
     *            The selector expression for the consumer.
     * @param messageListener
     *            Instance of MessageListener to be registerd as a listener to the Topic/Queue
     * @param isQueue
     *            True if Queue, false if Topic
     * @param durable
     *            True if you want the Topic/Queue to be durable/persistent
     * @return Session object
     * @throws JMSException
     */
    public Session createConsumer(final String subject, final String selector, final MessageListener messageListener,
            final boolean isQueue, final boolean durable) throws JMSException {

        logger.debug("Called Create Consumer(w/selector) with the following parameters:" + "\n Subject= " + subject
                + "\n Selector= " + selector + "\n Queue= " + isQueue + "\n Durable= " + durable);
        MessageConsumer consumer = null;

        // Create a new session for every subscriber
        final Session subscriberSession = connection.createSession(transacted, ackMode);

        if (messageListener != null) {
            if (isQueue) {// Queue
                destination = subscriberSession.createQueue(subject);
                consumer = subscriberSession.createConsumer(destination);
            } else if (durable && !isQueue) {// Durable Topic
                if (selector != null && selector.length() > 0) {
                    destination = subscriberSession.createTopic(subject);
                    consumer = subscriberSession.createDurableSubscriber((Topic) destination, "UniqueSessionName",
                            selector, false);
                } else {
                    destination = subscriberSession.createTopic(subject);
                    consumer = subscriberSession.createDurableSubscriber((Topic) destination, "UniqueSessionName");
                }
            } else if (!isQueue) {// Topic
                if (selector != null && selector.length() > 0) {
                    destination = subscriberSession.createTopic(subject);
                    consumer = subscriberSession.createConsumer(destination, selector);
                } else {
                    destination = subscriberSession.createTopic(subject);
                    consumer = subscriberSession.createConsumer(destination);
                }
            }

            consumer.setMessageListener(messageListener);
        }

        return subscriberSession;
    }

    /**
     * Return the URL of the connection.
     * 
     * @return The URL the connection in the form tcp://host:port. (the optional prefixes, such as failover://, are not
     *         present)
     */
    public String getBrokerURL() {
        return activeMqConn.getBrokerInfo().getBrokerURL();
    }

    /**
     * Return the String used when the connection was created, for examle, failover://(tcp://localhost:61616). This is
     * because it may be desired to be known and the getURL only returns host:port.
     * 
     * @return The full string used when the connection was created.
     */
    public String getBrokerFullURL() {
        return fullConnectionURL;
    }

    /**
     * Gets the topic names (non-Advisory topics) from the broker connection. The ActiveMQ broker must have JMX turned
     * on. If this method is called, the jmx connection will be with the default URL; for example,
     * service:jmx:rmi:///jndi/rmi://localhost:1099/jmxrmi
     *
     * @return List A list of the topic names.
     */
    public List<String> getTopicNames() {
        return getTopicNames(DEFAULT_JMX_SERVICE_URL);

    }

    /**
     * Gets the topic names (non-Advisory topics) from the broker connection. The ActiveMQ broker must have JMX turned
     * on.
     *
     * @param jmxURL
     *            The URL to the JMX service. For example, the default URL is
     *            service:jmx:rmi:///jndi/rmi://localhost:1099/jmxrmi If the jmxURL is null or empty, then the default
     *            url is used.
     *
     * @return List A list of the topic names.
     **/
    public List<String> getTopicNames(String jmxURL) {
        final List<String> tempList = new ArrayList<String>();
        if ((jmxURL == null) || (jmxURL.length() == 0))
            jmxURL = DEFAULT_JMX_SERVICE_URL;

        JMXServiceURL addr = null;
        JMXConnector jc = null;
        MBeanServerConnection serverConn = null;
        try {
            addr = new JMXServiceURL(jmxURL);
            final HashMap<String, String> env = new HashMap<String, String>();
            env.put("com.sun.jmx.remote.connect.timeout", "1000");
            jc = JMXConnectorFactory.connect(addr, env);
            jc.connect();
            serverConn = jc.getMBeanServerConnection();
            final ObjectName objectName = new ObjectName(
                    "org.apache.activemq:Type=Broker,BrokerName=" + activeMqConn.getBrokerName());

            /**
             * When upgrading from AMQ 5.7 core to 5.13 client, this got a compilation error. I didn't spend any time on
             * it because it is not used anywhere. It can be addressed later. -Chad Beaudin
             */
            // final BrokerViewMBean mbroker = MBeanServerInvocationHandler.newProxyInstance(serverConn, objectName,
            // BrokerViewMBean.class, true);

            // for (int i = 0; i < mbroker.getTopics().length; i++) {
            // if (!mbroker.getTopics()[i].getKeyProperty("Destination").startsWith("ActiveMQ"))
            // tempList.add(mbroker.getTopics()[i].getKeyProperty("Destination"));
            // }
            jc.close();
        } catch (final MalformedURLException e5) {
            logger.error("Error creating JMX service.", e5);
        } catch (final IOException e) {
            logger.error("****  Please verify that the useJMX flag is set to true in the activemq.xml file ****", e);
        } catch (final MalformedObjectNameException e) {
            logger.error("Error creating JMX connection to broker.", e);
        }

        return tempList;
    }

    private void dumpStats(final Connection connection) {
        logger.debug("******************* BEGIN STAT DUMP ******************* ");
        final ActiveMQConnection c = (ActiveMQConnection) connection;
        c.getConnectionStats().dump(new IndentPrinter());
        logger.debug("******************** END STAT DUMP ******************** ");
    }

    /**
     * Method to get the session.
     * 
     * @return Session
     */
    public Session getSession() {
        return producerSession;
    }

    /**
     * Returns the url of the connection in the form host:port.
     */
    @Override
    public String toString() {
        return this.getBrokerURL().substring(6);
    }

}
