package br.ufc.sma.contractnet;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.ufc.sma.Cupom;
import br.ufc.sma.Preferencia;
import br.ufc.sma.comportamento.ComportamentoBuscarAgenteDeReputacaoCentralizado;
import br.ufc.sma.reputacao.Reputation;
import br.ufc.sma.xml.BuilderPreferencia;
import br.ufc.sma.xml.IBuiderPeferencia;

public class AgenteIniciante extends Agent implements IAgente{


	private static final int INTERVALO_ENVIAR_MENSAGEM = 2000;

	private final int INTERVALO_BUSCAR_VENDEDOR = 20000;

	private final int INTERVALO_BUSCAR_AGENTE_DE_REPUTACAO_CENTRALIZADO = 100;

	private List<AID> agentesVendedores;

	private AID agenteDeReputacao;

	private Map<String, Preferencia> preferencias;

	private Map<String,Cupom> propostasBoas;

	protected void setup() {
		
		Object args[] = getArguments();
		if(args.length > 0){
			pegarPreferencias(((String) args[0]));
		}else{
			this.doDelete();
		}

		agentesVendedores = new ArrayList<AID>(); 
		propostasBoas = new HashMap<String, Cupom>();
		

		System.out.println("Olá! O agente "+getAID().getName()+" está pronto para buscar cupons.");

	
		addBehaviour(new ComportamentoBuscarAgenteDeReputacaoCentralizado(this, INTERVALO_BUSCAR_AGENTE_DE_REPUTACAO_CENTRALIZADO));         

		// TickerBehaviour para enviar um request a cada minuto
		addBehaviour(new BuscarVendedoresDeCupons(this, INTERVALO_BUSCAR_VENDEDOR));
		
	}

	private void pegarPreferencias(String caminhoDoArquivo) {
		
		IBuiderPeferencia construtor = new BuilderPreferencia(caminhoDoArquivo);
		
		preferencias = construtor.getPrefencias();
	}
	
	public Collection<Cupom> getCupons(){
		return propostasBoas.values();
	}

	protected void takeDown() {
		System.out.println("Agente "+getAID().getName()+" já esgotou suas preferências e será finalizada.");
	}

	/*
	 * Classe interna RequestPerformer. 
	 * Comportamento usado pelo comprador para encontrar vendedores do livro procurado
	 */
	private class RequestPerformer extends Behaviour {
		
		private int repliesCnt = 0; // Número de respostas de vendedores
		
		private List<AID> agentesVendedoresNovos; 

		private int repliesReputation = 0;
		
		private int repliesCompra = 0;

		private int qtdPropostasPreAceitas = 0; //Propostas para analisar a reputacao do vendedor

		private MessageTemplate mt; // Template para receber respostas


		private int step = 0;

		public RequestPerformer(Agent a, List<AID> agentesVendedoresNovos){
			super(a);
			this.agentesVendedoresNovos = agentesVendedoresNovos;
		}
		
		public void action() {
			switch (step) {
				case 0:
					passoEnviarCFP();
					break;
					
				case 1:
					passoAnalisarPropostas();
					break;
					
				case 2:
					passoAvaliarReputacao();
					break;
					
				case 3:
					passoEnviarMensagemDeCompra();
					break;
	
				case 4:
					PassoAvaliarVendedor();
					break;
					
				case 5:
					removeBehaviour(this);
					break;
			}

		}

		private void PassoAvaliarVendedor() {
			
			ACLMessage reply = myAgent.receive(mt);
			
			if (reply != null) {

				ACLMessage informReputation = new ACLMessage();
				
				System.out.println(myAgent.getLocalName()+" Avaliando Vendedor "+reply.getSender().getLocalName());
				
				Cupom cupom = null;
				try {
					cupom = (Cupom) reply.getContentObject();
				} catch (UnreadableException e) {
					e.printStackTrace();
				}
				Reputation reputation = null;
				
				if(cupom != null){
					reputation = new Reputation(10, reply.getSender());
					for(Preferencia p : preferencias.values()){
						if(cupom.getTipoProduto().equalsIgnoreCase(p.getTipo())){
							preferencias.remove(p.getTipo());
						}
					}
				}else{
					reputation = new Reputation(0, reply.getSender());
				}
				
				try {
					informReputation.setContentObject(reputation);
					informReputation.setPerformative(ACLMessage.INFORM);
					informReputation.addReceiver(agenteDeReputacao);
					informReputation.setConversationId("informe-reputacao");
					myAgent.send(informReputation);

				} catch (IOException ex) {
					ex.printStackTrace();
				}
				myAgent.doDelete();


				repliesCompra++;
				
				if(repliesCompra >= propostasBoas.size()){
					step = 5;
				}
				
			}
			else {
				block();
			}
		}

