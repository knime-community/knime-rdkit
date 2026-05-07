/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by KNIME AG, Zurich, Switzerland
 *  Website: http://www.knime.com; Email: contact@knime.com
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME AG herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ------------------------------------------------------------------------
 */
    
package org.rdkit.knime.nodes.mcs;

import org.knime.node.parameters.NodeParameters;
import org.knime.node.parameters.Widget;
import org.knime.node.parameters.migration.LoadDefaultsForAbsentFields;
import org.knime.node.parameters.persistence.Persist;
import org.knime.node.parameters.updates.ParameterReference;
import org.knime.node.parameters.updates.ValueProvider;
import org.knime.node.parameters.updates.ValueReference;
import org.knime.node.parameters.widget.choices.ChoicesProvider;
import org.knime.node.parameters.widget.choices.ValueSwitchWidget;
import org.knime.node.parameters.widget.number.NumberInputWidget;
import org.knime.node.parameters.widget.number.NumberInputWidgetValidation.MaxValidation;
import org.knime.node.parameters.widget.number.NumberInputWidgetValidation.MinValidation;
import org.knime.node.parameters.widget.number.NumberInputWidgetValidation.MinValidation.IsNonNegativeValidation;
import org.rdkit.knime.util.RDKitMoleculeColumnAutoGuessProvider;
import org.rdkit.knime.util.RDKitMoleculeColumnChoicesProvider;

/**
 * Node parameters for RDKit MCS.
 *
 * @author Magnus Gohm, KNIME GmbH, Konstanz, Germany
 * @author AI Migration Pipeline v1.2
 */
@LoadDefaultsForAbsentFields
final class RDKitMCSNodeParameters implements NodeParameters {

    @Widget(title = "RDKit molecule column",
        description = "Select here the RDKit Molecule column to be used as input.")
    @Persist(configKey = "input_column")
    @ChoicesProvider(RDKitMoleculeColumnChoicesProvider.class)
    @ValueProvider(InputColumnAutoGuessProvider.class)
    @ValueReference(InputColumnNameRef.class)
    String m_inputColumnName;

    static final class InputColumnNameRef implements ParameterReference<String> {
    }

    @Widget(title = "Threshold",
        description = "Fraction of molecules that the MCS must cover. Defaults to 1.0. 0 &lt; threshold &lt;= 1.0.")
    @NumberInputWidget(minValidation = ThresholdMinValidation.class, maxValidation = ThresholdMaxValidation.class, 
    	stepSize = 0.05)
    @Persist(configKey = "threshold")
    double m_threshold = RDKitMCSNodeDialog.DEFAULT_THRESHOLD;

    static final class ThresholdMinValidation extends MinValidation {
    	
        @Override
        public double getMin() {
            return 0.0;
        }

        @Override
        public boolean isExclusive() {
            return true;
        }

    }

    static final class ThresholdMaxValidation extends MaxValidation {
    	
        @Override
        public double getMax() {
            return 1.0;
        }
        
    }

    @Widget(title = "Ring matches ring only",
        description = "Determines whether ring bonds only match other ring bonds. Default is unchecked.")
    @Persist(configKey = "ringMatchesRingOnly")
    boolean m_ringMatchesRingOnly = RDKitMCSNodeDialog.DEFAULT_RING_MATCHES_RING_ONLY_OPTION;

    @Widget(title = "Complete rings only",
        description = "Determines whether only complete rings are included in the MCS. Default is unchecked.")
    @Persist(configKey = "completeRingsOnly")
    boolean m_completeRingsOnly = RDKitMCSNodeDialog.DEFAULT_COMPLETE_RINGS_ONLY_OPTION;

    @Widget(title = "Match valences",
        description = "Determines whether atom valences are used in the comparison. Default is unchecked.")
    @Persist(configKey = "matchValences")
    boolean m_matchValences = RDKitMCSNodeDialog.DEFAULT_MATCH_VALENCES_OPTION;

    @Widget(title = "Atom comparison",
        description = "Specify here how atoms shall be compared.")
    @Persist(configKey = "atomComparison")
    @ValueSwitchWidget
    AtomComparison m_atomComparison = RDKitMCSNodeDialog.DEFAULT_ATOM_COMPARISON;

    @Widget(title = "Bond comparison",
        description = "Specify here how bonds shall be compared.")
    @Persist(configKey = "bondComparison")
    @ValueSwitchWidget
    BondComparison m_bondComparison = RDKitMCSNodeDialog.DEFAULT_BOND_COMPARISON;

    @Widget(title = "Timeout (in seconds)", description = """
    		Specify here how long the MCS calculation shall run at a maximum. Default is 300 seconds.
    		There is currently no way to interrupt a long-running MCS calculation. Even if you cancel an executing 
    		node the calculation will continue in the background and will occupy system resources until it is either 
    		done or the specified timeout occurs. Use longer timeouts with care!
    		""")
    @NumberInputWidget(minValidation = IsNonNegativeValidation.class, stepSize = 60)
    @Persist(configKey = "timeout")
    int m_timeout = RDKitMCSNodeDialog.DEFAULT_TIMEOUT;
    
    static final class InputColumnAutoGuessProvider extends RDKitMoleculeColumnAutoGuessProvider {
    	
        InputColumnAutoGuessProvider() {
            super(InputColumnNameRef.class, 0);
        }
        
    }
    
}
