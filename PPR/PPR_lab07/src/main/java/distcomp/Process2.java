package distcomp;

import javax.jms.*;
import java.util.Random;

public class Process2 extends Thread {

    private final Session session;
    private final Connection con;
    private final MessageProducer producer20;
    private final MessageProducer producer23;
    private final MessageProducer producer24;
    private final MessageConsumer consumer02;
    private final MessageConsumer consumer32;
    private final MessageConsumer consumer42;
    private final MessageProducer topicProducer;
    private String parent = "x";
    private String myProcessId = "2";
    private int ile = 0;
    private boolean close = false;
    Random rand;

    public Process2(boolean procesId, int ile) throws JMSException {
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

        Queue queue20 = session.createQueue("20");
        this.producer20 = session.createProducer(queue20);

        Queue queue23 = session.createQueue("23");
        this.producer23 = session.createProducer(queue23);

        Queue queue24 = session.createQueue("24");
        this.producer24 = session.createProducer(queue24);


        Queue queue02 = session.createQueue("02");
        consumer02 = session.createConsumer(queue02);

        Queue queue32 = session.createQueue("32");
        consumer32 = session.createConsumer(queue32);

        Queue queue42 = session.createQueue("42");
        consumer42 = session.createConsumer(queue42);

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

                    Message mes02 = consumer02.receiveNoWait();
                    if (mes02 != null) {
                        String type02 = mes02.getStringProperty("type");
                        if (type02.equals("qe")) {
                            if (parent.equals("x")){
                                parent = mes02.getStringProperty("SenderId");
                                sendN("qe");
                            }
                            if(!parent.equals(mes02.getStringProperty("SenderId"))) ile--;
                        } else if (type02 == "re") this.ile--;

                        System.out.println("[" + myProcessId + "] get 0 to 2 " + type02);
                    }

                    Message mes32 = consumer32.receiveNoWait();
                    if (mes32 != null) {
                        String type32 = mes32.getStringProperty("type");
                        if (type32 == "qe") {
                            if (parent.equals("x")){
                                parent = mes32.getStringProperty("SenderId");
                                sendN("qe");
                            }
                            if(!parent.equals(mes32.getStringProperty("SenderId"))) ile--;
                        } else if (type32 == "re") this.ile--;

                        System.out.println("[" + myProcessId + "] get 3 to 2 " + type32);
                    }

                    Message mes42 = consumer42.receiveNoWait();
                    if (mes42 != null) {
                        String type42 = mes42.getStringProperty("type");
                        if (type42 == "qe") {
                            if (parent.equals("x")) parent = mes42.getStringProperty("SenderId");
                            sendN("qe");
                            if(!parent.equals(mes42.getStringProperty("SenderId"))) ile--;
                        } else if (type42 == "re") this.ile--;

                        System.out.println("[" + myProcessId + "] get 4 to 2 " + type42);
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
                if(!this.parent.equals("0")) producer20.send(m);
                if(!this.parent.equals("3")) producer23.send(m);
                if(!this.parent.equals("4")) producer24.send(m);
            }else if(type.equals("re")){
                if(this.parent.equals("0")) producer20.send(m);
                else if(this.parent.equals("3")) producer23.send(m);
                else if(this.parent.equals("4")) producer24.send(m);
            }

    }
}
