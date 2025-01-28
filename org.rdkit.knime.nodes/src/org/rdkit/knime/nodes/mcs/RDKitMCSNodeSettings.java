package org.rdkit.knime.nodes.mcs;

import org.RDKit.AtomComparator;
import org.knime.base.node.preproc.double2int.WarningMessage;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.webui.node.dialog.defaultdialog.DefaultNodeSettings;
import org.knime.core.webui.node.dialog.defaultdialog.persistence.field.FieldNodeSettingsPersistor;
import org.knime.core.webui.node.dialog.defaultdialog.persistence.field.Persist;
import org.knime.core.webui.node.dialog.defaultdialog.widget.ChoicesWidget;
import org.knime.core.webui.node.dialog.defaultdialog.widget.Label;
import org.knime.core.webui.node.dialog.defaultdialog.widget.Widget;
import org.rdkit.knime.util.RDKitMoleculeColumnChoicesProvider;

/**
 * Settings for the RDKit MCS node using the webui framework.
 * 
 * @author Marc Lehner, KNIME GmbH, Zurich, Switzerland
 */
public final class RDKitMCSNodeSettings implements DefaultNodeSettings {
	
	private static final class AtomComparisonPersistor implements FieldNodeSettingsPersistor<AtomComparison> {
		@Override
		public AtomComparison load(NodeSettingsRO settings) throws InvalidSettingsException {
			String val = settings.getString("atomComparison");
			if (val != null && val.equalsIgnoreCase("Compare Elements")) {
				return AtomComparison.CompareElements;
			} else if (val != null && val.equalsIgnoreCase("Compare Isotopes")) {
				return AtomComparison.CompareIsotopes;
			} else {
				return AtomComparison.CompareAny;
			}
		}

		@Override
		public void save(AtomComparison obj, NodeSettingsWO settings) {
			settings.addString("atomComparison", obj.toString());
		}

		@Override
		public String[] getConfigKeys() {
			return new String[] { "atomComparison" };
		}
	}
	
	private static final class BondComparisonPersistor implements FieldNodeSettingsPersistor<BondComparison> {
		@Override
		public BondComparison load(NodeSettingsRO settings) throws InvalidSettingsException {
			String val = settings.getString("bondComparison");
			if (val != null && val.equalsIgnoreCase("Compare Order")) {
				return BondComparison.CompareOrder;
			} else {
				return BondComparison.CompareAny;
			}
		}
		
		@Override
		public void save(BondComparison obj, NodeSettingsWO settings) {
			settings.addString("bondComparison", obj.toString());
		}
		
		@Override
		public String[] getConfigKeys() {
			return new String[] { "bondComparison" };
		}
	}

		
	
    @Persist(configKey = "input_column")
    @Widget(title = "Input Molecule Column", description = "Select the input column that contains the RDKit Molecule.")
    @ChoicesWidget(choices = RDKitMoleculeColumnChoicesProvider.class)
    String m_inputColumn;

    @Persist(configKey = "timeout")
    @Widget(title = "Timeout", description = "The maximum time allowed for the MCS calculation in seconds.")
    int m_timeout;

    @Persist(configKey = "ringMatchesRingOnly")
    @Widget(title = "Ring Matches Ring Only", description = "If checked, only ring atoms will match ring atoms.")
    boolean m_ringMatchesRingOnly;

    @Persist(configKey = "completeRingsOnly")
    @Widget(title = "Complete Rings Only", description = "If checked, only complete rings will be considered in the MCS.")
    boolean m_completeRingsOnly;

    @Persist(configKey = "matchValences")
    @Widget(title = "Match Valences", description = "If checked, atom valences will be considered during matching.")
    boolean m_matchValences;

    @Persist(configKey = "atomComparison", customPersistor = AtomComparisonPersistor.class)
    @Widget(title = "Atom Compare", description = "The method used to compare atoms during the MCS calculation.")
    AtomComparison m_atomCompare;

    @Persist(configKey = "bondComparison", customPersistor = BondComparisonPersistor.class)
    @Widget(title = "Bond Compare", description = "The method used to compare bonds during the MCS calculation.")
    BondComparison m_bondCompare;
    
    @Persist(configKey = "threshold")
    @Widget(title = "Threshold", description = "The threshold used for atom and bond comparisons.")
    double m_threshold;
}

