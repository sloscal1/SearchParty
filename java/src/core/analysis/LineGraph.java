package core.analysis;

import java.awt.Dimension;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

public class LineGraph extends CompNode {

	public LineGraph(CompNode src){
		super(src);
	}
	
	@Override
	public Object execute() {
		Object input = src.execute();
		
		DefaultCategoryDataset data = new DefaultCategoryDataset();
		
		int pos = 0;
		for(Object obj : (Iterable<?>)input)
			data.addValue((Number)obj, "x-axis", pos++);
		
		JFreeChart chart = ChartFactory.createLineChart((String)getInput("TITLE"), 
				(String)getInput("X-AXIS"), 
				(String)getInput("Y-AXIS"), 
				data);
		
		JFrame frame = new JFrame((String)getInput("TITLE"));
		frame.setSize(new Dimension(600, 400));
		ChartPanel panel = new ChartPanel(chart);
		panel.setPreferredSize(new Dimension(600, 400));
		frame.setContentPane(panel);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		
		return null;
	}

}
