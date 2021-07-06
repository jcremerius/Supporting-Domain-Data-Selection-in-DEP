package org.processmining.plugins.inductiveVisualMiner.dep;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerPanel;
import org.processmining.plugins.inductiveVisualMiner.Selection;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.OnOffPanel;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.RangeSlider;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.SideWindow;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecoratorI;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMPanel;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.SwitchPanel;

import au.com.bytecode.opencsv.CSVReader;
import gnu.trove.iterator.TIntIterator;

public class DepView extends SideWindow {

	private static final long serialVersionUID = -4833037956665918455L;
	private final OnOffPanel<IvMPanel> onOffPanel;
	private final JComboBox<String> keySelector;
	private final DefaultComboBoxModel<String> keySelectorModel;
	private final JTextArea explanation;
	private final JLabel title, aggTitle, catTitle, filterTitle, cat_list_title, con_list_title, con_Slider_lower, con_Slider_upper, cat_Slider_lower, cat_Slider_upper, cat_cv_label, con_cv_label;
	private final JButton enabled, groupAggregation;
	private final JList<String> continuousSelector, categoricalSelector;
	private final DefaultListModel<String> continuousSelectorModel, categoricalSelectorModel;
	JPanel filterPanel;

	public static final int maxColours = 7;
	public static final String prefix = "       ";

	private Runnable onUpdate;

	/**
	 * 
	 */
	private final RangeSlider catSlider, conSlider;
	private final JScrollPane scrollPane, scrollPane_cat;
	private final JTextField categoricalAggregationValue;
	private String selectedAggregationMethod;
	private String selectedFilterValue;
	private final DefaultComboBoxModel<String> aggregationSelectorModel, filterValueModel;
	private final JComboBox<String> aggregationSelector, filterValue;
	private String selectedActivity;
	private HashMap<String, Object[][]> attributes_to_display; 
	private HashMap<String, String> attributeClasses, attributeTypes;
	private HashMap<String, Double> attributeCV;
	private boolean addMultipleAggregations;
	
