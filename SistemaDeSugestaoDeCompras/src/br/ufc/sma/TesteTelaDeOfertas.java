package br.ufc.sma;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class TesteTelaDeOfertas {
	
	public static void main(String[] args) {
		
		List<Cupom> cupons = new ArrayList<Cupom>();
		
		Calendar calendar = Calendar.getInstance();
		
	
		
		
		
		
		for(int i =0; i <= 10; i++){
			cupons.add(new Cupom("viagem para",45.0,calendar,"viagem", i));
		}
		
		TelaAvaliacaoDeOfertas tela = new TelaAvaliacaoDeOfertas(cupons);
		
	}

}
