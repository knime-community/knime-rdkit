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

package org.rdkit.knime.nodes.moleculesubstructfilter;

import java.util.Optional;

import org.knime.core.data.DataColumnSpec;
import org.knime.node.parameters.Advanced;
import org.knime.node.parameters.NodeParameters;
import org.knime.node.parameters.NodeParametersInput;
import org.knime.node.parameters.Widget;
import org.knime.node.parameters.migration.LoadDefaultsForAbsentFields;
import org.knime.node.parameters.persistence.Persist;
import org.knime.node.parameters.updates.Effect;
import org.knime.node.parameters.updates.Effect.EffectType;
import org.knime.node.parameters.updates.EffectPredicate;
import org.knime.node.parameters.updates.EffectPredicateProvider;
import org.knime.node.parameters.updates.ParameterReference;
import org.knime.node.parameters.updates.util.BooleanReference;
import org.knime.node.parameters.updates.ValueProvider;
import org.knime.node.parameters.updates.ValueReference;
import org.knime.node.parameters.widget.choices.ChoicesProvider;
import org.knime.node.parameters.widget.choices.ValueSwitchWidget;
import org.knime.node.parameters.widget.number.NumberInputWidget;
import org.knime.node.parameters.widget.number.NumberInputWidgetValidation;
import org.knime.node.parameters.widget.number.NumberInputWidgetValidation.MinValidation.IsPositiveIntegerValidation;
import org.rdkit.knime.nodes.moleculesubstructfilter.RDKitMoleculeSubstructFilterNodeModel.MatchingCriteria;
import org.rdkit.knime.util.RDKitMoleculeColumnAutoGuessProvider;
import org.rdkit.knime.util.RDKitMoleculeColumnChoicesProvider;
import org.rdkit.knime.util.RDKitResultColumnNameAutoGuessProvider;


/**
 * Node parameters for RDKit Molecule Substructure Filter.
 *
 * @author Jannik Semperowitsch, KNIME GmbH, Konstanz, Germany
 * @author AI Migration Pipeline v1.2
 */
@LoadDefaultsForAbsentFields
final class RDKitMoleculeSubstructFilterNodeParameters implements NodeParameters {

    interface InputColumnRef extends ParameterReference<String> {
    }

    static final class InputColumnAutoGuesser extends RDKitMoleculeColumnAutoGuessProvider {
        InputColumnAutoGuesser() {
            super(InputColumnRef.class, 0);
        }
    }

    @Widget(title = "RDKit mol column",
        description = "Select the column from the first input table that contains the RDKit molecules.")
    @ChoicesProvider(RDKitMoleculeColumnChoicesProvider.class)
    @ValueProvider(InputColumnAutoGuesser.class)
    @ValueReference(InputColumnRef.class)
    @Persist(configKey = RDKitMoleculeSubstructFilterNodeModel.CFG_INPUT_COLUMN)
    String m_inputColumn;

    interface QueryColumnRef extends ParameterReference<String> {
    }

    /**
     * Auto-guesses the query column from the second input table. If both input ports are connected
     * to the same table (identical specs), the second compatible column is selected; otherwise the first.
     */
    static final class QueryColumnAutoGuesser extends RDKitMoleculeColumnAutoGuessProvider {
        QueryColumnAutoGuesser() {
            super(QueryColumnRef.class, 1, 0);
        }

        @Override
        protected Optional<DataColumnSpec> autoGuessColumn(final NodeParametersInput input) {
            final boolean sameTable = input.getInTableSpec(0).isPresent()
                && input.getInTableSpec(0).equals(input.getInTableSpec(1));
            m_valueIndex = sameTable ? 1 : 0;
            return super.autoGuessColumn(input);
        }
    }

    static final class QueryColumnChoices extends RDKitMoleculeColumnChoicesProvider {
        QueryColumnChoices() {
            super(1);
        }
    }

    @Widget(title = "Query mol column",
        description = "Select the column from the second input table that contains the query molecules. "
            + "Acceptable types are SMARTS, SMILES, SDF and RDKit Mol.")
    @ChoicesProvider(QueryColumnChoices.class)
    @ValueProvider(QueryColumnAutoGuesser.class)
    @ValueReference(QueryColumnRef.class)
    @Persist(configKey = RDKitMoleculeSubstructFilterNodeModel.CFG_QUERY_COLUMN)
    String m_queryColumn;

