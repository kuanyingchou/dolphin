package test;

import gui.IntensityHistory;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;


import api.audio.PitchListener;
import api.audio.SoundAnalyzer;
import api.util.Pair;
import api.util.Util;

public class PitchTester extends JComponent implements PitchListener {
	SoundAnalyzer sa=null;
	final IntensityHistory ih=new IntensityHistory();
	final JLabel output=new JLabel("N/A");
	private double currentPitch=-1;
	static {
	   SoundAnalyzer.settings.ceiling=500;
	   SoundAnalyzer.settings.floor=70;
	}
	public PitchTester() {
		setLayout(new BorderLayout());
		
		
		//[ up
		final JTable record=new JTable();
		record.setModel(new RecordModel());
		record.setAutoCreateRowSorter(true);
		add(new JScrollPane(record), BorderLayout.NORTH);
		
		//[ center
		output.setOpaque(true);
		//output.setBackground(Color.black);
		//output.setForeground(Color.green);
		output.setFont(output.getFont().deriveFont(50.0f));
		add(output, BorderLayout.CENTER);
		
		//[ bottom
		final JPanel bottomPane=new JPanel();
		bottomPane.setLayout(new FlowLayout());
		
		bottomPane.add(ih);
		
		final JButton startButton=new JButton("Start");
		startButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				start();
			}
		});
		bottomPane.add(startButton);
		
		final JButton stopButton=new JButton("Stop");
		stopButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				stop();
			}
		});
		bottomPane.add(stopButton);
		
		final JButton saveButton=new JButton("Save");
		saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				final RecordModel rm=(RecordModel)record.getModel();
				String name=null;
				do {
					if(name!=null) {
						JOptionPane.showMessageDialog(
								null, "Please Choose Another Name.");
					}
					name=JOptionPane.showInputDialog("Please Enter Your Name");
				} while((name!=null && name.isEmpty()) || rm.contains(name));
				if(name==null) return;
				rm.addRow(name, currentPitch);
				record.revalidate();
			}
		});
		bottomPane.add(saveButton);
		
		add(bottomPane, BorderLayout.SOUTH);
	}
	public void start() {
		sa=new SoundAnalyzer();
		sa.addIntensityListener(ih);
		sa.addFrequencyListener(this);
		sa.start();
	}
	public void stop() {
		if(sa!=null) {
			sa.stopRecording();
		}
	}
	
	private final DecimalFormat df=new DecimalFormat("0.00");
	@Override
	public void gotPitch(double pitch, double len) {
		currentPitch=pitch;
		final int p=(int)Math.round(Util.frequencyToPitch(pitch));
		//System.out.println(Util.getPitchName(p));
		//Util.getPitchName(p)
		output.setText(df.format(pitch)+"Hz("+Util.getPitchName(p)+")");
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		final JFrame jf=new JFrame("Pitch Tester");
		jf.setContentPane(new PitchTester());
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jf.pack();
		jf.setVisible(true);
	}
	

}
class RecordModel extends AbstractTableModel {
	private final String[] columnNames={"Name", "Pitch"};
	java.util.List<Pair<String, Double>> values
		=new ArrayList<Pair<String, Double>>();
//	private final Map<String, Double> values
//		=new HashMap<String, Double>();
	
	public String getColumnName(int column) {
		return columnNames[column];
	}
	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	@Override
	public int getRowCount() {
		return values.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		final Pair<String, Double> row=values.get(rowIndex);
		return columnIndex==0?row.getLeft():row.getRight();
	}
	
	public void addRow(String name, Double pitch) {
		values.add(new Pair(name, pitch));
	}
	
	public boolean contains(String name) {
		for(Pair p: values) {
			if(p.getLeft().equals(name)) return true;
		}
		return false;
	}
	
}
