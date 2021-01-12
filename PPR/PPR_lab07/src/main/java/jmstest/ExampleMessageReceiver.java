/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jmstest;

import javax.jms.*;

public class ExampleMessageReceiver implements MessageListener {

    private Connection con;

    public void startListener () throws JMSException {
        ConnectionFactory factory = JmsProvider.getConnectionFactory();
        this.con = factory.createConnection();
        con.start();

        Session session = con.createSession(false, Session.AUTO_ACKNOWLEDGE);

        Queue queue = session.createQueue("example.queue");
        MessageConsumer consumer = session.createConsumer(queue);
        consumer.setMessageListener(this);
    }

    @Override
    public void onMessage (Message message) {
        if (message instanceof TextMessage) {
            TextMessage tm = (TextMessage) message;
            try {

                System.out.printf("Message received: %s, Thread: %s%n",
                                  tm.getText(),
                                  Thread.currentThread().getName());
            } catch (JMSException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void destroy () throws JMSException {
        con.close();
    }
}