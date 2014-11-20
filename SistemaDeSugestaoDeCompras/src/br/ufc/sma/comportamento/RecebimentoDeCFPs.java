package br.ufc.sma.comportamento;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import br.ufc.sma.Cupom;
import br.ufc.sma.contractnet.AgenteParticipante;

public class RecebimentoDeCFPs extends CyclicBehaviour {
	private AgenteParticipante agente;
	public RecebimentoDeCFPs(AgenteParticipante myAgent) {
		super(myAgent);
		this.agente = myAgent;
	}
	
	public void action() {
		
		
		MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.CFP),
											MessageTemplate.MatchConversationId("venda-cupom"));
		ACLMessage mensagemCFP = myAgent.receive(mt);
	 	if(mensagemCFP != null){
			
	 		System.out.println(myAgent.getLocalName()+" recebeu uma mesagem CFP de "+mensagemCFP.getSender().getLocalName());
			String nomeDoCupom = mensagemCFP.getContent();
			List<Cupom> cupons = agente.buscarCupons(nomeDoCupom);
			
			ACLMessage propose = mensagemCFP.createReply();
			propose.setConversationId("venda-cupom");
			if (cupons.size() > 0) {
				propose.setPerformative(ACLMessage.PROPOSE);
				
				try {
					propose.setContentObject((Serializable) cupons);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	
			}else{
				propose.setPerformative(ACLMessage.REFUSE);
				propose.setContent("not-available");
			}
			
			myAgent.send(propose);
		}else{
			block();
		}
	}
	
}