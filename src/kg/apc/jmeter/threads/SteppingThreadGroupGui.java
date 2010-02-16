package kg.apc.jmeter.threads;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import kg.apc.jmeter.vizualizers.AbstractGraphRow;
import kg.apc.jmeter.vizualizers.DateTimeRenderer;
import kg.apc.jmeter.vizualizers.GraphPanelChart;
import kg.apc.jmeter.vizualizers.GraphRowAverages;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.threads.JMeterThread;
import org.apache.jmeter.threads.gui.AbstractThreadGroupGui;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.gui.layout.VerticalLayout;

public class SteppingThreadGroupGui
      extends AbstractThreadGroupGui
{
   protected ConcurrentHashMap<String, AbstractGraphRow> model;
   private GraphPanelChart chart;
   private JTextField initialDelay;
   private JTextField incUserCount;
   private JTextField incUserPeriod;
   private JTextField flightTime;
   private JTextField decUserCount;
   private JTextField decUserPeriod;

   public SteppingThreadGroupGui()
   {
      init();
   }

   @Override
   protected void init()
   {
      super.init();

      JPanel containerPanel=new JPanel(new BorderLayout());

      containerPanel.add(createParamsPanel(), BorderLayout.NORTH);

      chart=new GraphPanelChart();
      model=new ConcurrentHashMap<String, AbstractGraphRow>();
      chart.setRows(model);
      chart.setDrawFinalZeroingLines(true);
      chart.setxAxisLabelRenderer(new DateTimeRenderer("HH:mm:ss"));
      containerPanel.add(chart, BorderLayout.CENTER);

      add(containerPanel, BorderLayout.CENTER);
   }

    private JPanel createParamsPanel() {
        JPanel panel = new JPanel(new VerticalLayout(0, VerticalLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder("Threads Scheduling Parameters"));

        JPanel panel1=new JPanel();
        panel1.add(new JLabel("First, wait for ", JLabel.RIGHT));
        initialDelay=new JTextField("0", 5);
        panel1.add(initialDelay);
        panel1.add(new JLabel(" seconds.", JLabel.LEFT));
        panel.add(panel1);

        JPanel panel2=new JPanel();
        panel2.add(new JLabel("Then start ", JLabel.RIGHT));
        incUserCount=new JTextField("1", 5);
        panel2.add(incUserCount);
        panel2.add(new JLabel("threads every ", JLabel.LEFT));
        incUserPeriod=new JTextField("1", 5);
        panel2.add(incUserPeriod);
        panel2.add(new JLabel(" seconds.", JLabel.LEFT));
        panel.add(panel2);


        JPanel panel3=new JPanel();
        panel3.add(new JLabel("Then work for ", JLabel.RIGHT));
        flightTime=new JTextField("60", 5);
        panel3.add(flightTime);
        panel3.add(new JLabel(" seconds. ", JLabel.LEFT));
        panel.add(panel3);

        JPanel panel4=new JPanel();
        panel4.add(new JLabel("Finally, stop ", JLabel.RIGHT));
        decUserCount=new JTextField("1", 5);
        panel4.add(decUserCount);
        panel4.add(new JLabel(" threads every ", JLabel.LEFT));
        decUserPeriod=new JTextField("1", 5);
        panel4.add(decUserPeriod);
        panel4.add(new JLabel(" seconds.", JLabel.LEFT));
        panel.add(panel4);

        return panel;
    }
   public String getLabelResource()
   {
      return this.getClass().getSimpleName();
   }

   @Override
   public String getStaticLabel()
   {
      return "Stepping Thread Group";
   }

   public TestElement createTestElement()
   {
      SteppingThreadGroup tg = new SteppingThreadGroup();
      modifyTestElement(tg);
      return tg;
   }

   public void modifyTestElement(TestElement tg)
   {
      super.configureTestElement(tg);
      if (tg instanceof SteppingThreadGroup)
      {
         updateChart((SteppingThreadGroup) tg);
      }
   }

   @Override
   public void configure(TestElement tg)
   {
      super.configure(tg);
   }

   private void updateChart(SteppingThreadGroup tg)
   {
      model.clear();
      GraphRowAverages row=new GraphRowAverages();
      row.setColor(Color.RED);
      row.setDrawLine(true);
      row.setMarkerSize(AbstractGraphRow.MARKER_SIZE_SMALL);

      final HashTree hashTree = new HashTree();
      hashTree.add(new LoopController());
      JMeterThread thread = new JMeterThread(hashTree, null, null);

      HashMap<Long, Long> counts=new HashMap<Long, Long>();

      for (int n=0; n<tg.getNumThreads(); n++)
      {
         thread.setThreadNum(n);
         tg.scheduleThread(thread);
         addCount(counts, thread.getStartTime());
         addCount(counts, thread.getEndTime());
      }

      Iterator it=counts.keySet().iterator();
      while (it.hasNext())
      {
         Long time = (Long) it.next();
         row.add(time, counts.get(time));
      }
      model.put("Expected parallel users count", row);
   }

   private void addCount(HashMap<Long, Long> counts, long xVal)
   {
      if (counts.containsKey(xVal))
      {
         counts.put(xVal, counts.get(xVal)+1);
      }
      else
      {
         counts.put(xVal, 1L);
      }
   }
}