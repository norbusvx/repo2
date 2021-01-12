
package jmstest;


import javax.jms.*;

public class ExampleMessageSender {

    private final MessageProducer producer;
    private final Session session;
    private final Connection con;

    public ExampleMessageSender () throws JMSException {
        ConnectionFactory factory = JmsProvider.getConnectionFactory();
        this.con = factory.createConnection();
        con.start();

        this.session = con.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue queue = session.createQueue("example.queue");
        this.producer = session.createProducer(queue);
    }

    public void sendMessage (String message) throws JMSException {
        System.out.printf("Sending message: %s, Thread:%s%n",
                          message,
                          Thread.currentThread().getName());
        TextMessage textMessage = session.createTextMessage(message);
        producer.send(textMessage);
    }

    public void destroy () throws JMSException {
        con.close();
    }
}
