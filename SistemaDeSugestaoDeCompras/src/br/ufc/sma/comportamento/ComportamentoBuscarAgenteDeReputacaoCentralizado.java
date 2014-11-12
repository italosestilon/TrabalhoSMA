package br.ufc.sma.comportamento;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import br.ufc.sma.contractnet.IAgente;

public class ComportamentoBuscarAgenteDeReputacaoCentralizado extends TickerBehaviour{

	public ComportamentoBuscarAgenteDeReputacaoCentralizado(Agent a, long period) {
		super(a, period);
	}

	@Override
	protected void onTick() {
		DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("agente-reputacao-centralizado");
        template.addServices(sd);
        try {
            DFAgentDescription[] result = DFService.search(myAgent, template);
            if (result.length > 0) {
            	
            	((IAgente) myAgent).setAgenteDeReputcaoCentralizado(result[0].getName());
               
                myAgent.removeBehaviour(this);
                
            } else {
            }
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
		
	}

}
