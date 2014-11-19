package br.ufc.sma;

import jade.util.leap.Serializable;

import java.util.Calendar;

public class Preferencia implements Serializable{

	private String tipo;
	
	private double preco;
	
	private Calendar dataDeInicio;
	
	private Calendar dataDeFim;
	
	public Preferencia(String tipo, double preco, Calendar dataDeInicio, Calendar dataDeFim) {
		this.tipo = tipo;
		this.preco = preco;
		this.dataDeInicio = dataDeInicio;
		this.dataDeFim = dataDeFim;
	}
	public Calendar getDataDeInicio() {
		return dataDeInicio;
	}
	
	public Calendar getDataDeFim(){
		return this.dataDeFim;
	}

	public double getPreco() {
		return preco;
	}

	public String getTipo() {
		return tipo;
	}


}
