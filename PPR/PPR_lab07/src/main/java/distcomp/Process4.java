package distcomp;

import javax.jms.*;
import java.util.Random;

public class Process4 extends Thread {

    private final Session session;
    private final Connection con;
    private final MessageProducer producer42;
    private final MessageProducer producer43;
    private final MessageProducer producer45;
    private final MessageConsumer consumer24;
    private final MessageConsumer consumer34;
    private final MessageConsumer consumer54;
    private final MessageProducer topicProducer;
    private String parent = "x";
    private String myProcessId = "4";
    private int ile = 0;
    private boolean close = false;
    Random rand;

    public Process4(boolean procesId, int ile) throws JMSException {
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

        Queue queue42 = session.createQueue("42");
        this.producer42 = session.createProducer(queue42);

        Queue queue43 = session.createQueue("43");
        this.producer43 = session.createProducer(queue43);

        Queue queue45 = session.createQueue("45");
        this.producer45 = session.createProducer(queue45);


        Queue queue24 = session.createQueue("24");
        consumer24 = session.createConsumer(queue24);

        Queue queue34 = session.createQueue("34");
        consumer34 = session.createConsumer(queue34);

        Queue queue54 = session.createQueue("54");
        consumer54 = session.createConsumer(queue54);

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
                    System.out.println(ile +" QE   --------------- "+ ile +" RE");

                    Message mes24 = consumer24.receiveNoWait();
                    if (mes24 != null) {
                        String type24 = mes24.getStringProperty("type");
                        if (type24.equals("qe")) {
                            if (parent.equals("x")){
                                parent = mes24.getStringProperty("SenderId");
                                sendN("qe");
                            }
                            if(!parent.equals(mes24.getStringProperty("SenderId"))) ile--;
                        } else if (type24 == "re") this.ile--;

                        System.out.println("[" + myProcessId + "] get 2 to 4 " + type24);
                    }

                    Message mes34 = consumer34.receiveNoWait();
                    if (mes34 != null) {
                        String type34 = mes34.getStringProperty("type");
                        if (type34 == "qe") {
                            if (parent.equals("x")){
                                parent = mes34.getStringProperty("SenderId");
                                sendN("qe");
                            }
                            if(!parent.equals(mes34.getStringProperty("SenderId"))) this.ile--;
                        } else if (type34 == "re") this.ile--;

                        System.out.println("[" + myProcessId + "] get 3 to 4 " + type34);
                    }

                    Message mes54 = consumer54.receiveNoWait();
                    if (mes54 != null) {
                        String type54 = mes54.getStringProperty("type");
                        if (type54 == "qe") {
                            if (parent.equals("x")){
                                parent = mes54.getStringProperty("SenderId");
                                sendN("qe");
                            }
                            if(!parent.equals(mes54.getStringProperty("SenderId"))) this.ile--;
                        } else if (type54 == "re") this.ile--;

                        System.out.println("[" + myProcessId + "] get 5 to 4 " + type54);
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
                if(!this.parent.equals("2")) producer42.send(m);
                if(!this.parent.equals("3")) producer43.send(m);
                if(!this.parent.equals("5")) producer45.send(m);
            }else if(type.equals("re")){
                if(this.parent.equals("2")) producer42.send(m);
                else if(this.parent.equals("3")) producer43.send(m);
                else if(this.parent.equals("5")) producer45.send(m);
            }

    }
}
