package br.ufc.sma.xml;

import java.io.File;
import java.util.List;

import br.ufc.sma.Cupom;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class BuilderCupom implements IBuilderCupom{
	
	private XStream stream;
	private File arquivo;
	
	public BuilderCupom(String caminhoDoArquivo){
		
		configurarStream();
		
		arquivo = new File("xml/"+caminhoDoArquivo);
	}
	
	private void configurarStream(){
		stream = new XStream(new DomDriver());
		stream.alias("List", List.class);
	}
	
	public List<Cupom> getCupons(){
		
		return (List<Cupom>)stream.fromXML(arquivo);
	}
	
}
