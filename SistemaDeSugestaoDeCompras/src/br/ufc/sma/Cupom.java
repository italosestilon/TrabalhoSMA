package br.ufc.sma;

import java.util.Calendar;

public class Cupom{
	private String nomeProduto;
	private Double precoProduto;
	private Calendar data;
	
	public Cupom(String nomeProduto, Double precoProduto, Calendar data){
		this.nomeProduto = nomeProduto;
		this.precoProduto = precoProduto;
		this.data = data;
	}
	
	public String getNomeProduto(){
		return this.nomeProduto;
	}
	
	public Double getPrecoProduto(){
		return this.precoProduto;
	}
	
	public Calendar getData(){
		return this.data;
	}
}
