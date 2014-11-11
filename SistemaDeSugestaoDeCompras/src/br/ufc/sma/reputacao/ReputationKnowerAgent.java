package br.ufc.sma.reputacao;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class ReputationKnowerAgent extends Agent{
	
	private Map<String,Float> reputationMap;
	
	@Override
	public void setup(){
		
		reputationMap = new TreeMap<String, Float>();
		
		DFAgentDescription df = new DFAgentDescription();
		
		df.setName(getAID());
		
		ServiceDescription sd = new ServiceDescription();
		
		sd.setType("agent-rating");
		sd.setName("JADE-Agent-Reputation");
		
		df.addServices(sd);
		
		try {
			DFService.register(this, df);
		} catch (FIPAException e) {
			e.printStackTrace();
		}
		
		addBehaviour(new ReputationAdition());
		addBehaviour(new ReputationRequest());
		
	}
	
	protected void takeDown() {
		
		try {
			DFService.deregister(this);
		}
		catch (FIPAException e) {
			e.printStackTrace();
		}
	}
	
	private class ReputationRequest extends CyclicBehaviour{

		@Override
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
			ACLMessage msg = myAgent.receive(mt);
			
			if(msg != null){
				
				@SuppressWarnings("deprecation")
				AID agentId = new AID(msg.getContent());
				
				ACLMessage reply = msg.createReply();
				
				Reputation reputacao = null;
				
				if(reputationMap.containsKey(agentId)){
					reputacao = new Reputation(reputationMap.get(agentId), agentId);
					
					reply.setPerformative(ACLMessage.INFORM);
					
					try {
						
						reply.setContentObject((reputacao));
						
					} catch (IOException e) {
						//TODO remover "tratamento" de excecao
						e.printStackTrace();
					}
					
				}else{
					reply.setPerformative(ACLMessage.REFUSE);
					reply.setContent("not-available");
				}
				
				myAgent.send(reply);
			}else{
				block();
			}
			
		}
		
	}
	
	private class ReputationAdition extends CyclicBehaviour{

		
		public void action() {
			
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
			ACLMessage msg = myAgent.receive(mt);
			
			if(msg != null){
				String[] content = msg.getContent().split("<>");
				String agentId = content[0];
				Float agentRating = Float.valueOf(content[1]);
				
				if(!reputationMap.containsKey(agentId)){
					reputationMap.put(agentId, agentRating);
				}else{
					reputationMap.put(agentId, (reputationMap.get(agentId)+agentRating)/2);
				}
				
				
				System.out.println("agente "+agentId+" avaliado");
				
				System.out.println("Mapa de reputação");
				for (Entry<String, Float> entry : reputationMap.entrySet())
				{
				    System.out.println(entry.getKey() + "<> " + entry.getValue());
				}
				
			}else{
				block();
			}
		}
		
	}
}
