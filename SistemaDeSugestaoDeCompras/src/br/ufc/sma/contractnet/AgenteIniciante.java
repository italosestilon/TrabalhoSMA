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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.ufc.sma.Cupom;
import br.ufc.sma.Preferencia;
import br.ufc.sma.comportamento.ComportamentoBuscarAgenteDeReputacaoCentralizado;
import br.ufc.sma.reputacao.Reputation;

public class AgenteIniciante extends Agent {

	private String targetBookTitle;

	private static final int INTERVALO_ENVIAR_MENSAGEM = 2000;

	private final int INTERVALO_BUSCAR_VENDEDOR = 20000;

	private final int INTERVALO_BUSCAR_AGENTE_DE_REPUTACAO_CENTRALIZADO = 100;

	private List<AID> agentesVendedores;

	private AID agenteDeReputacao;

	private Map<String, Preferencia> preferencias;

	private Map<AID,Cupom> propostasBoas;

	protected void setup() {

		propostasBoas = new HashMap<AID, Cupom>();
		
		preferencias = new HashMap<String, Preferencia>();

		System.out.println("Olá! O agente comprador "+getAID().getName()+" está pronto.");

		// Recebe o titulo do livro a ser comprado
		Object[] args = getArguments();
		if (args != null && args.length > 0) {

			targetBookTitle = (String) args[0];

			System.out.println("O livro a ser comprado é "+targetBookTitle);

			addBehaviour(new ComportamentoBuscarAgenteDeReputacaoCentralizado(this, INTERVALO_BUSCAR_AGENTE_DE_REPUTACAO_CENTRALIZADO));         

			// TickerBehaviour para enviar um request a cada minuto
			addBehaviour(new BuscarVendedoresDeCupons(this, INTERVALO_BUSCAR_VENDEDOR));
		}
		else {
			// Finaliza o Agente
			System.out.println("Nenhum livro informado");
			doDelete();
		}
	}
	
	public Iterable<Cupom> getCupons(){
		return propostasBoas.values();
	}

	protected void takeDown() {
		System.out.println("Agente comprador "+getAID().getName()+" finalizado.");
	}

	/*
	 * Classe interna RequestPerformer.
	 * Comportamento usado pelo comprador para encontrar vendedores do livro procurado
	 */
	private class RequestPerformer extends Behaviour {
		private int repliesCnt = 0; // Número de respostas de vendedores

		private int repliesReputation = 0;

		private int qtdPropostasPreAceitas = 0; //Propostas para analisar a reputacao do vendedor

		private MessageTemplate mt; // Template para receber respostas

		private MessageTemplate mtReputation;

		private Cupom cupomEscolhido = null;

		private int step = 0;

		public void action() {
			switch (step) {
			case 0:
				// Envia a cfp (call for proposals) para todos os vendedores
				passoEnviarCFP();

				break;

			case 1:
				// Recebe todas as propostas/rejeições dos agentes vendedores

				//TODO analisar respostas recebida
				passoAnalisarPropostas();

				break;

			case 2:
				// Recebe resposta do Reputation Agent
				passoAnalisarProposta();

				break;
			case 3:
				//TODO esperando a resposta da tela
				passoEnviarMensagemDeCompra();


				break;

			case 4:

				PassoAvaliarVendedor();
				break;
			}

		}

		private void PassoAvaliarVendedor() {
			ACLMessage reply = myAgent.receive(mt);

			if (reply != null) {


				// Compra realizada
				System.out.println(targetBookTitle+" foi comprado com sucesso do agente "+reply.getSender().getName());

				ACLMessage informReputation = new ACLMessage();

				Reputation reputation = new Reputation(10, cupomEscolhido.getVendedor());
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



				step = 5;
			}
			else {
				block();
			}
		}

		private void passoEnviarMensagemDeCompra() {
			ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);

			order.addReceiver(cupomEscolhido.getVendedor());

