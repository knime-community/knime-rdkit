/*
 * ------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright (C)2012-2023
 * Novartis Pharma AG, Switzerland
 *
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
 *  KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
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
 * ---------------------------------------------------------------------
 */
package org.rdkit.knime.nodes.substructurecounter;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Supplier;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.StringValue;
import org.knime.node.parameters.NodeParameters;
import org.knime.node.parameters.NodeParametersInput;
import org.knime.node.parameters.Widget;
import org.knime.node.parameters.layout.After;
import org.knime.node.parameters.layout.Layout;
import org.knime.node.parameters.layout.Section;
import org.knime.node.parameters.legacy.updates.ColumnNameAutoGuessValueProvider;
import org.knime.node.parameters.migration.LoadDefaultsForAbsentFields;
import org.knime.node.parameters.persistence.Persist;
import org.knime.node.parameters.updates.Effect;
import org.knime.node.parameters.updates.Effect.EffectType;
import org.knime.node.parameters.updates.ParameterReference;
import org.knime.node.parameters.updates.ValueProvider;
import org.knime.node.parameters.updates.ValueReference;
import org.knime.node.parameters.updates.util.BooleanReference;
import org.knime.node.parameters.widget.choices.ChoicesProvider;
import org.knime.node.parameters.widget.choices.util.ColumnSelectionUtil;
import org.knime.node.parameters.widget.choices.util.CompatibleColumnsProvider;
import org.rdkit.knime.util.RDKitAdapterCellSupport;
import org.rdkit.knime.util.RDKitMoleculeColumnAutoGuessProvider;
import org.rdkit.knime.util.RDKitMoleculeColumnChoicesProvider;
import org.rdkit.knime.util.RDKitResultColumnNameAutoGuessProvider;

/**
 * Node parameters for the "RDKit Substructure Counter" node.
 *
 * @author Jannik Semperowitsch, KNIME GmbH, Konstanz, Germany
 * @author AI Migration Pipeline v1.2
 */
@LoadDefaultsForAbsentFields
final class SubstructureCounterNodeParameters implements NodeParameters {

    @Widget(title = "RDKit mol column", description = "The input column with RDKit Molecules.")
    @ChoicesProvider(RDKitMoleculeColumnChoicesProvider.class)
    @ValueReference(InputColumnRef.class)
    @ValueProvider(InputColumnAutoGuess.class)
    @Persist(configKey = SubstructureCounterNodeModel.CFG_INPUT_COLUMN)
    String m_inputColumn;

    static final class InputColumnRef implements ParameterReference<String> {
    }

    static final class InputColumnAutoGuess extends RDKitMoleculeColumnAutoGuessProvider {
        InputColumnAutoGuess() {
            super(InputColumnRef.class, 0);
        }
    }

    @Widget(title = "Input query column",
        description = "The name of the column in second table containing query molecules which will act as "
            + "substructure for the input molecules.")
    @ChoicesProvider(QueryColumnChoicesProvider.class)
    @ValueReference(QueryColumnRef.class)
    @ValueProvider(QueryColumnAutoGuesser.class)
    @Persist(configKey = SubstructureCounterNodeModel.CFG_INPUT_QUERY_COLUMN)
    String m_queryInputColumn;

    static final class QueryColumnRef implements ParameterReference<String> {
    }

    static final class QueryColumnChoicesProvider extends RDKitMoleculeColumnChoicesProvider {
        QueryColumnChoicesProvider() {
            super(1);
        }
    }

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

    @Section(title = "Search")
    interface SearchSection {
    }

    @Section(title = "Output")
    @After(SearchSection.class)
    interface OutputSection {
    }

    @Widget(title = "Count unique matches only",
        description = "This option is selected if user requires unique matches of the query molecule in an input "
            + "molecule.")
    @Layout(SearchSection.class)
    @Persist(configKey = SubstructureCounterNodeModel.CFG_COUNT_UNIQUE_MATCHES)
    boolean m_uniqueMatchesOnly = true;

    @Widget(title = "Use chirality when matching",
        description = "If this is set, information about stereochemistry will be used in the substructure search.")
    @ValueReference(UseChiralityRef.class)
    @Layout(SearchSection.class)
    @Persist(configKey = SubstructureCounterNodeModel.CFG_USE_CHIRALITY)
    boolean m_useChirality;

    static final class UseChiralityRef implements BooleanReference {
    }

    @Widget(title = "Use enhanced stereochemistry when matching",
        description = "If this is set, information about enhanced stereochemistry will be used in the substructure "
            + "search.")
    @Effect(predicate = UseChiralityRef.class, type = EffectType.ENABLE)
    @Layout(SearchSection.class)
    @Persist(configKey = SubstructureCounterNodeModel.CFG_USE_ENHANCED_STEREO)
    boolean m_useEnhancedStereo;

    @Widget(title = "Instead of query molecules use names as result header titles (and tags)",
        description = "Select this option to allow the node to use names specified in the query table as column "
            + "title for the resulting count columns and tags. If disabled, it will use the SMILES or SMARTS value as "
            + "title.")
    @ValueReference(UseQueryNameColumnRef.class)
    @Layout(OutputSection.class)
    @Persist(configKey = SubstructureCounterNodeModel.CFG_USE_QUERY_NAME_COLUMN)
    boolean m_useQueryNameColumn;

