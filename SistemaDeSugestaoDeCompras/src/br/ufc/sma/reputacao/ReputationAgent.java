/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufc.sma.reputacao;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author spectrus
 */

public class ReputationAgent extends Agent{
    private Map<AID, Float> reputationMap;
    
    protected void setup(){
        reputationMap = new TreeMap<AID, Float>();
        
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("agent-reputation");
        sd.setName("JADE-book-trading");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        
        addBehaviour(new ReputationRequest());
        
        addBehaviour(new ReputationInform());
        
    }
    
    public void printReputations() {
        
    }
    
    private class ReputationRequest extends CyclicBehaviour{

        @Override
        public void action() {
            
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
            ACLMessage msg = myAgent.receive(mt);
            
            if(msg != null){
                
                String content = msg.getContent();
                
                Float reputationValue = (Float) reputationMap.get(content); 
                ACLMessage reply = msg.createReply();
                AID id = new AID(msg.getContent());
                Reputation reputation;
                
                if(reputationValue != null){
                    reputation = new Reputation(reputationValue, id);
                }else{
                    reputation = new Reputation(0, id);
                }
                
                try {
                    reply.setContentObject(reputation);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                
                reply.setPerformative(ACLMessage.INFORM);
                myAgent.send(reply);
                
                printReputations();
            }else{
                block();
            }
            
        }
        
        protected void takeDown(){
            
        }
        
    }
    
    private class ReputationInform extends CyclicBehaviour{

        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
            ACLMessage msg = myAgent.receive(mt);
            
            if(msg != null){
                try {
                    Reputation reputation = (Reputation) msg.getContentObject();
                    float reputationValue = reputation.getReputation();
                    AID idAgent = reputation.getAgent();
                    
                    Float actualReputation = (Float) reputationMap.get(idAgent.getName());
                    
                    if (actualReputation != null) {
                        reputationValue = (reputationValue + actualReputation) / 2;
                    }

                    reputationMap.put(idAgent, reputationValue);
                } catch (UnreadableException ex) {
                    ex.printStackTrace();
                }
                
                
                
                
                printReputations();
            }else{
                block();
            }
        }
        
    }
}
