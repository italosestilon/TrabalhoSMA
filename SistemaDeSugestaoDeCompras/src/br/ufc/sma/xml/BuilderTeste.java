package br.ufc.sma.xml;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.zip.DataFormatException;

import br.ufc.sma.Cupom;

public class BuilderTeste implements IBuilderCupom{

	@Override
	public List<Cupom> getCupons() {
		List<Cupom> cupons = new ArrayList<Cupom>();
		

		DateFormat formato = DateFormat.getDateInstance();
		
		try {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(formato.parse("31/12/2014"));
			cupons.add(new Cupom("New York", 400.0, calendar, "viagem"));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(formato.parse("30/12/2014"));
			cupons.add(new Cupom("New York", 450.0, calendar, "viagem"));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		try {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(formato.parse("29/12/2014"));
			cupons.add(new Cupom("New York", 550.0, calendar, "viagem"));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		return cupons;
	}

}
