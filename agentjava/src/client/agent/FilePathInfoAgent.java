package client.agent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import client.model.FilePathResponse;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPANames.InteractionProtocol;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.util.Logger;

public class FilePathInfoAgent extends Agent
{
	private ACLMessage spokenMsg;

	protected void setup()
	{
		addBehaviour(new FilePathGiver(this));
		
	}

	class FilePathGiver extends CyclicBehaviour
	{
		private ObjectMapper mapper;

		FilePathGiver(Agent a)
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
			if(msg != null && msg.getPerformative() == ACLMessage.REQUEST && msg.getProtocol().equals(InteractionProtocol.FIPA_REQUEST))
			{
				try
				{
					FilePathResponse response = new FilePathResponse();
					response.setFilePath("D:\\produse.json");
					String json = mapper.writeValueAsString(response);
					
					spokenMsg = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
					spokenMsg.setContent(json);
					spokenMsg.addReceiver(msg.getSender());
					send(spokenMsg);
				}
				catch (Exception e)
				{
					Logger.println(e.toString());
					e.printStackTrace();
				}
			}
			else if(msg == null)
			{
				block();
			}
			else
			{
				try
				{

					FilePathResponse response = new FilePathResponse();
					response.setErrorText("Message needs to be of type REQUEST and needs to use the interaction protocol FIPA_REQUEST");
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
