package br.ufc.sma.comportamento;

import br.ufc.sma.Cupom;
import br.ufc.sma.contractnet.AgenteParticipante;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class EnvioAcceptProposal extends CyclicBehaviour {
	private AgenteParticipante agente;
	public EnvioAcceptProposal(AgenteParticipante myAgent) {
		super(myAgent);
		this.agente = myAgent;
	}

	@Override
	public void action() {
		MessageTemplate mt = MessageTemplate
				.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
		ACLMessage msg = myAgent.receive(mt);
		
		if (msg != null) {
			Cupom cupom = null;
			
			try {
				cupom = (Cupom) msg.getContentObject();
			} catch (UnreadableException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if(cupom != null){
				
				ACLMessage reply = msg.createReply();
				reply.setPerformative(ACLMessage.INFORM);
			}
			
		} else {
			block();
		}
	}

}
