package br.ufc.sma.contractnet;


import java.io.IOException;
import java.util.List;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import br.ufc.sma.comportamento.ComportamentoBuscarAgenteDeReputacaoCentralizado;
import br.ufc.sma.xml.BuilderCupom;
import br.ufc.sma.Cupom;

public class AgenteParticipante extends Agent{
	private final int INTERVALO_BUSCAR_AGENTE_DE_REPUTACAO_CENTRALIZADO = 100;
	private List<Cupom> cupons;
	
	protected void setup(){
		
		Object args[] = getArguments();
		if(args.length > 0){
			lerCuponsDoXML((String) args[0]);
		}else{
			System.out.println("Erro nos parâmetros!");
			this.doDelete();
		}
		
		addBehaviour(new ComportamentoBuscarAgenteDeReputacaoCentralizado(this, INTERVALO_BUSCAR_AGENTE_DE_REPUTACAO_CENTRALIZADO));
		
		addBehaviour(new recebimentoDeCFPs(this));
		
		addBehaviour(new envioAcceptProposal(this));
	}
	
	private Cupom buscarCupom(String nomeDoCupom) {
		Cupom cupom = null;
		for(Cupom c : this.cupons){
			if(c.getNomeProduto().equals(nomeDoCupom) || c.getTipoProduto().equals(nomeDoCupom)){
				cupom = c;
				break;
			}
		}
		return cupom;
	}

	
	private void lerCuponsDoXML(String caminhoXML){
		BuilderCupom builderCupom = new BuilderCupom(caminhoXML);
		this.cupons = builderCupom.getCupons();
	}
	
	private class recebimentoDeCFPs extends CyclicBehaviour {
		public recebimentoDeCFPs(Agent myAgent) {
			super(myAgent);
		}
		
		public void action() {
			
			
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
			ACLMessage mensagemCFP = myAgent.receive(mt);
			if(mensagemCFP != null){
				
				String nomeDoCupom = mensagemCFP.getContent();
				Cupom cupom = buscarCupom(nomeDoCupom);
				
				ACLMessage propose = mensagemCFP.createReply();
				
				if (cupom != null) {
					
					
					propose.setPerformative(ACLMessage.PROPOSE);
					try {
						propose.setContentObject(cupom);
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
	
	private class envioAcceptProposal extends CyclicBehaviour{
		public envioAcceptProposal(Agent myAgent) {
			super(myAgent);
		}
		
		@Override
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
			ACLMessage msg = myAgent.receive(mt);
			
			if (msg != null) {
				
			}else{
				block();
			}
		}
		
	}
	
}
