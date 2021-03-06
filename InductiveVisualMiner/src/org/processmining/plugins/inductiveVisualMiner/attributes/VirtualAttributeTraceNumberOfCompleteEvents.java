package org.processmining.plugins.inductiveVisualMiner.attributes;

import org.deckfour.xes.model.XAttributable;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMMove;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;
import org.processmining.plugins.inductiveminer2.attributes.AttributeVirtualTraceNumericAbstract;

public class VirtualAttributeTraceNumberOfCompleteEvents extends AttributeVirtualTraceNumericAbstract {

	@Override
	public String getName() {
		return "number of completion events";
	}

	public double getNumeric(XAttributable x) {
		if (x instanceof IvMTrace) {
			int result = 0;
			for (IvMMove move : (IvMTrace) x) {
				if (move.hasAttributes() && move.isComplete()) {
					result++;
				}
			}
			return result;
		}
		return -Double.MAX_VALUE;
	}

}
