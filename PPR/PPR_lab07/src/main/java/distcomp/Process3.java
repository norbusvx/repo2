package distcomp;

import javax.jms.*;
import java.util.ArrayList;
import java.util.Random;

public class Process3 extends Thread {

    private final Session session;
    private final Connection con;
    private final MessageProducer producer31;
    private final MessageProducer producer32;
    private final MessageProducer producer34;
    private final MessageConsumer consumer13;
    private final MessageConsumer consumer23;
    private final MessageConsumer consumer43;
    private final MessageProducer topicProducer;
    private String parent = "x";
    private String myProcessId = "3";
    private int ile = 0;
    private boolean close = false;
    private ArrayList<String> table = new ArrayList<>();
    Random rand;

    public Process3(boolean procesId, int ile) throws JMSException {
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

        Queue queue31 = session.createQueue("31");
        this.producer31 = session.createProducer(queue31);

        Queue queue32 = session.createQueue("32");
        this.producer32 = session.createProducer(queue32);

        Queue queue34 = session.createQueue("34");
        this.producer34 = session.createProducer(queue34);


        Queue queue13 = session.createQueue("13");
        consumer13 = session.createConsumer(queue13);

        Queue queue23 = session.createQueue("23");
        consumer23 = session.createConsumer(queue23);

        Queue queue43 = session.createQueue("43");
        consumer43 = session.createConsumer(queue43);

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
                    System.out.println("[" + myProcessId + "] "+ile +" QE   --------------- "+ ile +" RE");

                    Message mes13 = consumer13.receiveNoWait();
                    if (mes13 != null) {
                        String type13 = mes13.getStringProperty("type");
                        if (type13.equals("qe")) {
                            if (parent.equals("x")) {
                                parent = mes13.getStringProperty("SenderId");
                                sendN("qe");
                            }
                            if(!parent.equals(mes13.getStringProperty("SenderId"))) ile--;
                        } else if (type13 == "re") this.ile--;

                        System.out.println("[" + myProcessId + "] get 1 to 3 " + type13);
                    }

                    Message mes23 = consumer23.receiveNoWait();
                    if (mes23 != null) {
                        String type23 = mes23.getStringProperty("type");
                        if (type23 == "qe") {
                            if (parent.equals("x")){
                                parent = mes23.getStringProperty("SenderId");
                                sendN("qe");
                            }
                            if(!parent.equals(mes23.getStringProperty("SenderId"))) this.ile--;
                        } else if (type23 == "re") this.ile--;

                        System.out.println("[" + myProcessId + "] get 2 to 3 " + type23);
                    }

                    Message mes43 = consumer43.receiveNoWait();
                    if (mes43 != null) {
                        String type43 = mes43.getStringProperty("type");
                        if (type43 == "qe") {
                            if (parent.equals("x")){
                                parent = mes43.getStringProperty("SenderId");
                                sendN("qe");
                            }
                            if(!parent.equals(mes43.getStringProperty("SenderId"))) this.ile--;
                        } else if (type43 == "re") this.ile--;

                        System.out.println("[" + myProcessId + "] get 4 to 3 " + type43);
                    }

                    if(ile == 0 && !parent.equals("x")){
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
                if(!this.parent.equals("1")) producer31.send(m);
                if(!this.parent.equals("2")) producer32.send(m);
                if(!this.parent.equals("4")) producer34.send(m);
            }else if(type.equals("re")){
                if(this.parent.equals("1")) producer31.send(m);
                else if(this.parent.equals("2")) producer32.send(m);
                else if(this.parent.equals("4")) producer34.send(m);
            }

    }
}
