package br.ufc.sma.contractnet;


import java.util.List;

import jade.core.Agent;
import br.ufc.sma.comportamento.ComportamentoBuscarAgenteDeReputacaoCentralizado;
import br.ufc.sma.comportamento.EnvioAcceptProposal;
import br.ufc.sma.comportamento.RecebimentoDeCFPs;
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
		
		addBehaviour(new RecebimentoDeCFPs(this));
		
		addBehaviour(new EnvioAcceptProposal(this));
	}
	
	public Cupom buscarCupom(String nomeDoCupom) {
		Cupom cupom = null;
		for(Cupom c : this.cupons){
			if(c.getNomeProduto().equalsIgnoreCase(nomeDoCupom) || c.getTipoProduto().equalsIgnoreCase(nomeDoCupom)){
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
	
	public boolean cupomExiste(Cupom cupom){
		for(Cupom cupomAtual: this.cupons){
			if(cupomAtual.equals(cupom)){
				return true;
			}
		}
		return false;
	}
	
	public void comprarCupom(Cupom cupom){
		
	}
	
}
