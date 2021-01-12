package distcomp;

import javax.jms.*;
import java.util.Random;

public class Process1 extends Thread {

    private final Session session;
    private final Connection con;
    private final MessageProducer producer10;
    private final MessageProducer producer13;
    private final MessageConsumer consumer01;
    private final MessageConsumer consumer31;
    private final MessageProducer topicProducer;
    private String parent = "x";
    private String myProcessId = "1";
    private int ile = 0;
    private boolean close = false;
    Random rand;

    public Process1(boolean procesId, int ile) throws JMSException {
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

        Queue queue10 = session.createQueue("10");
        this.producer10 = session.createProducer(queue10);

        Queue queue13 = session.createQueue("13");
        this.producer13 = session.createProducer(queue13);

        Queue queue01 = session.createQueue("01");
        consumer01 = session.createConsumer(queue01);

        Queue queue31 = session.createQueue("31");
        consumer31 = session.createConsumer(queue31);

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

                    Message mes01 = consumer01.receiveNoWait();
                    if (mes01 != null) {
                        String type01 = mes01.getStringProperty("type");
                        if (type01.equals("qe")) {
                            if (parent.equals("x")){
                                parent = mes01.getStringProperty("SenderId");
                                sendN("qe");
                            }
                            if(!parent.equals(mes01.getStringProperty("SenderId"))) ile--;
                        } else if (type01 == "re") this.ile--;

                        System.out.println("[" + myProcessId + "] get 0 to 1 " + type01);
                    }

                    Message mes31 = consumer31.receiveNoWait();
                    if (mes31 != null) {
                        String type31 = mes31.getStringProperty("type");
                        if (type31 == "qe") {
                            if (parent.equals("x")){
                                parent = mes31.getStringProperty("SenderId");
                                sendN("qe");
                            }
                            if(!parent.equals(mes31.getStringProperty("SenderId"))) ile--;
                        } else if (type31 == "re") this.ile--;

                        System.out.println("[" + myProcessId + "] get 3 to 1 " + type31);
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
                if(!this.parent.equals("0")) producer10.send(m);
                if(!this.parent.equals("3")) producer13.send(m);
            }else if(type.equals("re")){
                if(this.parent.equals("0")) producer10.send(m);
                else if(this.parent.equals("3")) producer13.send(m);
            }

    }
}
