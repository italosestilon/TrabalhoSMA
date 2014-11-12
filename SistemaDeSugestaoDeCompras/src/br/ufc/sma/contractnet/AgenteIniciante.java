package br.ufc.sma.contractnet;



import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import br.ufc.sma.comportamento.ComportamentoBuscarAgenteDeReputacaoCentralizado;



public class AgenteIniciante extends Agent implements IAgente {
	
	private final int INTERVALO_BUSCAR_VENDEDOR = 20000;
	private final int INTERVALO_BUSCAR_AGENTE_DE_REPUTACAO_CENTRALIZADO = 100;
	private List<AID> agentesVendedores;
    private AID agenteDeReputacao;
    
    
	protected void setup() { 
	  	Object[] args = getArguments();
	  	
	  	
	  	addBehaviour(new ComportamentoBuscarAgenteDeReputacaoCentralizado(this, INTERVALO_BUSCAR_AGENTE_DE_REPUTACAO_CENTRALIZADO));
	  	
	  	addBehaviour(new BuscarVendedoresDeCupons(this, INTERVALO_BUSCAR_VENDEDOR));
	  

	  	ACLMessage msg = new ACLMessage(ACLMessage.CFP);
	  	for (int i = 0; i < agentesVendedores.size(); ++i) {
  			msg.addReceiver(agentesVendedores.get(i));
  		}
	  	
		msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
		msg.setReplyByDate(new Date(System.currentTimeMillis() + 10000));
		//TODO:
		msg.setContent("dummy-action");
		
		addBehaviour(new Iniciador(this, msg));
	}


	@Override
	public void setAgenteDeReputcaoCentralizado(AID agente) {
		this.agenteDeReputacao = agente;
	}


	@Override
	public void adicionarAgenteDeReputacao(AID agente) {
		// TODO Auto-generated method stub
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
			}
			catch (FIPAException fe) {
				fe.printStackTrace();
			}
		}
	}
	

	private class Iniciador extends ContractNetInitiator{

		public Iniciador(Agent a, ACLMessage cfp) {
			super(a, cfp);
		}
		
		protected void handlePropose(ACLMessage propose, Vector v) {
			
		}
		
		protected void handleRefuse(ACLMessage refuse) {
			
			//TODO por debug
			
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
	
		
	}

}
