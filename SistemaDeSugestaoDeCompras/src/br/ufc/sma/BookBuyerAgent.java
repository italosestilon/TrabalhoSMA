

package br.ufc.sma;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.UnreadableException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import br.ufc.sma.reputacao.Reputation;

public class BookBuyerAgent extends Agent {
	private String targetBookTitle;
	private AID[] sellerAgents;
    private AID reputationAgent;
	protected void setup() {
		System.out.println("Olá! O agente comprador "+getAID().getName()+" está pronto.");

		// Recebe o titulo do livro a ser comprado
		Object[] args = getArguments();
		if (args != null && args.length > 0) {
			targetBookTitle = (String) args[0];
			System.out.println("O livro a ser comprado é "+targetBookTitle);

                        addBehaviour(new TickerBehaviour(this, 100){
                            @Override
                            protected void onTick() {
                                // Procurando agente de reputação
                                DFAgentDescription template = new DFAgentDescription();
                                ServiceDescription sd = new ServiceDescription();
                                sd.setType("agent-reputation");
                                template.addServices(sd);
                                try {
                                    DFAgentDescription[] result = DFService.search(myAgent, template);
                                    if (result.length > 0) {
                                        reputationAgent = result[0].getName();
                                        System.out.println("Agente de reputação " + reputationAgent.getName());
                                        myAgent.removeBehaviour(this);
                                    } else {
                                    }
                                } catch (FIPAException fe) {
                                    fe.printStackTrace();
                                }
                            }
                        });
                        
			// TickerBehaviour para enviar um request a cada minuto
			addBehaviour(new TickerBehaviour(this, 20000) {
				protected void onTick() {
					System.out.println("Tentando comprar "+targetBookTitle);
					// Atualiza a lista de agentes vendedores
					DFAgentDescription template = new DFAgentDescription();
					ServiceDescription sd = new ServiceDescription();
					sd.setType("book-selling");
					template.addServices(sd);
					try {
						DFAgentDescription[] result = DFService.search(myAgent, template); 
						System.out.println("Os seguintes vendedores foram encontrados:");
						sellerAgents = new AID[result.length];
						for (int i = 0; i < result.length; ++i) {
							sellerAgents[i] = result[i].getName();
							System.out.println(sellerAgents[i].getName());
						}
					}
					catch (FIPAException fe) {
						fe.printStackTrace();
					}

					// Adiciona um comportamento RequestPerformer
					myAgent.addBehaviour(new RequestPerformer());
				}
			} );
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
				for (int i = 0; i < sellerAgents.length; ++i) {
					cfp.addReceiver(sellerAgents[i]);
				} 
				cfp.setContent(targetBookTitle);
				cfp.setConversationId("book-trade");
				cfp.setReplyWith("cfp"+System.currentTimeMillis()); // valor unico
				myAgent.send(cfp);
				// Prepara o template para receber propostas
				mt = MessageTemplate.and(MessageTemplate.MatchConversationId("book-trade"),
						MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
				step = 1;
				break;
			case 1:
				// Recebe todas as propostas/rejeições dos agentes vendedores
				ACLMessage reply = myAgent.receive(mt);
				if (reply != null) {
					if (reply.getPerformative() == ACLMessage.PROPOSE) {
						
                                                
                                                ACLMessage requestReputation = new ACLMessage();
                                                requestReputation.setPerformative(ACLMessage.REQUEST);
                                                requestReputation.setContent(reply.getSender().getName());
                                                requestReputation.addReceiver(reputationAgent);
                                                requestReputation.setConversationId("request-reputation");
                                                requestReputation.setReplyWith("reputation"+System.currentTimeMillis());
                                                myAgent.send(requestReputation);
                                                
                                                mtReputation = MessageTemplate.MatchConversationId("request-reputation");
                                               
					}
					repliesCnt++;
					if (repliesCnt >= sellerAgents.length) {
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
                                ACLMessage replyReputation = myAgent.receive(mtReputation);
                                
                                if(replyReputation != null){
                                    
                  
                                    try {
                                        Reputation reputation = (Reputation) replyReputation.getContentObject();
                                        float reputationValue = reputation.getReputation();
                                       
                                        if (bestSeller == null || bestReputation < reputationValue) {
                                            // Melhor oferta até o momento
                                            bestReputation = reputationValue;
                                            bestSeller = reputation.getAgent();
                                        }
                                    } catch (UnreadableException ex) {
                                        ex.printStackTrace();
                                    }
     
                                 
                                    repliesReputation++;
                                    
                                    if(repliesReputation >= sellerAgents.length){
                                        step = 3;
                                    }
                                    
                                }else{
                                    block();
                                }
                                
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
                                                    informReputation.addReceiver(reputationAgent);
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

		public boolean done() {
			if (step == 3 && bestSeller == null) {
				System.out.println("Falha: "+targetBookTitle+" não encontrado para vender");
			}
			return ((step == 3 && bestSeller == null) || step == 5);
		}
	} 
}
