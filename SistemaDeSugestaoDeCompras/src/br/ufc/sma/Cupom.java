package br.ufc.sma;

import jade.core.AID;
import jade.util.leap.Serializable;

import java.util.Calendar;

public class Cupom implements Serializable{
	private String nomeProduto;
	private Double precoProduto;
	private Calendar data;
	private String tipoProduto;
	private AID vendedor;
	
	public Cupom(String nomeProduto, Double precoProduto, Calendar data, String tipoProduto){
		this.nomeProduto = nomeProduto;
		this.precoProduto = precoProduto;
		this.data = data;
		this.tipoProduto = tipoProduto;
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
	
	public String getTipoProduto(){
		return this.tipoProduto;
	}
	
	public void setVendedor(AID vendedor){
		this.vendedor = vendedor;
	}
	
	public AID getVendedor(){
		return this.vendedor;
	}
}
