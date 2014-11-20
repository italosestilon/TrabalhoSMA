package br.ufc.sma.contractnet;


import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

import java.util.ArrayList;
import java.util.List;

import br.ufc.sma.Cupom;
import br.ufc.sma.comportamento.EnvioAcceptProposal;
import br.ufc.sma.comportamento.RecebimentoDeCFPs;
import br.ufc.sma.xml.BuilderTeste;
import br.ufc.sma.xml.IBuilderCupom;

public class AgenteParticipante extends Agent{
	private List<Cupom> cupons;
	
	protected void setup(){
		
		Object args[] = getArguments();
		if(args.length > 0){
			lerCuponsDoXML((String) args[0]);
		}else{
			System.out.println("Erro nos parâmetros!");
			this.doDelete();
		}
		
		cadastrarNoDF();
		
		addBehaviour(new RecebimentoDeCFPs(this));
		
		addBehaviour(new EnvioAcceptProposal(this));
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
		IBuilderCupom builderCupom = new BuilderTeste();
		this.cupons = builderCupom.getCupons();
		for(Cupom cupom : cupons){
			cupom.setVendedor(getAID());
		}
	}
	
	public boolean cupomExiste(Cupom cupom){
		for(Cupom cupomAtual: this.cupons){
			if(cupomAtual.getId() == cupom.getId()){
				return true;
			}
		}
		return false;
	}
	
	public void comprarCupom(Cupom cupom){
		
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
