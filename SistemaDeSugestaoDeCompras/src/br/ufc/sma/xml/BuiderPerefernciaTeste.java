package br.ufc.sma.xml;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import br.ufc.sma.Cupom;
import br.ufc.sma.Preferencia;

public class BuiderPerefernciaTeste implements IBuiderPeferencia {

	@Override
	public Map<String, Preferencia> getPrefencias() {
		
		Map<String,Preferencia> mapa = new HashMap<String, Preferencia>();
		DateFormat formato = DateFormat.getDateInstance();
		try {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(formato.parse("31/12/2014"));
			mapa.put("viagem",new Preferencia("viagem", 400.0, calendar, calendar));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return mapa;
	}

}
