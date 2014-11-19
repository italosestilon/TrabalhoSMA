package br.ufc.sma;


import java.awt.BorderLayout; 
import java.util.Collection;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import br.ufc.sma.contractnet.AgenteIniciante;


public class TelaAvaliacaoDeOfertas extends JFrame{
	private AgenteIniciante agente;
	
	private Collection<Cupom> collectionCupons;
	private DefaultTableModel defaul;
	
	public TelaAvaliacaoDeOfertas(AgenteIniciante agente) {
		this.agente = agente;
		getContentPane().setLayout(null);
		this.setSize(760, 700);
		this.setLocationRelativeTo(null);
		this.setResizable(false);
		initComponents();
	}
	
	public TelaAvaliacaoDeOfertas(Collection<Cupom> cupons) {
		this.collectionCupons = cupons;
		getContentPane().setLayout(null);
		this.setSize(760, 700);
		this.setLocationRelativeTo(null);
		this.setResizable(false);
		
		initComponents();
		this.setVisible(true);
	}
	
	private void initComponents(){
		//Collection<Cupom> collectionCupons = this.agente.getCupons();
		Object[][] dados = new Object[collectionCupons.size()][6]; 
		int index = 0;
		

		String[] colunas = {"Selecionar", "Nome", "Valor", "Tipo do item", "Nome do vendedor", "Data"};
		
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setBounds(30, 80, 700, 600);
		
		
		JTable table = new JTable(dados, colunas);
		table.setBounds(0, 0, 700, 600);
		
		defaul = new DefaultTableModel(
				new Object[][] {
				{null, null, null, null, null, null},
				},
				colunas
				) {
				Class[] columnTypes = new Class[] {
				Boolean.class, Object.class, Object.class, Object.class, Object.class, Object.class
				};
				public Class getColumnClass(int columnIndex) {
				return columnTypes[columnIndex];
				}
				};
		
				
				for(Cupom cupom: collectionCupons){
					
					dados[index][0] = null;
					dados[index][1] = cupom.getNomeProduto();
					dados[index][2] = String.format(".2f", cupom.getPrecoProduto());
					dados[index][3] = cupom.getTipoProduto();
					dados[index][4] = cupom.getVendedor();
					dados[index][5] = cupom.getData().get(cupom.getData().DAY_OF_MONTH);
					
					defaul.addRow(dados);
					
					index++;
					
				}
		table.setModel(defaul);
		
		JScrollPane scrollbar = new JScrollPane(table);
		scrollbar.setBounds(743, 32, 17, 536);
		panel.add(scrollbar, BorderLayout.CENTER);
		
		getContentPane().add(panel);
	}
}
