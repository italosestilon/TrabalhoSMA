package br.ufc.sma.contractnet;


import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import br.ufc.sma.Cupom;
import br.ufc.sma.comportamento.EnvioAcceptProposal;
import br.ufc.sma.comportamento.RecebimentoDeCFPs;
import br.ufc.sma.xml.BuilderCupom;
import br.ufc.sma.xml.IBuilderCupom;

public class AgenteParticipante extends Agent{
	private List<Cupom> cupons;
	private Map<AID,Cupom> compras;
	
	protected void setup(){
		
		
		Object args[] = getArguments();
		if(args.length > 0){
			lerCuponsDoXML((String) args[0]);
		}else{
			this.doDelete();
		}
		
		compras = new TreeMap<AID,Cupom>();
		cadastrarNoDF();
		
		
	}
	
	@Override
	protected void takeDown(){
		System.out.println("Erro nos parâmetros. O agente "+getAID().getLocalName()+" será finalizado");
	}
	
	public List<Cupom> buscarCupons(String nomeDoCupom) {
		List<Cupom> cuponsUteis = new ArrayList<Cupom>();
		for(Cupom c : this.cupons){
			if(c.getTipoProduto().equalsIgnoreCase(nomeDoCupom)){
				cuponsUteis.add(c);
			}
		}
		return cuponsUteis;
	}
	
	private void lerCuponsDoXML(String caminhoXML){
		IBuilderCupom builderCupom = new BuilderCupom(caminhoXML);
		this.cupons = builderCupom.getCupons();
		for(Cupom cupom : cupons){
			cupom.setVendedor(getAID());
		}
		
		addBehaviour(new RecebimentoDeCFPs(this));
		
		addBehaviour(new EnvioAcceptProposal(this));
	}
	
	public boolean cupomExiste(Cupom cupom){
		for(Cupom cupomAtual: this.cupons){
			if(cupomAtual.getId() == cupom.getId()){
				return true;
			}
		}
		return false;
	}
	
	public void comprarCupom(Cupom cupom, AID agente){
		this.compras.put(agente, cupom);
	}
	
	public void cadastrarNoDF(){
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("vendedor-cupons");
		sd.setName("vendedor");
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
	}
	
}
