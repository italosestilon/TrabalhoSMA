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
import jade.lang.acl.UnreadableException;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

@SuppressWarnings("serial")
public class ReputationAgent extends Agent{
	
    private Map<AID, Float> reputationMap;
    
    private Map<AID, Integer> quantidadeDeAvaliacoes;
    
    protected void setup(){
    	
        reputationMap = new TreeMap<AID, Float>();
        
        quantidadeDeAvaliacoes  = new TreeMap<AID, Integer>();
        
        DFAgentDescription dfd = new DFAgentDescription();
        
        dfd.setName(getAID());
        
        ServiceDescription sd = new ServiceDescription();
        
        sd.setType("agente-reputacao-centralizado");
        
		sd.setName("JADE-Agent-Reputation");
		
        dfd.addServices(sd);
        
        try {
            DFService.register(this, dfd);
            
        } catch (FIPAException fe) {
        	
            fe.printStackTrace();
        }
        
        addBehaviour(new ReputationRequest());
        
        addBehaviour(new ReputationInform());
        
    }
    
    
	private class ReputationRequest extends CyclicBehaviour{

    	public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
			
			ACLMessage msg = myAgent.receive(mt);
			
			if(msg != null){
				System.out.println(myAgent.getLocalName()+" recebeu requisicao de "+msg.getSender().getLocalName());
				
				@SuppressWarnings("deprecation")
				AID idAgent = new AID(msg.getContent());
				
				ACLMessage reply = msg.createReply();
				
				Reputation reputacao = null;
				
				if(reputationMap.containsKey(idAgent)){
					
					reputacao = new Reputation(reputationMap.get(idAgent), idAgent);
					
					quantidadeDeAvaliacoes.put(idAgent,quantidadeDeAvaliacoes.get(idAgent)+1);
					
					reply.setPerformative(ACLMessage.INFORM);
					reply.setConversationId("inform-de-reputacao");
					
					try {
						
						reply.setContentObject((reputacao));
						
					} catch (IOException e) {
						//TODO remover "tratamento" de excecao
						e.printStackTrace();
					}
					
				}else{
					reply.setPerformative(ACLMessage.REFUSE);
					reply.setConversationId("refuse-de-reputacao");
					reply.setContent("not-available");
				}
				
				myAgent.send(reply);
			}else{
				block();
			}
			
		}
        
    }
    
  
	private class ReputationInform extends CyclicBehaviour{

        @Override
        public void action() {
        	
            MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchConversationId("informe-reputacao"), MessageTemplate.MatchPerformative(ACLMessage.INFORM));
            
            ACLMessage msg = myAgent.receive(mt);
            
            if(msg != null){
            	
                try {
                    Reputation reputation = (Reputation) msg.getContentObject();
                    
                    float reputationValue = reputation.getReputation();
                    
                    AID idAgent = reputation.getAgent();
                    
                    Float actualReputation = (Float) reputationMap.get(idAgent.getName());
                    
                    Integer peso = 0;
                    
                    if(quantidadeDeAvaliacoes.containsKey(idAgent)){
                    	peso = quantidadeDeAvaliacoes.get(idAgent);
                    }else{
                    	quantidadeDeAvaliacoes.put(idAgent, 0);
                    }
                    
                    if (actualReputation != null) {
                    	
                        reputationValue = (reputationValue + (actualReputation * peso)) / peso+1;
                    }

                    reputationMap.put(idAgent, reputationValue);
                    
                    
                    for(Iterator<AID> iterador = reputationMap.keySet().iterator(); iterador.hasNext();){
                    	AID aux = iterador.next();
                    	System.out.println("Agente = "+aux.getLocalName()+" reputacao = "+reputationMap.get(aux));
                    }
                    
                    while(reputationMap.keySet().iterator().hasNext()){
                    	
                    	
                    	
                    }
                    
                    
                    
                } catch (UnreadableException ex) {
                	
                    ex.printStackTrace();
                }
    
            }else{
            	
                block();
            }
        }
        
    }
}
