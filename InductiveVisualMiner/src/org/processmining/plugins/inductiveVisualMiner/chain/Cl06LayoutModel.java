package org.processmining.plugins.inductiveVisualMiner.chain;

import java.util.HashMap;

import org.processmining.plugins.InductiveMiner.Triple;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.visualisation.DotPanel;
import org.processmining.plugins.graphviz.visualisation.DotPanelUserSettings;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerSelectionColourer;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.data.AlignedLogVisualisationData;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.data.AlignedLogVisualisationDataImplEmpty;
import org.processmining.plugins.inductiveVisualMiner.configuration.InductiveVisualMinerConfiguration;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.mode.Mode;
import org.processmining.plugins.inductiveVisualMiner.traceview.TraceViewEventColourMap;
import org.processmining.plugins.inductiveVisualMiner.visualisation.DfmVisualisation;
import org.processmining.plugins.inductiveVisualMiner.visualisation.ProcessTreeVisualisation;
import org.processmining.plugins.inductiveVisualMiner.visualisation.ProcessTreeVisualisationInfo;
import org.processmining.plugins.inductiveVisualMiner.visualisation.ProcessTreeVisualisationParameters;
import org.processmining.plugins.inductiveminer2.attributes.AttributesInfo;

import com.kitfox.svg.SVGDiagram;

public class Cl06LayoutModel extends DataChainLinkComputationAbstract {

	@Override
	public String getName() {
		return "layout model ";
	}

	@Override
	public String getStatusBusyMessage() {
		return "Layouting model..";
	}

	@Override
	public IvMObject<?>[] createInputObjects() {
		return new IvMObject<?>[] { IvMObject.model, IvMObject.selected_visualisation_mode,
				IvMObject.selected_graph_user_settings, IvMObject.attributes_info};
	}

	@Override
	public IvMObject<?>[] createOutputObjects() {
		return new IvMObject<?>[] { IvMObject.graph_dot, IvMObject.graph_svg, IvMObject.graph_visualisation_info, IvMObject.activity_attributes };
	}

	@Override
	public IvMObjectValues execute(InductiveVisualMinerConfiguration configuration, IvMObjectValues inputs,
			IvMCanceller canceller) throws Exception {
		IvMModel model = inputs.get(IvMObject.model);
		AttributesInfo attributesInfo = inputs.get(IvMObject.attributes_info);
		Mode mode = inputs.get(IvMObject.selected_visualisation_mode);
		DotPanelUserSettings graphSettings = inputs.get(IvMObject.selected_graph_user_settings);
		
		//compute dot
		AlignedLogVisualisationData data = new AlignedLogVisualisationDataImplEmpty();
		ProcessTreeVisualisationParameters parameters = mode.getParametersWithoutAlignments();
		InductiveVisualMinerSelectionColourer.createColourArray();
		Triple<Dot, ProcessTreeVisualisationInfo, TraceViewEventColourMap> p;
		if (model.isTree()) {
			ProcessTreeVisualisation visualiser = new ProcessTreeVisualisation();
			p = visualiser.fancy(model, data, parameters, attributesInfo, false, null, null);
		} else {
			DfmVisualisation visualiser = new DfmVisualisation();
			p = visualiser.fancy(model, data, parameters);
		}

		//set the graph direction
		graphSettings.applyToDot(p.getA());
		//compute svg from dot
		SVGDiagram diagram = DotPanel.dot2svg(p.getA());


		return new IvMObjectValues().//
				s(IvMObject.graph_dot, p.getA()).//
				s(IvMObject.graph_svg, diagram).//
				s(IvMObject.graph_visualisation_info, p.getB()).//
				s(IvMObject.activity_attributes, new HashMap<String, Object[][]>());
	}

}
