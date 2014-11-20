package br.ufc.sma.xml;

import java.io.File;
import java.util.Map;

import br.ufc.sma.Preferencia;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class BuilderPreferencia implements IBuiderPeferencia{
	
	private XStream stream;
	private File arquivo;
	
	public BuilderPreferencia(String caminhoDoArquivo){
		
		configurarStream();
		
		arquivo = new File(caminhoDoArquivo);
	}
	
	private void configurarStream(){
		stream = new XStream(new DomDriver());
		stream.alias("Map", Map.class);
	}
	
	@Override
	public Map<String, Preferencia> getPrefencias() {
		
		return (Map<String,Preferencia>)stream.fromXML(arquivo);
	}

}
