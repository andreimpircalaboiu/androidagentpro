package chat.client.agent;

import android.content.Context;
import android.content.Intent;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.logging.Level;
import chat.client.model.FilePathResponse;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.util.Logger;

/**
 * Created by andrei.pircalaboiu on 14.12.2016.
 */

public class MobileAgent extends Agent implements IMobileAgent
{
    private static final long serialVersionUID = 1594371294421614291L;

    private Logger logger = Logger.getJADELogger(this.getClass().getName());
    private ACLMessage productsRequestMsg;
    private ACLMessage filePathRequestMsg;
    private Context context;
    private MobileAgent instance;

    protected void setup() {
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            if (args[0] instanceof Context) {
                context = (Context) args[0];
            }
        }

        registerO2AInterface(IMobileAgent.class, this);
        addBehaviour(new MobileAgent.FilePathReciever(this));
        addBehaviour(new MobileAgent.ProductsReciever(this));
        instance = this;
        filePathRequestMsg = new ACLMessage(ACLMessage.REQUEST);
        filePathRequestMsg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
        productsRequestMsg = new ACLMessage(ACLMessage.REQUEST);
        productsRequestMsg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);

    }

    @Override
    public void requestFilename() {
        addBehaviour(new MobileAgent.FilenameRequester(this));
    }

    class FilePathReciever extends CyclicBehaviour {
        private ObjectMapper mapper;

        FilePathReciever(Agent a) {
            super(a);
        }

        public void onStart() {
            mapper = new ObjectMapper();
        }

        public void action() {
            ACLMessage msg = myAgent.receive();
            if (msg != null && msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
                try {
                    FilePathResponse response = mapper.readValue(msg.getContent(),FilePathResponse.class);
                    addBehaviour(new MobileAgent.ProductsRequester(instance,response.getFilePath()));
                } catch (Exception e) {
                    Logger.println(e.toString());
                    e.printStackTrace();
                }
            } else if (msg == null) {
                block();
            }
        }
    }

    class ProductsReciever extends CyclicBehaviour {

        ProductsReciever(Agent a) {
            super(a);
        }

        public void onStart() {

        }

        public void action() {
            ACLMessage msg = myAgent.receive();
            if (msg != null && msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
                try {
                    Intent broadcast = new Intent();
                    broadcast.setAction("jade.demo.chat.PRODUCT_LIST");
                    broadcast.putExtra("productResponse",msg.getContent());
                    context.sendBroadcast(broadcast);
                } catch (Exception e) {
                    Logger.println(e.toString());
                    e.printStackTrace();
                }
            } else if (msg == null) {
                block();
            }
        }
    }

    class FilenameRequester extends OneShotBehaviour {
        private static final long serialVersionUID = -1426033904935339194L;
        private String sentence;

        private FilenameRequester(Agent a) {
            super(a);
            sentence = "RequestFilename";
        }

        public void action() {
            filePathRequestMsg.clearAllReceiver();
            AID aid = getAID("filePathInfoAgent");
            filePathRequestMsg.addReceiver(aid);
            filePathRequestMsg.setContent(sentence);
            send(filePathRequestMsg);
        }
    }

    private class ProductsRequester extends OneShotBehaviour {
        private static final long serialVersionUID = -1426033904935339194L;
        private String sentence;

        private ProductsRequester(Agent a, String s) {
            super(a);
            sentence = s;
        }

        public void action() {
            productsRequestMsg.clearAllReceiver();
            AID aid = getAID("displayAgent");
            productsRequestMsg.addReceiver(aid);
            productsRequestMsg.setContent(sentence);
            send(productsRequestMsg);
        }
    }

}