    static final class UseChiralityRef implements BooleanReference {
    }

    @Widget(title = "Use stereochemistry",
        description = "If this is set, information about stereochemistry will be used in the substructure search.")
    @ValueReference(UseChiralityRef.class)
    @Persist(configKey = RDKitMoleculeSubstructFilterNodeModel.CFG_USE_CHIRALITY)
    boolean m_useChirality;

    @Widget(title = "Use enhanced stereochemistry when matching",
        description = "If this is set, information about enhanced stereochemistry will be used in the substructure search.")
    @Effect(predicate = UseChiralityRef.class, type = EffectType.ENABLE)
    @Persist(configKey = RDKitMoleculeSubstructFilterNodeModel.CFG_USE_ENHANCED_STEREO)
    boolean m_useEnhancedStereo;

    interface MatchingCriteriaRef extends ParameterReference<MatchingCriteria> {
    }

    @Widget(title = "Matches",
        description = "Select the minimum number of patterns that must match for each molecule so that it is "
            + "included in the first output table.")
    @ValueReference(MatchingCriteriaRef.class)
    @ValueSwitchWidget
    @Persist(configKey = RDKitMoleculeSubstructFilterNodeModel.CFG_MATCHING)
    MatchingCriteria m_matchingCriteria = MatchingCriteria.All;

    static final class MinimumMatchesDisabled implements EffectPredicateProvider {
        @Override
        public EffectPredicate init(final PredicateInitializer i) {
            return i.getEnum(MatchingCriteriaRef.class).isOneOf(MatchingCriteria.All, MatchingCriteria.Exact);
        }
    }

    @Widget(title = "Minimum number of matches",
        description = "The minimum number of patterns that must match when 'At least' is selected.")
    @Effect(predicate = MinimumMatchesDisabled.class, type = EffectType.DISABLE)
    @Persist(configKey = RDKitMoleculeSubstructFilterNodeModel.CFG_MINIMUM_MATCHES)
    @NumberInputWidget(minValidation = IsPositiveIntegerValidation.class, maxValidation = Max999Validation.class)
    int m_minimumMatches = 1;
    
    static final class Max999Validation extends NumberInputWidgetValidation.MaxValidation {
        @Override
        protected double getMax() {
            return 999;
        }
    }

    @Widget(title = "New column name for matching substructures",
        description = "Enter here the name of the new column that will contain a list of all matching substructure row indices.")
    @Persist(configKey = RDKitMoleculeSubstructFilterNodeModel.CFG_NEW_COLUMN_NAME)
    @ValueProvider(NewColumnNameProvider.class)
    @ValueReference(NewColumnNameRef.class)
    String m_newColumnName;
    
    static final class NewColumnNameRef implements ParameterReference<String> {
	}
    
    static final class NewColumnNameProvider extends RDKitResultColumnNameAutoGuessProvider {

        protected NewColumnNameProvider() {
            super("Matched Substructs", InputColumnRef.class, NewColumnNameRef.class);
        }

    }

    @Widget(title = "Fingerprint screening threshold",
        description = "Substructure search performance can be improved using fingerprints. This makes sense when "
            + "there are many different query molecules and a lot of input molecules. Enter here the minimum "
            + "number of query molecules that must be present to enable fingerprint screening. "
            + "A value of 0 disables fingerprint screening. A value of -1 uses the current default threshold.")
    @Advanced
    @NumberInputWidget(minValidation = MinNeg1Validation.class)
    @Persist(configKey = RDKitMoleculeSubstructFilterNodeModel.CFG_FP_SCREENING_THRESHOLD)
    int m_fingerprintScreeningThreshold = RDKitMoleculeSubstructFilterNodeModel.DEFAULT_FINGERPRINT_SCREENING_THRESHOLD;
    
    static final class MinNeg1Validation extends NumberInputWidgetValidation.MinValidation {
        @Override
        protected double getMin() {
            return -1;
        }
    }

    @Widget(title = "Use row keys as substructure match information",
        description = "The column for matching substructure indices contained (for historic reasons) the row index, "
            + "which turned out not to be too useful. Click this flag to use row keys instead.")
    @Advanced
    @Persist(configKey = RDKitMoleculeSubstructFilterNodeModel.CFG_ROW_KEY_MATCH_INFO)
    boolean m_rowKeyMatchInfo = true;

}
