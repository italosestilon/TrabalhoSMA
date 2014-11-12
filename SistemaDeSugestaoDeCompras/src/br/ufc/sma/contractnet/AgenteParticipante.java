package br.ufc.sma.contractnet;


import java.io.IOException;
import java.util.List;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import br.ufc.sma.comportamento.ComportamentoBuscarAgenteDeReputacaoCentralizado;
import br.ufc.sma.reputacao.Reputation;
import br.ufc.sma.Cupom;

public class AgenteParticipante extends Agent implements IAgente{
	
	private AID agenteDeReputacao;
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
		
		addBehaviour(new recebimentoDeCFPs());
		
		addBehaviour(new envioAcceptProposal());
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

	private boolean reputacaoAceita(Reputation reputacao){
		return reputacao.getReputation() >= 7;
	}
	
	private void lerCuponsDoXML(String caminhoXML){
	
	}
	
	private class recebimentoDeCFPs extends CyclicBehaviour {
		private boolean reputacaoVerificada = false;
		private Reputation reputacao;
		@Override
		public void action() {
			
			
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
			ACLMessage mensagemCFP = myAgent.receive(mt);
			if(mensagemCFP != null){
				
				if(!reputacaoVerificada){
					ACLMessage msgRequisicaoReputacao = new ACLMessage(ACLMessage.REQUEST);
					msgRequisicaoReputacao.addReceiver(agenteDeReputacao);
					
					try {
						msgRequisicaoReputacao.setContentObject(mensagemCFP.getSender().getName());
						send(msgRequisicaoReputacao);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					addBehaviour(new CyclicBehaviour() {
						
						@Override
						public void action(){
							MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM), 
									MessageTemplate.MatchConversationId("requisicao-de-reputacao"));
							ACLMessage msgRespostaReputacao = receive(mt);
							
							if(msgRespostaReputacao != null){
								try {
									Reputation r = (Reputation) msgRespostaReputacao.getContentObject();
									reputacaoVerificada = true;
									reputacao = r;
								} catch (UnreadableException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}else{
								block();
							}
						}
						
					});
				
				}else if(reputacaoVerificada && reputacaoAceita(reputacao)){
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
				}
			}else{
				block();
			}
		}
		
	}
	
	private class envioAcceptProposal extends CyclicBehaviour{

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
	
	@Override
	public void setAgenteDeReputcaoCentralizado(AID agente) {
		this.agenteDeReputacao = agente;
	}
	
	@Override
	public void adicionarAgenteDeReputacao(AID agente) {
		// TODO Auto-generated method stub
	}
	
}
