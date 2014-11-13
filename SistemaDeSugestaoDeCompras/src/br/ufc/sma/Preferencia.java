package br.ufc.sma;

import jade.util.leap.Serializable;

import java.util.Calendar;

public class Preferencia implements Serializable{

	private String tipo;
	
	private Double preco;
	
	private Calendar dataDeInicio;
	
	private Calendar dataDeFim;

	public Calendar getDataDeInicio() {
		return dataDeInicio;
	}
	
	public Calendar getDataDeFim(){
		return this.dataDeFim;
	}

	public Double getPreco() {
		return preco;
	}

	public String getTipo() {
		return tipo;
	}


}
