package br.ufc.sma.contractnet;



import jade.core.Agent; 
import jade.core.AID;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.util.Enumeration;



public class AgenteIniciante extends Agent {
	
	private List<AID> agentesVendedores;
    private AID agenteDeReputacao;
    
    
	protected void setup() { 
	  	Object[] args = getArguments();
	  	
	  	addBehaviour(new TickerBehaviour(this, 100){
            @Override
            protected void onTick() {
                DFAgentDescription template = new DFAgentDescription();
                ServiceDescription sd = new ServiceDescription();
                sd.setType("agente-reputacao");
                template.addServices(sd);
                try {
                    DFAgentDescription[] result = DFService.search(myAgent, template);
                    if (result.length > 0) {
                        agenteDeReputacao = result[0].getName();
                        System.out.println("Agente de reputação " + agenteDeReputacao.getName());
                        myAgent.removeBehaviour(this);
                    } else {
                    }
                } catch (FIPAException fe) {
                    fe.printStackTrace();
                }
            }
        });
	  	
	  	
	  	addBehaviour(new TickerBehaviour(this, 20000) {
			protected void onTick() {
				
				DFAgentDescription template = new DFAgentDescription();
				ServiceDescription sd = new ServiceDescription();
				sd.setType("vendedor-cupons");
				template.addServices(sd);
				try {
					DFAgentDescription[] result = DFService.search(myAgent, template); 
					System.out.println("Os seguintes vendedores foram encontrados:");
					agentesVendedores = new ArrayList<AID>();
					for (int i = 0; i < result.length; ++i) {
						agentesVendedores.add(result[i].getName());
					}
				}
				catch (FIPAException fe) {
					fe.printStackTrace();
				}
			}
		} );
	  	
	  	

	  	ACLMessage msg = new ACLMessage(ACLMessage.CFP);
	  	for (int i = 0; i < agentesVendedores.size(); ++i) {
  			msg.addReceiver(agentesVendedores.get(i));
  		}
	  	
		msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
		msg.setReplyByDate(new Date(System.currentTimeMillis() + 10000));
		//TODO:
		msg.setContent("dummy-action");
		
		addBehaviour(new ContractNetInitiator(this, msg) {
			
			protected void handlePropose(ACLMessage propose, Vector v) {
				System.out.println("Agent "+propose.getSender().getName()+" proposed "+propose.getContent());
			}
			
			protected void handleRefuse(ACLMessage refuse) {
				System.out.println("Agent "+refuse.getSender().getName()+" refused");
			}
			
			protected void handleFailure(ACLMessage failure) {
				if (failure.getSender().equals(myAgent.getAMS())) {
					System.out.println("Agente não existe");
				}else {
					System.out.println("Agente "+failure.getSender().getName()+" falhou");
				}
			}
			
			protected void handleAllResponses(Vector responses, Vector acceptances) {
				if (responses.size() < agentesVendedores.size()) {
					System.out.println("Timeout expired: missing "+(agentesVendedores.size() - responses.size())+" responses");
				}
				
				int bestProposal = -1;
				AID bestProposer = null;
				ACLMessage accept = null;
				Enumeration e = responses.elements();
				while (e.hasMoreElements()) {
					ACLMessage msg = (ACLMessage) e.nextElement();
					if (msg.getPerformative() == ACLMessage.PROPOSE) {
						ACLMessage reply = msg.createReply();
						reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
						acceptances.addElement(reply);
						int proposal = Integer.parseInt(msg.getContent());
						if (proposal > bestProposal) {
							bestProposal = proposal;
							bestProposer = msg.getSender();
							accept = reply;
						}
					}
				}
			
				if (accept != null) {
					System.out.println("Accepting proposal "+bestProposal+" from responder "+bestProposer.getName());
					accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
				}						
			}
			
			protected void handleInform(ACLMessage inform) {
				System.out.println("Agent "+inform.getSender().getName()+" successfully performed the requested action");
			}
		} );
	} 
}
