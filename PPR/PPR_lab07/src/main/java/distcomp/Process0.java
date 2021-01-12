package distcomp;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.*;

public class Process0 extends Thread {

    private final Session session;
    private final Connection con;
    private final MessageProducer producer01;
    private final MessageProducer producer02;
    private final MessageConsumer consumer10;
    private final MessageConsumer consumer20;
    private final MessageProducer topicProducer;
    private String parent = "x";
    private String myProcessId = "0";
    private int ile = 0;
    private boolean close = false;
    Random rand;

    public Process0(boolean procesId, int ile) throws JMSException {
        if(!procesId){
            this.ile = ile-1;
        }else{
            this.ile = ile;
            this.parent = myProcessId;
        }

        rand = new Random();

        ConnectionFactory factory = JmsProvider.getConnectionFactory();
        this.con = factory.createConnection();
        con.start();

        this.session = con.createSession(false, Session.AUTO_ACKNOWLEDGE);

        Queue queue01 = session.createQueue("01");
        this.producer01 = session.createProducer(queue01);

        Queue queue02 = session.createQueue("02");
        this.producer02 = session.createProducer(queue02);

        Queue queue10 = session.createQueue("10");
        consumer10 = session.createConsumer(queue10);

        Queue queue20 = session.createQueue("20");
        consumer20 = session.createConsumer(queue20);

        Topic topic = session.createTopic("ReportTopic");
        topicProducer = session.createProducer(topic);

    }

    private void sleepRandomTime() {
        try {
            Thread.sleep((rand.nextInt(4) + 1) * 1000);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    public void sendMessage(String message) throws JMSException {
        TextMessage textMessage = session.createTextMessage(message);
        topicProducer.send(textMessage);
    }

    @Override
    public void run() {
    try{
            while (true) {

                if(!close) {
                    if (this.parent.equals(myProcessId)) {
                        sendN("qe");
                    }

                    Message mes10 = consumer10.receiveNoWait();
                    if (mes10 != null) {
                        String type10 = mes10.getStringProperty("type");
                        if (type10.equals("qe")) {
                            if (parent.equals("x")){
                                parent = mes10.getStringProperty("SenderId");
                                sendN("qe");
                            }
                            if(!parent.equals(mes10.getStringProperty("SenderId"))) ile--;
                        } else if (type10 == "re") this.ile--;

                        System.out.println("[" + myProcessId + "] get 1 to 0 " + type10);
                    }

                    Message mes20 = consumer20.receiveNoWait();
                    if (mes20 != null) {
                        String type20 = mes20.getStringProperty("type");
                        if (type20 == "qe") {
                            if (parent.equals("x")){
                                parent = mes20.getStringProperty("SenderId");
                                sendN("qe");
                            }
                            if(!parent.equals(mes20.getStringProperty("SenderId"))) ile--;
                        } else if (type20 == "re") this.ile--;

                        System.out.println("[" + myProcessId + "] get 0 to 2 " + type20);
                    }

                    if(ile == 0 && !parent.equals("x")) {
                        if (!parent.equals(myProcessId)) {
                            sendN("re");
                        }
                        sendMessage("[proc:"+myProcessId+" | parent:"+parent+"] ,");
                        Thread.sleep(2000);
                        close = true;
                        System.out.println("[" + myProcessId + "] close");
                    }

                    Thread.sleep(2000);
                }
            }
    } catch (JMSException | InterruptedException e) {
        e.printStackTrace();
    }

    }

    public void destroy() throws JMSException {
        con.close();
    }


    public void sendN(String type ) throws JMSException {
            Message m = session.createMessage();
            m.setStringProperty("SenderId", myProcessId);
            m.setStringProperty("type", type);
            if(type.equals("qe")){
                if(!this.parent.equals("1")) producer01.send(m);
                if(!this.parent.equals("2")) producer02.send(m);
            }else if(type.equals("re")){
                if(this.parent.equals("1")) producer01.send(m);
                else if(this.parent.equals("2")) producer02.send(m);
            }

    }
}
