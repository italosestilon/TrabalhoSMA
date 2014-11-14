package br.ufc.sma;


import java.awt.BorderLayout;
import java.util.Iterator;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import java.util.Collection;

import br.ufc.sma.contractnet.AgenteIniciante;


public class TelaAvaliacaoDeOfertas extends JFrame{
	private AgenteIniciante agente;
	
	public TelaAvaliacaoDeOfertas(AgenteIniciante agente) {
		this.agente = agente;
		getContentPane().setLayout(null);
		this.setSize(760, 700);
		this.setLocationRelativeTo(null);
		this.setResizable(false);
		initComponents();
	}
	
	private void initComponents(){
		Collection<Cupom> collectionCupons = this.agente.getCupons();
		Object[][] dados = new Object[collectionCupons.size()][3]; 
		int index = 0;
		for(Cupom cupom: collectionCupons){
			dados[index][0] = new JCheckBox();
			dados[index][1] = cupom.getNomeProduto();
			dados[index][2] = String.format(".2f", cupom.getPrecoProduto());
			dados[index][3] = cupom.getTipoProduto();
			dados[index][4] = cupom.getVendedor().getName();
			dados[index][5] = cupom.getData();
			index++;
		}

		String[] colunas = {"Selecionar", "Nome", "Valor", "Tipo do item", "Nome do vendedor", "Data"};
		
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setBounds(30, 80, 700, 600);
		
		
		JTable table = new JTable(dados, colunas);
		table.setBounds(0, 0, 700, 600);
		
		JScrollPane scrollbar = new JScrollPane(table);
		scrollbar.setBounds(743, 32, 17, 536);
		panel.add(scrollbar, BorderLayout.CENTER);
		
		getContentPane().add(panel);
	}
}