		private void passoEnviarMensagemDeCompra() {
			ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
			
			for(Cupom proposta : propostasBoas.values()){
				order.addReceiver(proposta.getVendedor());
				System.out.println(myAgent.getLocalName()+" tentando comprar o cupom "+proposta.getNomeProduto()+" do agente "+proposta.getVendedor().getLocalName());

				try {
					order.setContentObject(proposta);
	
					order.setConversationId("venda-cupom");
	
					order.setReplyWith("order"+System.currentTimeMillis());
	
					myAgent.send(order);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			// preparando o template para receber a resposta
			mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
									 MessageTemplate.MatchConversationId("venda-cupom"));

			step = 4;
		}

		private void passoAnalisarPropostas() {
			ACLMessage reply = myAgent.receive(mt);

			if (reply != null) {
				
				System.out.println(myAgent.getLocalName()+" Analisando Resposta do Agente "+reply.getSender().getLocalName());
				
				List<Cupom> cupons = null;

				try {
					cupons = (ArrayList)reply.getContentObject();
				} catch (UnreadableException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(cupons != null){
					for(Cupom cupom : cupons){
						if(avaliarProposta(cupom)){
							
							if(propostasBoas.containsKey(cupom)){
								propostasBoas.remove(cupom.getTipoProduto());
							}
							
							propostasBoas.put(cupom.getTipoProduto(), cupom);
		
							qtdPropostasPreAceitas++;
		
							ACLMessage requestReputation = new ACLMessage();
		
							requestReputation.setPerformative(ACLMessage.REQUEST);
		
							requestReputation.setContent(reply.getSender().getName());
		
							requestReputation.addReceiver(agenteDeReputacao);
		
							requestReputation.setConversationId("informe-de-reputacao");
		
							//requestReputation.setReplyWith("reputation"+System.currentTimeMillis());
		
							myAgent.send(requestReputation);
							
						}
					}
				}


				repliesCnt++;

				if (repliesCnt >= agentesVendedoresNovos.size()) {
					step = 2; 
				}
			}

			else {
				block();
			}
		}

		private void passoEnviarCFP() {
			if(agentesVendedoresNovos.size() == 0) return;
			
			
			for (Preferencia preferencia : preferencias.values()){
				
				ACLMessage cfp = new ACLMessage(ACLMessage.CFP);

				for (int i = 0; i < agentesVendedoresNovos.size(); i++) {
					
					System.out.println(myAgent.getLocalName()+" Enviando CFP para "+agentesVendedoresNovos.get(i).getLocalName());
					cfp.addReceiver(agentesVendedoresNovos.get(i));
					
				} 

				
				cfp.setContent(preferencia.getTipo());

				cfp.setConversationId("venda-cupom");

				cfp.setReplyWith("cfp"+System.currentTimeMillis());

				myAgent.send(cfp);
				
				mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.PROPOSE),
						MessageTemplate.and(MessageTemplate.MatchConversationId("venda-cupom"),
								MessageTemplate.MatchInReplyTo(cfp.getReplyWith())));
			}

			step = 1;
		}

		private void passoAvaliarReputacao() {
			MessageTemplate mtReputationInform = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
					MessageTemplate.MatchConversationId("inform-de-reputacao"));
			ACLMessage replyInform = myAgent.receive(mtReputationInform);
			
			MessageTemplate mtReputationRefuse = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.REFUSE),
					MessageTemplate.MatchConversationId("refuse-de-reputacao"));
			ACLMessage replyRefuse = myAgent.receive(mtReputationRefuse);
			
			if(replyInform != null){
				
				System.out.println(myAgent.getLocalName()+" Analisando reputação");
				try {
					Reputation reputation = (Reputation) replyInform.getContentObject();

					if (!avaliarReputacao(reputation)) {	
						propostasBoas.remove(reputation.getAgent());
					}
				} catch (UnreadableException ex) {
					ex.printStackTrace();
				}


				repliesReputation++;

				if(repliesReputation >= qtdPropostasPreAceitas){
					step = 3;
				}

			}else if(replyRefuse != null){
				repliesReputation++;
				
				if(repliesReputation >= qtdPropostasPreAceitas){
					step = 3;
				}
			}else{
				block();
			}
		}

		@Override
		public boolean done() {
			return false;
		}

		private boolean avaliarReputacao(Reputation reputacao){
			//TODO implementar
			if(reputacao.getReputation() >= 7){
				return true;
			}else{
				return false;
			}
		}

		private boolean avaliarProposta(Cupom cupom){
			System.out.println("passou aqui");
			if(cupom != null){
				Preferencia preferencia = preferencias.get(cupom.getTipoProduto());
				if(cupom.getPrecoProduto() <= preferencia.getPreco() && !cupom.getData().before(preferencia.getDataDeInicio()) 
						&& !cupom.getData().after(preferencia.getDataDeFim())){
					if(propostasBoas.containsKey(cupom.getTipoProduto()) && propostasBoas.get(cupom.getTipoProduto()).getPrecoProduto() > cupom.getPrecoProduto()){
						return true;
					}else if (!propostasBoas.containsKey(cupom.getTipoProduto())){
						return true;
					}else{
						return false;
					}
				}else{
					return false;
				}
			}else{
				return false;
			}

		}
	}

	private class BuscarVendedoresDeCupons extends TickerBehaviour{

		public BuscarVendedoresDeCupons(Agent a, long period) {
			super(a, period);
		}

		protected void onTick() {

			DFAgentDescription template = new DFAgentDescription();
			ServiceDescription sd = new ServiceDescription();
			sd.setType("vendedor-cupons");
			template.addServices(sd);
			try {
				DFAgentDescription[] result = DFService.search(myAgent, template);
				
				List<AID> agentesNovos = new ArrayList<AID>();
				if(result.length > 0){
					
					for(DFAgentDescription agente : result){
						if(!agentesVendedores.contains(agente.getName())){
							agentesNovos.add(agente.getName());
							System.out.println("Agente novo encontrado "+agente.getName().getLocalName());
						}
					}
					
					
					myAgent.addBehaviour(new RequestPerformer(myAgent, agentesNovos));
				}
				
				for (int i = 0; i < result.length; i++) {
					agentesVendedores.add(result[i].getName());
				}
				
			}
			catch (FIPAException fe) {
				fe.printStackTrace();
			}
		}
	}

	@Override
	public void setAgenteDeReputcaoCentralizado(AID name) {
		this.agenteDeReputacao = name;
	}
}