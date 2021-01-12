package distcomp;

import java.util.Arrays;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.*;

public class ProcessE extends Thread implements MessageListener {

    private final Session session;
    private final Connection con;
    private final MessageConsumer consumer;
    private boolean exit = false;
    private String road = "";

    public ProcessE() throws JMSException {
        ConnectionFactory factory = JmsProvider.getConnectionFactory();
        this.con = factory.createConnection();
        con.start();

        this.session = con.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Topic topic = session.createTopic("ReportTopic");

        consumer = session.createConsumer(topic);
    }

    @Override
    public void onMessage(Message msg) {
        try {
            TextMessage tx = (TextMessage) msg;
            road += tx.getText();
            System.out.printf("Road %s \n", road);
        } catch (JMSException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            consumer.setMessageListener(this);
            while (!exit) {
                Thread.sleep(100);
            }
        } catch (JMSException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void destroy() throws JMSException {
        con.close();
        exit = true;
    }

}
