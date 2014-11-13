

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
import java.util.List;

import br.ufc.sma.Cupom;
import br.ufc.sma.Preferencia;
import br.ufc.sma.comportamento.ComportamentoBuscarAgenteDeReputacaoCentralizado;
import br.ufc.sma.reputacao.Reputation;

public class BookBuyerAgent extends Agent {

	private String targetBookTitle;


	private static final int INTERVALO_ENVIAR_MENSAGEM = 2000;
	private final int INTERVALO_BUSCAR_VENDEDOR = 20000;
	private final int INTERVALO_BUSCAR_AGENTE_DE_REPUTACAO_CENTRALIZADO = 100;
	private List<AID> agentesVendedores;
	private AID agenteDeReputacao;
	private Preferencia preferencia;

	protected void setup() {
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

	protected void takeDown() {
		System.out.println("Agente comprador "+getAID().getName()+" finalizado.");
	}

	/*
	 * Classe interna RequestPerformer.
	 * Comportamento usado pelo comprador para encontrar vendedores do livro procurado
	 */
	private class RequestPerformer extends Behaviour {
		private AID bestSeller; // Agente com melhor reputação
		private float bestReputation;  // Melhor preço
		private int repliesCnt = 0; // Número de respostas de vendedores
		private int repliesReputation = 0;
		private MessageTemplate mt; // Template para receber respostas
		private MessageTemplate mtReputation;
		private int step = 0;

		public void action() {
			switch (step) {
			case 0:
				// Envia a cfp (call for proposals) para todos os vendedores
				ACLMessage cfp = new ACLMessage(ACLMessage.CFP);

				for (int i = 0; i < agentesVendedores.size(); ++i) {

					cfp.addReceiver(agentesVendedores.get(i));
				} 

				cfp.setContent(targetBookTitle);

				cfp.setConversationId("book-trade");

				cfp.setReplyWith("cfp"+System.currentTimeMillis()); // valor unico

				myAgent.send(cfp);

				// Prepara o template para receber propostas
				mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.PROPOSE),
						MessageTemplate.and(MessageTemplate.MatchConversationId("venda-cupom"),
								MessageTemplate.MatchInReplyTo(cfp.getReplyWith())));

				step = 1;

				break;

			case 1:
				// Recebe todas as propostas/rejeições dos agentes vendedores
				
				//TODO analisar respostas recebida
				ACLMessage reply = myAgent.receive(mt);

				if (reply != null) {

					ACLMessage requestReputation = new ACLMessage();

					requestReputation.setPerformative(ACLMessage.REQUEST);

					requestReputation.setContent(reply.getSender().getName());

					requestReputation.addReceiver(agenteDeReputacao);

					requestReputation.setConversationId("informe-de-reputacao");

					requestReputation.setReplyWith("reputation"+System.currentTimeMillis());

					myAgent.send(requestReputation);

					mtReputation = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
							MessageTemplate.MatchConversationId("informe-de-reputacao"));


					repliesCnt++;
					
					if (repliesCnt >= agentesVendedores.size()) {
						//Não há mais vendedores
						step = 2; 
					}
				}
				
				else {
					
					block();
				}
				
				break;
				
			case 2:
				// Recebe resposta do Reputation Agent
				passoAnalisarProposta();

				break;
			case 3:
				// Envia mensagem para o vendedor com melhor valor
				ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
				order.addReceiver(bestSeller);
				order.setContent(targetBookTitle);
				order.setConversationId("book-trade");
				order.setReplyWith("order"+System.currentTimeMillis());
				myAgent.send(order);
				// preparando o template para receber a resposta
				mt = MessageTemplate.and(MessageTemplate.MatchConversationId("book-trade"),
						MessageTemplate.MatchInReplyTo(order.getReplyWith()));
				step = 4;
				break;
			case 4:      
				reply = myAgent.receive(mt);
				if (reply != null) {
					if (reply.getPerformative() == ACLMessage.INFORM) {
						// Compra realizada
						System.out.println(targetBookTitle+" foi comprado com sucesso do agente "+reply.getSender().getName());
						System.out.println("Reputação = "+bestReputation);

						ACLMessage informReputation = new ACLMessage();
						
						Reputation reputation = new Reputation(10, bestSeller);
						try {
							informReputation.setContentObject(reputation);
							informReputation.setPerformative(ACLMessage.INFORM);
							informReputation.addReceiver(agenteDeReputacao);
							myAgent.send(informReputation);
						} catch (IOException ex) {
							ex.printStackTrace();
						}
						myAgent.doDelete();
					}
					else {
						System.out.println("Falha: O livro já foi vendido.");
					}

					step = 5;
				}
				else {
					block();
				}
				break;
			}        
		}

		private void passoAnalisarProposta() {
			ACLMessage replyReputation = myAgent.receive(mtReputation);

			if(replyReputation != null){


				try {
					
					Reputation reputation = (Reputation) replyReputation.getContentObject();
					
					float reputationValue = reputation.getReputation();

					//Logica de calcular melhor cupom e melhor jogador
					if (avaliacaoDeMelhorVendedor(reputation, cupom)) {
						// Melhor oferta até o momento
						bestReputation = reputationValue;
						bestSeller = reputation.getAgent();
					}
				} catch (UnreadableException ex) {
					ex.printStackTrace();
				}


				repliesReputation++;

				if(repliesReputation >= agentesVendedores.size()){
					step = 3;
				}

			}else{
				block();
			}
		}

		public boolean done() {
			if (step == 3 && bestSeller == null) {
				System.out.println("Falha: "+targetBookTitle+" não encontrado para vender");
			}
			return ((step == 3 && bestSeller == null) || step == 5);
		}

		private boolean avaliacaoDeMelhorVendedor(Reputation reputacao, Cupom cupom){
			//TODO implementar
			return true;
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