			try {
				order.setContentObject(cupomEscolhido);

				order.setConversationId("venda-cupom");

				order.setReplyWith("order"+System.currentTimeMillis());

				myAgent.send(order);
				// preparando o template para receber a resposta
				mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
						MessageTemplate.and(MessageTemplate.MatchConversationId("venda-cupom"),
								MessageTemplate.MatchInReplyTo(order.getReplyWith())));

				step = 4;


			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		private void passoAnalisarPropostas() {
			ACLMessage reply = myAgent.receive(mt);

			if (reply != null) {

				Cupom cupom = null;

				try {
					cupom = (Cupom)reply.getContentObject();
				} catch (UnreadableException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				if(cupom != null && avaliarProposta(cupom)){

					propostasBoas.put(cupom.getVendedor(), cupom);

					qtdPropostasPreAceitas++;

					ACLMessage requestReputation = new ACLMessage();

					requestReputation.setPerformative(ACLMessage.REQUEST);

					requestReputation.setContent(reply.getSender().getName());

					requestReputation.addReceiver(agenteDeReputacao);

					requestReputation.setConversationId("informe-de-reputacao");

					requestReputation.setReplyWith("reputation"+System.currentTimeMillis());

					myAgent.send(requestReputation);

					mtReputation = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
							MessageTemplate.MatchConversationId("informe-de-reputacao"));
				}


				repliesCnt++;

				if (repliesCnt >= agentesVendedores.size()) {
					//Não há mais vendedores
					step = 2; 
				}
			}

			else {

				block();
			}
		}

		private void passoEnviarCFP() {
			
			for (Preferencia preferencia : preferencias.values()){
				
				ACLMessage cfp = new ACLMessage(ACLMessage.CFP);

				for (int i = 0; i < agentesVendedores.size(); ++i) {

					cfp.addReceiver(agentesVendedores.get(i));
				} 

				cfp.setContent(preferencia.getTipo());

				cfp.setConversationId("venda-cupom");

				cfp.setReplyWith("cfp"+System.currentTimeMillis()); // valor unico

				myAgent.send(cfp);
				
				mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.PROPOSE),
						MessageTemplate.and(MessageTemplate.MatchConversationId("venda-cupom"),
								MessageTemplate.MatchInReplyTo(cfp.getReplyWith())));
			}

			step = 1;
		}

		private void passoAnalisarProposta() {
			ACLMessage replyReputation = myAgent.receive(mtReputation);

			if(replyReputation != null){


				try {

					Reputation reputation = (Reputation) replyReputation.getContentObject();

					float reputationValue = reputation.getReputation();

					Cupom cupom = propostasBoas.get(reputation.getAgent());

					//Logica de calcular melhor cupom e melhor jogador
					if (!avaliarReputacao(reputation )) {
						//Remove o cupom do mapa
						propostasBoas.remove(reputation.getAgent());
					}
				} catch (UnreadableException ex) {
					ex.printStackTrace();
				}


				repliesReputation++;

				if(repliesReputation >= qtdPropostasPreAceitas){
					step = 3;
				}

			}else{
				block();
			}
		}

		public boolean done() {
			if (step == 3) {
				System.out.println("Falha: "+targetBookTitle+" não encontrado para vender");
			}
			return (step == 3 || step == 5);
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

			if(cupom != null){
				Preferencia preferencia = preferencias.get(cupom.getTipoProduto());
				if(cupom.getPrecoProduto() >= preferencia.getPreco() && !cupom.getData().before(preferencia.getDataDeInicio()) 
						&& !cupom.getData().after(preferencia.getDataDeFim())){
					return true;
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
				System.out.println("Os seguintes vendedores foram encontrados:");
				agentesVendedores = new ArrayList<AID>();
				for (int i = 0; i < result.length; ++i) {
					agentesVendedores.add(result[i].getName());
				}

				myAgent.addBehaviour(new RequestPerformer());
			}
			catch (FIPAException fe) {
				fe.printStackTrace();
			}
		}
	}
}
