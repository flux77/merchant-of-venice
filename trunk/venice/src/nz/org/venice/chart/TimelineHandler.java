package nz.org.venice.chart;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JLabel;
import javax.swing.JScrollBar;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import nz.org.venice.util.Locale;
import nz.org.venice.util.TradingDate;

/**
 *  A class that contains the toolbar implemented to handle the timeline in a chart view
 *  This adds a toolbar with a JScrollbar component to the chart and handles the events
 *  To move the time being viewed when zoomed in
 *  
 * @author Guillermo Bonvehi - gbonvehi
 *
 */
class TimelineHandler implements ChangeListener, MouseWheelListener {
	/**
	 * 
	 */
	private final ChartModule chartModule;
	private JScrollBar bar;
	// Boolean to not run stateChanged while updating the value using Recalculate
	private boolean pause = false;
	// TradingDate holding the minimum value (cache it)
	private TradingDate minX = null;
	
	public TimelineHandler(ChartModule chartModule) {
		this.chartModule = chartModule;
		Chart chart = this.chartModule.getChart();
		
		JToolBar p = new JToolBar(SwingConstants.HORIZONTAL);

		JLabel tl = new JLabel(Locale.getString("TIMELINE"));

		bar = new JScrollBar(JScrollBar.HORIZONTAL);
		TradingDate minX = (TradingDate)chart.calculateStartX();
		TradingDate maxX = (TradingDate)chart.calculateEndX();
		BoundedRangeModel brm = new DefaultBoundedRangeModel();
		brm.setMaximum(maxX.getDifference(minX));    		
		brm.setMinimum(0);
		brm.setValue(0);
		brm.addChangeListener(this);
		brm.setExtent(maxX.getDifference(minX));
		bar.setModel(brm);
		bar.addMouseWheelListener(this);
		bar.setPreferredSize(new Dimension(200,20));
		
		p.add(tl);
		p.add(bar);
		this.chartModule.add(p, BorderLayout.NORTH);
		this.chartModule.updateUI();
		this.minX = minX;
	}
	
	// Call this method when the timeline was modified (zoomed in/out) to recalculate values
	public void Recalculate() {
		pause = true;
		Chart chart = this.chartModule.getChart();
		BoundedRangeModel brm = bar.getModel(); 
		int day = this.minX.getDifference((TradingDate)chart.getStartX());
		brm.setExtent(0);
		brm.setValue(day);
		brm.setExtent(chart.getSpanDays());
		pause = false;
	}

  //@Override
  public void stateChanged(ChangeEvent arg0) {
    if (!pause)
      this.chartModule.getChart().moveTo(bar.getValue());
  }

  //@Override
  public void mouseWheelMoved(MouseWheelEvent event) {
	  if (event.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
		  BoundedRangeModel brm = bar.getModel();
          int totalScrollAmount = event.getUnitsToScroll();
          brm.setValue(brm.getValue() + totalScrollAmount);
      }
  }
}