	public DepView(IvMDecoratorI decorator, InductiveVisualMinerPanel parent) {
		super(parent, "Attribute Selection " + InductiveVisualMinerPanel.title);
		setSize(520, 550);
		setMinimumSize(new Dimension(520, 550));
		IvMPanel content = new IvMPanel(decorator);
		attributeClasses = new HashMap<String,String>();
		attributeTypes = new HashMap<String,String>();
		attributeCV = new HashMap<String,Double>();

		onOffPanel = new OnOffPanel<>(decorator, content);
		onOffPanel.setOffMessage("Waiting for attributes..");
		add(onOffPanel);
		onOffPanel.on();

		parent.getSelectionLabel();
		
		BorderLayout layout = new BorderLayout();
		content.setLayout(layout);
		

		//explanation
		{
			explanation = new JTextArea(
					"Attribute Selection shows the attributes of the selected activity and helps to select attributes by filtering them according to their type and process characteristic");
			decorator.decorate(explanation);
			explanation.setWrapStyleWord(true);
			explanation.setLineWrap(true);
			explanation.setEnabled(false);
			explanation.setMargin(new Insets(5, 5, 5, 5));
			content.add(explanation, BorderLayout.PAGE_START);
		}
		//filter panel
		{
			SpringLayout filterPanelLayout = new SpringLayout();
			filterPanel = new SwitchPanel(decorator);
			filterPanel.setLayout(filterPanelLayout);
			filterPanel.setEnabled(false);
			content.add(filterPanel, BorderLayout.CENTER);

			//title
			{
				title = new JLabel("Selected Activity");
				decorator.decorate(title);

				filterPanel.add(title);
				filterPanelLayout.putConstraint(SpringLayout.NORTH, title, 10, SpringLayout.NORTH, filterPanel);
				filterPanelLayout.putConstraint(SpringLayout.WEST, title, 5, SpringLayout.WEST, filterPanel);
			}

			//key selector
			{
				keySelectorModel = new DefaultComboBoxModel<>();
				keySelector = new JComboBox<>();
				decorator.decorate(keySelector);
				keySelector.setModel(keySelectorModel);

				filterPanel.add(keySelector);
				filterPanelLayout.putConstraint(SpringLayout.VERTICAL_CENTER, keySelector, 0,
						SpringLayout.VERTICAL_CENTER, title);
				filterPanelLayout.putConstraint(SpringLayout.WEST, keySelector, 5, SpringLayout.EAST, title);

				filterPanelLayout.putConstraint(SpringLayout.EAST, keySelector, -5, SpringLayout.EAST, filterPanel);
			}
			
			{
				aggTitle = new JLabel("Selected Aggregation Method (quantitative)");
				decorator.decorate(aggTitle);

				filterPanel.add(aggTitle);
				filterPanelLayout.putConstraint(SpringLayout.NORTH, aggTitle, 30, SpringLayout.NORTH, title);
				filterPanelLayout.putConstraint(SpringLayout.WEST, aggTitle, 5, SpringLayout.WEST, filterPanel);
			}
			//aggregation selector
			{
				aggregationSelectorModel = new DefaultComboBoxModel<>();
				aggregationSelector = new JComboBox<>();
				decorator.decorate(aggregationSelector);
				aggregationSelector.setModel(aggregationSelectorModel);

				filterPanel.add(aggregationSelector);
				filterPanelLayout.putConstraint(SpringLayout.VERTICAL_CENTER, aggregationSelector, 0,
						SpringLayout.VERTICAL_CENTER, aggTitle);
				filterPanelLayout.putConstraint(SpringLayout.WEST, aggregationSelector, 5, SpringLayout.EAST, aggTitle);

				filterPanelLayout.putConstraint(SpringLayout.EAST, aggregationSelector, -5, SpringLayout.EAST, filterPanel);
			}
			
			{
				catTitle = new JLabel("Get relative frequency of category");
				decorator.decorate(catTitle);

				filterPanel.add(catTitle);
				filterPanelLayout.putConstraint(SpringLayout.NORTH, catTitle, 50, SpringLayout.NORTH, title);
				filterPanelLayout.putConstraint(SpringLayout.WEST, catTitle, 5, SpringLayout.WEST, filterPanel);
			}
			//aggregation selector
			{
				
				categoricalAggregationValue = new JTextField();

				filterPanel.add(categoricalAggregationValue);
				filterPanelLayout.putConstraint(SpringLayout.VERTICAL_CENTER, categoricalAggregationValue, 0,
						SpringLayout.VERTICAL_CENTER, catTitle);
				filterPanelLayout.putConstraint(SpringLayout.WEST, categoricalAggregationValue, 5, SpringLayout.EAST, catTitle);

				filterPanelLayout.putConstraint(SpringLayout.EAST, categoricalAggregationValue, -5, SpringLayout.EAST, filterPanel);
			
			}
			
			
			{
				filterTitle = new JLabel("Filter Attributes according to their process characteristic");
				decorator.decorate(filterTitle);

				filterPanel.add(filterTitle);
				filterPanelLayout.putConstraint(SpringLayout.NORTH, filterTitle, 150, SpringLayout.NORTH, title);
				filterPanelLayout.putConstraint(SpringLayout.WEST, filterTitle, 5, SpringLayout.WEST, filterPanel);
			}
			//aggregation selector
			{
				filterValueModel = new DefaultComboBoxModel<>();
				filterValue = new JComboBox<>();
				decorator.decorate(filterValue);
				filterValue.setModel(filterValueModel);

				filterPanel.add(filterValue);
				filterPanelLayout.putConstraint(SpringLayout.VERTICAL_CENTER, filterValue, 0,
						SpringLayout.VERTICAL_CENTER, filterTitle);
				filterPanelLayout.putConstraint(SpringLayout.WEST, filterValue, 5, SpringLayout.EAST, filterTitle);

				filterPanelLayout.putConstraint(SpringLayout.EAST, filterValue, -5, SpringLayout.EAST, filterPanel);
			
			}
			
			
			
			{
				enabled = new JButton();
				enabled.setText("Add Event Attribute Aggregation");
				decorator.decorate(enabled);
				filterPanel.add(enabled);
				filterPanelLayout.putConstraint(SpringLayout.VERTICAL_CENTER, enabled, 50,
						SpringLayout.VERTICAL_CENTER, aggTitle);
			}
			
			{
				groupAggregation = new JButton();
				groupAggregation.setText("Add Event Attribute Aggregations to all activities with the attribute");
				decorator.decorate(groupAggregation);
				filterPanel.add(groupAggregation);
				filterPanelLayout.putConstraint(SpringLayout.VERTICAL_CENTER, groupAggregation, 70,
						SpringLayout.VERTICAL_CENTER, aggTitle);
			}
			

			{
				
				continuousSelectorModel = new DefaultListModel<String>();
				continuousSelector = new JList<String>(continuousSelectorModel);
				continuousSelector.setCellRenderer(new ListCellRenderer<String>() {
					protected DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();

					public Component getListCellRendererComponent(JList<? extends String> list, String value, int index,
							boolean isSelected, boolean cellHasFocus) {

						JLabel renderer = (JLabel) defaultRenderer.getListCellRendererComponent(list, value, index,
								isSelected, cellHasFocus);
						if (!isSelected) {
							renderer.setOpaque(false);
						} else {
							renderer.setOpaque(true);
						}
						return renderer;
					}
				});
				continuousSelector.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
				scrollPane = new JScrollPane(continuousSelector);
				scrollPane.getViewport().setOpaque(false);
				continuousSelector.setOpaque(false);
				scrollPane.setOpaque(false);
				scrollPane.setPreferredSize(new Dimension(225,160));
				filterPanel.add(scrollPane);
				filterPanelLayout.putConstraint(SpringLayout.NORTH, scrollPane, 50, SpringLayout.NORTH, filterTitle);
				filterPanelLayout.putConstraint(SpringLayout.WEST, scrollPane, 20, SpringLayout.WEST, filterPanel);
			}
			{
				con_list_title = new JLabel("Quantitative Attributes");
				decorator.decorate(con_list_title);
				filterPanel.add(con_list_title);
				filterPanelLayout.putConstraint(SpringLayout.NORTH, con_list_title, -20, SpringLayout.NORTH, scrollPane);
				filterPanelLayout.putConstraint(SpringLayout.WEST, con_list_title, 50, SpringLayout.WEST, filterPanel);
			}
			
			
			{
				categoricalSelectorModel = new DefaultListModel<String>();
				categoricalSelector = new JList<String>(categoricalSelectorModel);
				categoricalSelector.setCellRenderer(new ListCellRenderer<String>() {
					protected DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();

					public Component getListCellRendererComponent(JList<? extends String> list, String value, int index,
							boolean isSelected, boolean cellHasFocus) {

						JLabel renderer = (JLabel) defaultRenderer.getListCellRendererComponent(list, value, index,
								isSelected, cellHasFocus);
						if (!isSelected) {
							renderer.setOpaque(false);
						} else {
							renderer.setOpaque(true);
						}
						return renderer;
					}
				});
				categoricalSelector.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
				scrollPane_cat = new JScrollPane(categoricalSelector);

				scrollPane_cat.getViewport().setOpaque(false);
				categoricalSelector.setOpaque(false);
				scrollPane_cat.setOpaque(false);
				scrollPane_cat.setPreferredSize(new Dimension(225,160));
				filterPanel.add(scrollPane_cat);
				filterPanelLayout.putConstraint(SpringLayout.NORTH, scrollPane_cat, 50, SpringLayout.NORTH, filterTitle);
				filterPanelLayout.putConstraint(SpringLayout.WEST, scrollPane_cat, 20, SpringLayout.EAST, scrollPane);
				filterPanelLayout.putConstraint(SpringLayout.EAST, scrollPane_cat, -5, SpringLayout.EAST, filterPanel);
				
			}
			{
				cat_list_title = new JLabel("Categorical Attributes");
				decorator.decorate(cat_list_title);
				filterPanel.add(cat_list_title);
				filterPanelLayout.putConstraint(SpringLayout.NORTH, cat_list_title, -20, SpringLayout.NORTH, scrollPane_cat);
				filterPanelLayout.putConstraint(SpringLayout.WEST, cat_list_title, 250, SpringLayout.WEST, con_list_title);
			}
			{
				conSlider = new RangeSlider(0, 100);
				//set lower value
				conSlider.setValue(0);
				//set high value
				conSlider.setUpperValue(100);
				conSlider.setPreferredSize(new Dimension(230, 20));
				conSlider.setEnabled(false);
				filterPanel.add(conSlider);
				filterPanelLayout.putConstraint(SpringLayout.NORTH, conSlider, 180, SpringLayout.NORTH, scrollPane);
				filterPanelLayout.putConstraint(SpringLayout.WEST, conSlider, 10, SpringLayout.WEST, filterPanel);
			}
			{
				con_Slider_lower = new JLabel("");
				decorator.decorate(con_Slider_lower);
				filterPanel.add(con_Slider_lower);
				filterPanelLayout.putConstraint(SpringLayout.NORTH, con_Slider_lower, 20, SpringLayout.NORTH, conSlider);
				filterPanelLayout.putConstraint(SpringLayout.WEST, con_Slider_lower, 20, SpringLayout.WEST, filterPanel);
			}
			{
				con_Slider_upper = new JLabel("");
				decorator.decorate(con_Slider_upper);
				filterPanel.add(con_Slider_upper);
				filterPanelLayout.putConstraint(SpringLayout.NORTH, con_Slider_upper, 20, SpringLayout.NORTH, conSlider);
				filterPanelLayout.putConstraint(SpringLayout.WEST, con_Slider_upper, 220, SpringLayout.WEST, filterPanel);
			}
			{
				con_cv_label = new JLabel("CV(%)");
				decorator.decorate(con_cv_label);
				filterPanel.add(con_cv_label);
				filterPanelLayout.putConstraint(SpringLayout.NORTH, con_cv_label, 20, SpringLayout.NORTH, conSlider);
				filterPanelLayout.putConstraint(SpringLayout.WEST, con_cv_label, 110, SpringLayout.WEST, filterPanel);
			}
			
			{
				catSlider = new RangeSlider(0, 100);
				//set lower value
				catSlider.setValue(0);
				//set high value
				catSlider.setUpperValue(100);
				catSlider.setPreferredSize(new Dimension(230, 20));
				catSlider.setEnabled(false);
				filterPanel.add(catSlider);
				filterPanelLayout.putConstraint(SpringLayout.NORTH, catSlider, 180, SpringLayout.NORTH, scrollPane_cat);
				filterPanelLayout.putConstraint(SpringLayout.WEST, catSlider, 250, SpringLayout.WEST, conSlider);
			}
			{
				cat_Slider_lower = new JLabel("");
				decorator.decorate(cat_Slider_lower);
				filterPanel.add(cat_Slider_lower);
				filterPanelLayout.putConstraint(SpringLayout.NORTH, cat_Slider_lower, 20, SpringLayout.NORTH, catSlider);
				filterPanelLayout.putConstraint(SpringLayout.WEST, cat_Slider_lower, 270, SpringLayout.WEST, filterPanel);
			}
			{
				cat_Slider_upper = new JLabel("");
				decorator.decorate(cat_Slider_upper);
				filterPanel.add(cat_Slider_upper);
				filterPanelLayout.putConstraint(SpringLayout.NORTH, cat_Slider_upper, 20, SpringLayout.NORTH, catSlider);
				filterPanelLayout.putConstraint(SpringLayout.WEST, cat_Slider_upper, 470, SpringLayout.WEST, filterPanel);
			}
			{
				cat_cv_label = new JLabel("CV(%)");
				decorator.decorate(cat_cv_label);
				filterPanel.add(cat_cv_label);
				filterPanelLayout.putConstraint(SpringLayout.NORTH, cat_cv_label, 20, SpringLayout.NORTH, catSlider);
				filterPanelLayout.putConstraint(SpringLayout.WEST, cat_cv_label, 360, SpringLayout.WEST, filterPanel);
			}
			
			
			


		}
		conSlider.addChangeListener(new ChangeListener() {
			
			public void stateChanged(ChangeEvent e) {
				// TODO Auto-generated method stub


				String element = "";
				String attributeClass = "";
				List<String> itemsToRemove = new ArrayList<>();
				selectedFilterValue = (String) filterValue.getSelectedItem();
				fillAttributeList();
				int listSize = continuousSelectorModel.getSize();
				
				for(int i = 0; i < listSize; i++) {
					element = continuousSelectorModel.get(i);
					attributeClass = attributeClasses.get(element);
					if(((attributeCV.get(element)*100) < conSlider.getValue() | (attributeCV.get(element)*100) > conSlider.getUpperValue()) | !attributeClass.equals("dynamic")) {
			
						itemsToRemove.add(element);
					}
				}
				for(String ele: itemsToRemove) {
					continuousSelectorModel.removeElement(ele);
				}
				itemsToRemove = new ArrayList<>();
				listSize = categoricalSelectorModel.getSize();
				
				for(int i = 0; i < listSize; i++) {
					element = categoricalSelectorModel.get(i);
					attributeClass = attributeClasses.get(element);
					if((attributeCV.get(element)*100) < catSlider.getValue() | (attributeCV.get(element)*100) > catSlider.getUpperValue() | !attributeClass.equals("dynamic")) {
						itemsToRemove.add(element);
					}
				}
				for(String ele: itemsToRemove) {
					categoricalSelectorModel.removeElement(ele);
				}
			
				con_Slider_lower.setText(Integer.toString(conSlider.getValue()));
				con_Slider_upper.setText(Integer.toString(conSlider.getUpperValue()));
				
			}
		});
		
		catSlider.addChangeListener(new ChangeListener() {
			
			public void stateChanged(ChangeEvent e) {
				// TODO Auto-generated method stub

				String element = "";
				String attributeClass = "";
				List<String> itemsToRemove = new ArrayList<>();
				selectedFilterValue = (String) filterValue.getSelectedItem();
				fillAttributeList();
				int listSize = continuousSelectorModel.getSize();
				
				for(int i = 0; i < listSize; i++) {
					element = continuousSelectorModel.get(i);
					attributeClass = attributeClasses.get(element);
					if(((attributeCV.get(element)*100) < conSlider.getValue() | (attributeCV.get(element)*100) > conSlider.getUpperValue()) | !attributeClass.equals("dynamic")) {
			
						itemsToRemove.add(element);
					}
				}
				for(String ele: itemsToRemove) {
					continuousSelectorModel.removeElement(ele);
				}
				itemsToRemove = new ArrayList<>();
				listSize = categoricalSelectorModel.getSize();
				
				for(int i = 0; i < listSize; i++) {
					element = categoricalSelectorModel.get(i);
					attributeClass = attributeClasses.get(element);
					if((attributeCV.get(element)*100) < catSlider.getValue() | (attributeCV.get(element)*100) > catSlider.getUpperValue() | !attributeClass.equals("dynamic")) {
						itemsToRemove.add(element);
					}
				}
				for(String ele: itemsToRemove) {
					categoricalSelectorModel.removeElement(ele);
				}
				cat_Slider_lower.setText(Integer.toString(catSlider.getValue()));
				cat_Slider_upper.setText(Integer.toString(catSlider.getUpperValue()));
				
			
			}
		});
		

		enabled.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				addMultipleAggregations = false;
				updateView();
			}
		});
		
		groupAggregation.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				addMultipleAggregations = true;
				updateView();
			}
		});

		//filter list according to attribute class and CV
		filterValue.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					if (onOffPanel.isOn()) {
						String element = "";
						String attributeClass = "";
						List<String> itemsToRemove = new ArrayList<>();
						// remove categorical and continuous and categorical attributes not belonging to selected class!
						selectedFilterValue = (String) filterValue.getSelectedItem();
						fillAttributeList();
						int listSize = continuousSelectorModel.getSize();
						
						for(int i = 0; i < listSize; i++) {
							element = continuousSelectorModel.get(i);
							attributeClass = attributeClasses.get(element);
							if(!attributeClass.equals(selectedFilterValue) & !selectedFilterValue.equals("all")) {
								itemsToRemove.add(element);
							}
						}
						for(String ele: itemsToRemove) {
							continuousSelectorModel.removeElement(ele);
						}
						itemsToRemove = new ArrayList<>();
						listSize = categoricalSelectorModel.getSize();
						
						for(int i = 0; i < listSize; i++) {
							element = categoricalSelectorModel.get(i);
							attributeClass = attributeClasses.get(element);
							if(!attributeClass.equals(selectedFilterValue) & !selectedFilterValue.equals("all")) {
								itemsToRemove.add(element);
							}
						}
						for(String ele: itemsToRemove) {
							categoricalSelectorModel.removeElement(ele);
						}
						
						if(selectedFilterValue.equals("dynamic")) {
							conSlider.setEnabled(true);
							catSlider.setEnabled(true);
						}
						else {
							conSlider.setEnabled(false);
							catSlider.setEnabled(false);
						}
					}
				}
			}
		});
		//set up the controller
		aggregationSelector.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					if (onOffPanel.isOn()) {
						selectedAggregationMethod = (String) aggregationSelector.getSelectedItem();
					}
				}
			}
		});

	}
	public void updateSelection(InductiveVisualMinerPanel panel, Selection selection, IvMModel model) {
		if (selection.isAnActivitySelected()) {
			TIntIterator it = selection.getSelectedActivities().iterator();
			selectedActivity = model.getActivityName(it.next());
			keySelectorModel.removeAllElements();
			keySelectorModel.addElement(selectedActivity);
			continuousSelectorModel.clear();
			fillAttributeList();
	        continuousSelector.setVisible(true);
		}
		
	}
	
	public void fillAttributeList() {
		continuousSelectorModel.removeAllElements();
		categoricalSelectorModel.removeAllElements();
		List<List<String>> records = new ArrayList<List<String>>();
		try (CSVReader csvReader = new CSVReader(new FileReader("attributesForActivity.csv"));) {
		    String[] values = null;
		    while ((values = csvReader.readNext()) != null) {
		        records.add(Arrays.asList(values));
		    }
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        int lineNo = 0;
        for(List<String> line: records) {
            int columnNo = 0;
            for (String value: line) {
            	
            	if(records.get(lineNo).get(1).equals(selectedActivity) & columnNo != 0 & columnNo != 1)
            	{
            
            	      if(value.equals("1.0")) {
            	    	  if(attributeTypes != null) {
            	    		    	    	  
            	    	  if(attributeTypes.get(records.get(0).get(columnNo)).equals("categorical")) {
            	    		  categoricalSelectorModel.addElement(records.get(0).get(columnNo));
            	    	  }
            	    	  else {
            	    		  continuousSelectorModel.addElement(records.get(0).get(columnNo));
            	    	  	}	
            	    	  
            	    	  }
            	    	}
  	              
            	}
            columnNo++;
            }    
            lineNo++;
            
        }		
	}
	
	public void fillAttributeClassMap() {
		List<List<String>> records = new ArrayList<List<String>>();
		try (CSVReader csvReader = new CSVReader(new FileReader("attributeClasses.csv"));) {
		    String[] values = null;
		    while ((values = csvReader.readNext()) != null) {
		        records.add(Arrays.asList(values));
		    }
		} catch (FileNotFoundException err) {
			// TODO Auto-generated catch block
			err.printStackTrace();
		} catch (IOException err) {
			// TODO Auto-generated catch block
			err.printStackTrace();
		}
        int lineNo = 0;
        for(List<String> line: records) {
            int columnNo = 0;
            String attributeName = ""; 
            for (String value: line) {
            	if(columnNo == 1 && value != "Activity") {
            		 attributeName = value;
            	}
            	if(columnNo == 2 && lineNo > 0) {
            		this.attributeClasses.put(attributeName, value);
            	}
           
            columnNo++;
            }    
            lineNo++;
            
        }
	}
	public void fillAttributeDataTypeMap() {
		List<List<String>> records = new ArrayList<List<String>>();
		try (CSVReader csvReader = new CSVReader(new FileReader("attributeClasses.csv"));) {
		    String[] values = null;
		    while ((values = csvReader.readNext()) != null) {
		        records.add(Arrays.asList(values));
		    }
		} catch (FileNotFoundException err) {
			// TODO Auto-generated catch block
			err.printStackTrace();
		} catch (IOException err) {
			// TODO Auto-generated catch block
			err.printStackTrace();
		}
        int lineNo = 0;
        for(List<String> line: records) {
            int columnNo = 0;
            String attributeName = ""; 
            for (String value: line) {
            	if(columnNo == 1 && value != "Activity") {
            		 attributeName = value;
            	}
            	if(columnNo == 3 && lineNo > 0) {
            		this.attributeTypes.put(attributeName, value);
            	}
           
            columnNo++;
            }    
            lineNo++;
            
        }
	}
	
	public void fillAttributeCVMap() {
		double max_cat_CV = 0, min_cat_CV = 100;
		double max_con_CV = 0, min_con_CV = 100;
		List<List<String>> records = new ArrayList<List<String>>();
		try (CSVReader csvReader = new CSVReader(new FileReader("attributeClasses.csv"));) {
		    String[] values = null;
		    while ((values = csvReader.readNext()) != null) {
		        records.add(Arrays.asList(values));
		    }
		} catch (FileNotFoundException err) {
			// TODO Auto-generated catch block
			err.printStackTrace();
		} catch (IOException err) {
			// TODO Auto-generated catch block
			err.printStackTrace();
		}
        int lineNo = 0;
        for(List<String> line: records) {
        	String type = "";
            int columnNo = 0;
            String attributeName = ""; 
            for (String value: line) {
            	if(columnNo == 1 && value != "Activity") {
            		 attributeName = value;
            	}
            	if(columnNo == 3 && lineNo > 0) {
            		type = value;
            	}
            	if(columnNo == 4 && lineNo > 0) {
            		double val = Double.parseDouble(value);
            		this.attributeCV.put(attributeName, val);
            		if(type.equals("categorical")) {
            			if(val > max_cat_CV) {
            				max_cat_CV = val;
            			}
            			if(val < min_cat_CV) {
            				min_cat_CV = val;
            			}
            		}
            		if(type.equals("continuous")) {
            			if(val > max_con_CV) {
            				max_con_CV = val;
            			}
            			if(val < min_con_CV) {
            				min_con_CV = val;
            			}
            		}
            	}
           
            columnNo++;
            }    
            lineNo++;
            
        }
        
        conSlider.setMaximum((int)(max_con_CV*100) + 1);
        conSlider.setMinimum((int)(min_con_CV*100));
        catSlider.setMaximum((int)(max_cat_CV*100) + 1);
        catSlider.setMinimum((int)(min_cat_CV*100));
        con_Slider_lower.setText(Integer.toString(conSlider.getValue()));
        con_Slider_upper.setText(Integer.toString(conSlider.getUpperValue()));
        cat_Slider_lower.setText(Integer.toString(catSlider.getValue()));
        cat_Slider_upper.setText(Integer.toString(catSlider.getUpperValue()));
	}
	
	public void initDep() {
		aggregationSelectorModel.addElement("Mean");
		aggregationSelectorModel.addElement("Minimum");
		aggregationSelectorModel.addElement("Maximum");
		aggregationSelectorModel.addElement("Median");
		this.selectedAggregationMethod = "Mean";
		filterValueModel.addElement("all");
		filterValueModel.addElement("static");
		filterValueModel.addElement("semi-dynamic");
		filterValueModel.addElement("dynamic");
		this.selectedFilterValue = "all";
		fillAttributeClassMap();
		fillAttributeDataTypeMap();
		fillAttributeCVMap();
		System.out.println(this.attributeTypes);
		System.out.println(this.attributeCV);
	}
	
	
	public void updateAttributeSelection(InductiveVisualMinerPanel panel, HashMap<String, Object[][]> attributes_to_display ) {
		this.attributes_to_display = attributes_to_display;
	}
	public HashMap<String, Object[][]> getAttributeSelection() {
		return this.attributes_to_display;
	}
	public String getSelectedActivity() {
		return this.selectedActivity;
	}
	public String getSelectedAggregationMethod() {
		return this.selectedAggregationMethod;
	}
	public String getFilterValue() {
		return this.selectedFilterValue;
	}
	public String getCategoricalAggregationValue() {
		return this.categoricalAggregationValue.getText();
	}

	public List<String> getSelectedContinuousAttributes() {
		return continuousSelector.getSelectedValuesList();
	}
	public List<String> getSelectedCategoricalAttributes() {
		return categoricalSelector.getSelectedValuesList();
	}
	public boolean getMultipleAggregationsFlag() {
		return addMultipleAggregations;
	}

	public void invalidateAttributes() {
		//disable the gui
		{
			onOffPanel.off();
		}
	}

	public void updateView( ) {
		onUpdate.run();
	}
	public void update() {
		//onUpdate.run();
		//update values --> selectedAttributes
	}

	public void setFilterValue(String value) {
		this.selectedFilterValue = value;
	}


	public Runnable getOnUpdate() {
		return onUpdate;
	}

	
	public void setOnUpdate(Runnable onUpdate) {
		this.onUpdate = onUpdate;
	}

	
}