    static final class UseQueryNameColumnRef implements BooleanReference {
    }

    @Widget(title = "Column with names for header titles",
        description = "If the last option is enabled the column with the query names must be selected here.")
    @ChoicesProvider(StringQueryColumnsProvider.class)
    @ValueReference(QueryNameColumnRef.class)
    @ValueProvider(QueryNameColumnAutoGuess.class)
    @Effect(predicate = UseQueryNameColumnRef.class, type = EffectType.ENABLE)
    @Layout(OutputSection.class)
    @Persist(configKey = SubstructureCounterNodeModel.CFG_QUERY_NAME_COLUMN)
    String m_queryNameColumn;

    static final class QueryNameColumnRef implements ParameterReference<String> {
    }
    
    static final class StringQueryColumnsProvider extends CompatibleColumnsProvider {

		protected StringQueryColumnsProvider() {
			super(Arrays.asList(RDKitAdapterCellSupport.expandByAdaptableTypes(StringValue.class)));
		}
    	
    }

    static final class QueryNameColumnAutoGuess extends ColumnNameAutoGuessValueProvider {
        QueryNameColumnAutoGuess() {
            super(QueryNameColumnRef.class);
        }

        @Override
        protected Optional<DataColumnSpec> autoGuessColumn(final NodeParametersInput parametersInput) {
        	return ColumnSelectionUtil.getFirstCompatibleColumn(parametersInput, 0,
                    RDKitAdapterCellSupport.expandByAdaptableTypes(StringValue.class));
        }
    }

    @Widget(title = "Add total hits count column",
        description = "Select this option to add a column that contains the accumulated hits count of all hits.")
    @ValueReference(CountTotalHitsRef.class)
    @Layout(OutputSection.class)
    @Persist(configKey = SubstructureCounterNodeModel.CFG_COUNT_TOTAL_HITS)
    boolean m_countTotalHits;

    static final class CountTotalHitsRef implements BooleanReference {
    }

    @Widget(title = "New column name for total hits count",
        description = "If the last option is enabled the name for the new column must be specified here.")
    @Effect(predicate = CountTotalHitsRef.class, type = EffectType.ENABLE)
    @Layout(OutputSection.class)
    @Persist(configKey = SubstructureCounterNodeModel.CFG_COUNT_TOTAL_HITS_COLUMN)
    @ValueProvider(CountTotalHitsColumnNameProvider.class)
    @ValueReference(CountTotalHitsColumnNameRef.class)
    String m_countTotalHitsColumn = SubstructureCounterNodeModel.DEFAULT_TOTAL_HITS_COLUMN;

    static final class CountTotalHitsColumnNameRef implements ParameterReference<String> {
    }
    
    static final class CountTotalHitsColumnNameProvider extends RDKitResultColumnNameAutoGuessProvider {

		protected CountTotalHitsColumnNameProvider() {
			super(SubstructureCounterNodeModel.DEFAULT_TOTAL_HITS_COLUMN, 
					InputColumnRef.class, CountTotalHitsColumnNameRef.class);
		}
    	
    }

    @Widget(title = "Add column with tags for matching queries",
        description = "Select this option to add a column that contains a collection of all tags (SMILES, SMARTS "
            + "values or tags taken from header titles column) for the matching queries. This can be useful for "
            + "further processing.")
    @ValueReference(TrackQueryTagsRef.class)
    @Layout(OutputSection.class)
    @Persist(configKey = SubstructureCounterNodeModel.CFG_TRACK_QUERY_TAGS)
    boolean m_trackQueryTags;

    static final class TrackQueryTagsRef implements BooleanReference {
    }

    @Widget(title = "New column name for tags",
        description = "If the last option is enabled the name for the new column must be specified here.")
    @Effect(predicate = TrackQueryTagsRef.class, type = EffectType.ENABLE)
    @Layout(OutputSection.class)
    @Persist(configKey = SubstructureCounterNodeModel.CFG_TRACK_QUERY_TAGS_COLUMN)
    @ValueProvider(TrackQueryTagsColumnNameProvider.class)
    @ValueReference(TrackQueryTagsColumnNameRef.class)
    String m_trackQueryTagsColumn = SubstructureCounterNodeModel.DEFAULT_QUERY_TAGS_COLUMN;

    static final class TrackQueryTagsColumnNameRef implements ParameterReference<String> {
    }
    
    static final class TrackQueryTagsColumnNameProvider extends RDKitResultColumnNameAutoGuessProvider {

		protected TrackQueryTagsColumnNameProvider() {
			super(SubstructureCounterNodeModel.DEFAULT_QUERY_TAGS_COLUMN, 
					InputColumnRef.class, TrackQueryTagsColumnNameRef.class);
		}
		
		private Supplier<String> m_countTotalHitsColumn;
		
		@Override
		public void init(StateProviderInitializer initializer) {
			super.init(initializer);
			m_countTotalHitsColumn = initializer.getValueSupplier(CountTotalHitsColumnNameRef.class);
		}

		@Override
		protected String[] getExcludedColumnNames(NodeParametersInput parametersInput, 
			final String currentInputColumnName) {
			return new String[] { m_countTotalHitsColumn.get() };
		}
    	
    }
    
}
