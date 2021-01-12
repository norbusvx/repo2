package distcomp;

import javax.jms.*;
import java.util.Random;

public class Process5 extends Thread {

    private final Session session;
    private final Connection con;
    private final MessageProducer producer54;
    private final MessageConsumer consumer45;
    private final MessageProducer topicProducer;
    private String parent = "x";
    private String myProcessId = "5";
    private int ile = 0;
    private boolean close = false;
    Random rand;

    public Process5(boolean procesId, int ile) throws JMSException {
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

        Queue queue54 = session.createQueue("54");
        this.producer54 = session.createProducer(queue54);

        Queue queue45 = session.createQueue("45");
        consumer45 = session.createConsumer(queue45);

        Topic topic = session.createTopic("ReportTopic");
        topicProducer = session.createProducer(topic);

    }

    private double[] generateNumbers() {
        double[] result = new double[4];
        for (int i = 0; i < 4; i++) {
            result[i] = rand.nextInt(10) + 5;
        }
        return result;
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

                    Message mes45 = consumer45.receiveNoWait();
                    if (mes45 != null) {
                        String type45 = mes45.getStringProperty("type");
                        if (type45.equals("qe")) {
                            if (parent.equals("x")){
                                parent = mes45.getStringProperty("SenderId");
                                sendN("qe");
                            }
                            if(!parent.equals(mes45.getStringProperty("SenderId"))) ile--;
                        } else if (type45 == "re") this.ile--;

                        System.out.println("[" + myProcessId + "] get 4 to 5 " + type45);
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
                if(!this.parent.equals("4")) producer54.send(m);
            }else if(type.equals("re")){
                if(this.parent.equals("4")) producer54.send(m);
            }

    }
}
