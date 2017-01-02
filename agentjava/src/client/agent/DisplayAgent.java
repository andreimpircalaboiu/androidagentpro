package client.agent;

import java.nio.file.Files;
import java.nio.file.Paths;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import client.model.ProductResponse;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPANames.InteractionProtocol;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.util.Logger;

public class DisplayAgent extends Agent
{
	private ACLMessage spokenMsg;

	protected void setup()
	{
		addBehaviour(new ProductsGiver(this));
	}

	class ProductsGiver extends CyclicBehaviour
	{
		private ObjectMapper mapper;

		ProductsGiver(Agent a)
		{
			super(a);
		}

		public void onStart()
		{
			mapper = new ObjectMapper();
		}

		public void action()
		{
			ACLMessage msg = myAgent.receive();
			if (msg != null && msg.getPerformative() == ACLMessage.REQUEST && msg.getProtocol().equals(InteractionProtocol.FIPA_REQUEST))
			{
				if (msg.getContent().equals("D:\\produse.json"))
				{
					try
					{
						String products = new String(Files.readAllBytes(Paths.get(msg.getContent())));
						spokenMsg = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
						spokenMsg.setContent(products);
						spokenMsg.addReceiver(msg.getSender());
						send(spokenMsg);
					}
					catch (Exception e)
					{
						Logger.println(e.toString());
						e.printStackTrace();
					}
				}
				else
				{
					ProductResponse response = new ProductResponse();
					response.setErrorText("File name unavailable");
					try
					{
						String json = mapper.writeValueAsString(response);
						spokenMsg = new ACLMessage(ACLMessage.REJECT_PROPOSAL);
						spokenMsg.setContent(json);
						spokenMsg.addReceiver(msg.getSender());
						send(spokenMsg);
					}
					catch (JsonProcessingException e)
					{
						e.printStackTrace();
					}
				}
			}
			else if(msg == null)
			{
				block();
			}
			else
			{
				ProductResponse response = new ProductResponse();
				response.setErrorText("Message needs to be of type REQUEST and needs to use the interaction protocol FIPA_REQUEST");
				try
				{
					String json = mapper.writeValueAsString(response);
					spokenMsg = new ACLMessage(ACLMessage.NOT_UNDERSTOOD);
					spokenMsg.setContent(json);
					spokenMsg.addReceiver(msg.getSender());
					send(spokenMsg);
				}
				catch (JsonProcessingException e)
				{
					e.printStackTrace();
				}
			}
		}
	}
}
