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

package org.rdkit.knime.nodes.onecomponentreaction2;

import java.util.List;
import java.util.Optional;

import org.knime.chem.types.RxnValue;
import org.knime.core.data.DataColumnSpec;
import org.knime.node.parameters.Advanced;
import org.knime.node.parameters.NodeParameters;
import org.knime.node.parameters.NodeParametersInput;
import org.knime.node.parameters.Widget;
import org.knime.node.parameters.migration.LoadDefaultsForAbsentFields;
import org.knime.node.parameters.persistence.Persist;
import org.knime.node.parameters.persistence.Persistor;
import org.knime.node.parameters.updates.Effect;
import org.knime.node.parameters.updates.Effect.EffectType;
import org.knime.node.parameters.updates.EffectPredicate;
import org.knime.node.parameters.updates.EffectPredicateProvider;
import org.knime.node.parameters.updates.ParameterReference;
import org.knime.node.parameters.updates.ValueProvider;
import org.knime.node.parameters.updates.ValueReference;
import org.knime.node.parameters.updates.util.BooleanReference;
import org.knime.node.parameters.widget.choices.ChoicesProvider;
import org.knime.node.parameters.widget.choices.filter.ColumnFilter;
import org.knime.node.parameters.widget.choices.filter.ColumnFilterWidget;
import org.knime.node.parameters.widget.choices.util.AllColumnsProvider;
import org.knime.node.parameters.widget.choices.util.ColumnSelectionUtil;
import org.knime.node.parameters.widget.choices.util.CompatibleColumnsProvider;
import org.knime.node.parameters.widget.number.NumberInputWidget;
import org.knime.node.parameters.widget.number.NumberInputWidgetValidation.MinValidation.IsPositiveIntegerValidation;
import org.rdkit.knime.util.RDKitLegacyPersistors.LegacyMoleculeColumnPersistor;
import org.rdkit.knime.util.RDKitMoleculeColumnAutoGuessProvider;
import org.rdkit.knime.util.RDKitMoleculeColumnChoicesProvider;
import org.knime.node.parameters.legacy.updates.ColumnNameAutoGuessValueProvider;
import org.knime.node.parameters.legacy.widget.choices.filter.LegacyColumnFilterPersistor;

/**
 * Node parameters for RDKit One Component Reaction.
 *
 * @author Magnus Gohm, KNIME GmbH, Konstanz, Germany
 * @author AI Migration Pipeline v1.2
 */
@LoadDefaultsForAbsentFields
final class RDKitOneComponentReactionNodeParameters implements NodeParameters {

    @Widget(title = "Reactant RDKit mol column",
        description = "The column from the first table containing reactant molecules.")
    @Persistor(ReactantColumnPersistor.class)
    @ChoicesProvider(RDKitMoleculeColumnChoicesProvider.class)
    @ValueProvider(ReactantColumnAutoGuessProvider.class)
    @ValueReference(ReactantColumnRef.class)
    String m_inputColumnName;

    static final class ReactantColumnRef implements ParameterReference<String> {
    }

    @Widget(title = "Rxn column",
        description = "The column from the second table containing the Rxn. "
            + "Only used when a reaction table is connected to the second input port.")
    @Persist(configKey = "rxnColumn")
    @ChoicesProvider(RxnColumnChoicesProvider.class)
    @ValueProvider(RxnColumnAutoGuessProvider.class)
    @ValueReference(RxnColumnRef.class)
    @Effect(predicate = IsOptionalPortConnected.class, type = EffectType.SHOW)
    String m_rxnColumnName;

    static final class RxnColumnRef implements ParameterReference<String> {
	}
    
    @Widget(title = "Reaction SMARTS", description = """
    		A reaction SMARTS describing the reaction. Only used when no reaction table is connected to the second 
    		input port. For a description of the format, please have a look in 
    		<a href=\"http://rdkit.org/docs/RDKit_Book.html#reaction-smarts\">The RDKit Book</a>
            """)
    @Persist(configKey = "reactionSmarts")
    String m_reactionSmarts = "";

    @Widget(title = "Randomize reactants",
        description = "If checked, random reactants will be picked for the reactions.")
    @Persist(configKey = "randomizeReactants")
    @ValueReference(IsRandomizeReactant.class)
    boolean m_randomizeReactants;

    static final class IsRandomizeReactant implements BooleanReference {
    }
    
    @Widget(title = "Maximum number of random reactions",
        description = "Specify here the maximum number of reactions to be calculated.")
    @NumberInputWidget(minValidation = IsPositiveIntegerValidation.class)
    @Persist(configKey = "maxNumberOfRandomizedReactions")
    @Effect(predicate = IsRandomizeReactant.class, type = EffectType.SHOW)
    int m_maxNumberOfRandomizedReactions = 100;

    @Widget(title = "Random seed",
        description = "Specify here a seed for the random number generator or -1 to use it without a seed.")
    @Persist(configKey = "randomSeed")
    @Effect(predicate = IsRandomizeReactant.class, type = EffectType.SHOW)
    long m_randomSeed = -1L;

    @Widget(title = "Uniquify products",
        description = "Enable this option to filter out duplicates of products caused by symmetry in molecules. "
            + "Only the first of multiple encountered products will show up in the result table.")
    @Persist(configKey = "uniquifyProducts")
    boolean m_uniquifyProducts;

    @Advanced
    @Widget(title = "Include additional columns from reactant input table into product output table",
        description = "Enable this option in order to select additional data columns from reactant input table "
            + "to be included into the result table.")
    @Persist(configKey = "additionalColumnsEnabled")
    @ValueReference(IsAdditionalColumnsEnabled.class)
    boolean m_additionalColumnsEnabled;

    static final class IsAdditionalColumnsEnabled implements BooleanReference {
    }

    @Advanced
    @Widget(title = "Additional columns from reactant table",
        description = "Selection of additional data columns from reactant input table.")
    @Persistor(AdditionalColumnsFilterPersistor.class)
    @ColumnFilterWidget(choicesProvider = AllColumnsProvider.class)
    @Effect(predicate = IsAdditionalColumnsEnabled.class, type = EffectType.SHOW)
    ColumnFilter m_additionalColumnsFilter = new ColumnFilter();

    static final class IsOptionalPortConnected implements EffectPredicateProvider {

		@Override
		public EffectPredicate init(PredicateInitializer i) {
			return i.getConstant(pi -> pi.getInPortObject(1).isPresent());
		}
    	
    }
    
    static final class ReactantColumnAutoGuessProvider extends RDKitMoleculeColumnAutoGuessProvider {
    	
        ReactantColumnAutoGuessProvider() {
            super(ReactantColumnRef.class, 0);
        }
        
    }

    static final class ReactantColumnPersistor extends LegacyMoleculeColumnPersistor {
    	
        ReactantColumnPersistor() {
            super("input_column", "firstColumn");
        }
        
    }
    
    static final class RxnColumnAutoGuessProvider extends ColumnNameAutoGuessValueProvider {

		protected RxnColumnAutoGuessProvider() {
			super(RxnColumnRef.class);
		}

		@Override
		protected Optional<DataColumnSpec> autoGuessColumn(NodeParametersInput parametersInput) {
			return ColumnSelectionUtil.getFirstCompatibleColumnOfFirstPort(parametersInput, RxnValue.class);
		}
    	
    }
    
    static final class RxnColumnChoicesProvider extends CompatibleColumnsProvider {

		protected RxnColumnChoicesProvider() {
			super(List.of(RxnValue.class));
		}
        
    }
    
    static final class AdditionalColumnsFilterPersistor extends LegacyColumnFilterPersistor {
    	
        AdditionalColumnsFilterPersistor() {
            super("additionalColumnsFilter");
        }
        
    }
    
}
