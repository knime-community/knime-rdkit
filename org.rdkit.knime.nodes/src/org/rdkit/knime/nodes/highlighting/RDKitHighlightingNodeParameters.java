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

package org.rdkit.knime.nodes.highlighting;

import java.awt.Color;
import java.util.List;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.IntValue;
import org.knime.core.data.LongValue;
import org.knime.core.data.collection.CollectionDataValue;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.node.parameters.NodeParameters;
import org.knime.node.parameters.NodeParametersInput;
import org.knime.node.parameters.Widget;
import org.knime.node.parameters.array.ArrayWidget;
import org.knime.node.parameters.array.ArrayWidget.ElementLayout;
import org.knime.node.parameters.experimental.persistence.array.ArrayPersistor;
import org.knime.node.parameters.experimental.persistence.array.ElementFieldPersistor;
import org.knime.node.parameters.experimental.persistence.array.PersistArray;
import org.knime.node.parameters.experimental.persistence.array.PersistArrayElement;
import org.knime.node.parameters.layout.After;
import org.knime.node.parameters.layout.HorizontalLayout;
import org.knime.node.parameters.layout.Layout;
import org.knime.node.parameters.layout.Section;
import org.knime.node.parameters.legacy.persistence.PersistWithin;
import org.knime.node.parameters.migration.LoadDefaultsForAbsentFields;
import org.knime.node.parameters.persistence.Persist;
import org.knime.node.parameters.updates.ParameterReference;
import org.knime.node.parameters.updates.ValueProvider;
import org.knime.node.parameters.updates.ValueReference;
import org.knime.node.parameters.widget.choices.ChoicesProvider;
import org.knime.node.parameters.widget.choices.ColumnChoicesProvider;
import org.knime.node.parameters.widget.choices.Label;
import org.knime.node.parameters.widget.choices.NoneChoice;
import org.knime.node.parameters.widget.choices.StringOrEnum;
import org.knime.node.parameters.widget.choices.ValueSwitchWidget;
import org.rdkit.knime.nodes.highlighting.HighlightingDefinition.Type;
import org.rdkit.knime.nodes.highlighting.RDKitHighlightingNodeParameters.Highlightings.HighlightingActivation;
import org.rdkit.knime.util.RDKitMoleculeColumnAutoGuessProvider;
import org.rdkit.knime.util.RDKitMoleculeColumnChoicesProvider;

/**
 * Node parameters for RDKit Molecule Highlighting.
 *
 * @author Magnus Gohm, KNIME GmbH, Konstanz, Germany
 * @author AI Migration Pipeline v1.2
 */
@SuppressWarnings("restriction")
@LoadDefaultsForAbsentFields
final class RDKitHighlightingNodeParameters implements NodeParameters {

	@Section(title = "Highlighting Definitions")
	interface HighlightingDefinitionsSection {
	}
	
    @Widget(title = "RDKit mol column", description = "The input column with RDKit Molecules.")
    @Persist(configKey = "input_column")
    @ChoicesProvider(RDKitMoleculeColumnChoicesProvider.class)
    @ValueProvider(InputColumnAutoGuessProvider.class)
    @ValueReference(InputColumnNameRef.class)
    String m_inputColumnName;

    static final class InputColumnNameRef implements ParameterReference<String> {
    }

    @Widget(title = "Column name for molecule with highlighting",
        description = "The name of the new column, which will contain the result.")
    @Persist(configKey = "new_column_name")
    String m_newColumnName;

    @Layout(HighlightingDefinitionsSection.class)
    @PersistWithin("highlighting")
    @PersistArray(HighlightingArrayPersistor.class)
    @Widget(title = "Highlighting definitions",
        description = "Each entry defines a set of atoms or bonds to highlight with an optional color.")
    @ArrayWidget(addButtonText = "Add new highlighting definition", elementLayout = ElementLayout.VERTICAL_CARD)
    Highlightings[] m_highlightings = new Highlightings[0];

    static final class Highlightings implements NodeParameters {

    	@HorizontalLayout
    	interface TopHighlightParamSection {
    	}

    	@After(TopHighlightParamSection.class)
    	interface MiddleHighlightParamSection {
    	}
    	
    	@After(MiddleHighlightParamSection.class)
    	interface BottomHighlightParamSection {
    	}
    	
    	@Layout(TopHighlightParamSection.class)
    	@PersistArrayElement(ActivePersistor.class)
        @Widget(title = "Highlighting", description = """
        		Activates the highlighting definition. Inactive definitions are ignored and not applied to the molecule.
        		""")
    	@ValueSwitchWidget
        HighlightingActivation m_active = HighlightingActivation.ACTIVE;

    	@Layout(TopHighlightParamSection.class)
    	@PersistArrayElement(TypePersistor.class)
        @Widget(title = "Type", description = "Determines whether the indexes refer to atoms or bonds.")
    	@ValueSwitchWidget
        Type m_type = Type.Atoms;

    	@Layout(MiddleHighlightParamSection.class)
    	@PersistArrayElement(InputColumnPersistor.class)
        @Widget(title = "Column with indexes",
            description = "Select the column that contains atom or bond indexes (integer or long collection).")
        @ChoicesProvider(IntLongCollectionColumnChoicesProvider.class)
        StringOrEnum<NoneChoice> m_inputColumn = new StringOrEnum<>(NoneChoice.NONE);

    	@Layout(BottomHighlightParamSection.class)
    	@PersistArrayElement(ColorPersistor.class)
        @Widget(title = "Color",
            description = "Hex color for highlighting (e.g. 0xFF0000). Leave blank to use the RDKit default color.")
        Color m_color;

    	@Layout(BottomHighlightParamSection.class)
    	@PersistArrayElement(NeighborhoodPersistor.class)
        @Widget(title = "Include neighborhood", description = """
        		Flag to also highlight bonds between two highlighted atoms or atoms around a highlighted bond.
        		""")
        boolean m_neighborhood;
        
        static final class IntLongCollectionColumnChoicesProvider implements ColumnChoicesProvider {
        	
        	@Override
        	public List<DataColumnSpec> columnChoices(final NodeParametersInput context) {
        		return context.getInTableSpec(0).map(spec -> 
        			spec.stream().filter(IntLongCollectionColumnChoicesProvider::isIntLongCollection).toList())
        				.orElse(List.of());
        	}
        	
        	private static boolean isIntLongCollection(final DataColumnSpec colSpec) {
        		final DataType type = colSpec.getType();
        		if (!type.isCompatible(CollectionDataValue.class)) {
        			return false;
        		}
        		final DataType elementType = type.getCollectionElementType();
        		return elementType != null
        				&& (elementType.isCompatible(IntValue.class) || elementType.isCompatible(LongValue.class));
        	}
        	
        }
        
        static final class ActivePersistor 
        	implements ElementFieldPersistor<HighlightingActivation, Integer, Highlightings> {
        	
        	@Override
        	public HighlightingActivation load(final NodeSettingsRO nodeSettings, final Integer idx) 
        		throws InvalidSettingsException {
        		return nodeSettings.getBoolean("select_" + idx, false) ?
        				HighlightingActivation.ACTIVE : HighlightingActivation.INACTIVE;
        	}
        	
        	@Override
        	public void save(final HighlightingActivation value, final Highlightings dto) {
        		dto.m_active = value;
        	}
        	
        	@Override
        	public String[][] getConfigPaths() {
        		return new String[][]{{"select_" + ARRAY_INDEX_PLACEHOLDER}};
        	}
        	
        }
        
        static final class TypePersistor implements ElementFieldPersistor<Type, Integer, Highlightings> {
        	
        	@Override
        	public Type load(final NodeSettingsRO nodeSettings, final Integer idx) throws InvalidSettingsException {
        		return Type.getFromValue(nodeSettings.getString("type_" + idx, Type.Atoms.name()));
        	}
        	
        	@Override
        	public void save(final Type value, final Highlightings dto) {
        		dto.m_type = value;
        	}
        	
        	@Override
        	public String[][] getConfigPaths() {
        		return new String[][]{{"type_" + ARRAY_INDEX_PLACEHOLDER}};
        	}
        	
        }
        
        static final class InputColumnPersistor 
        	implements ElementFieldPersistor<StringOrEnum<NoneChoice>, Integer, Highlightings> {
        	
        	@Override
        	public StringOrEnum<NoneChoice> load(final NodeSettingsRO nodeSettings, final Integer idx) 
        		throws InvalidSettingsException {
        		final var inputColumn = nodeSettings.getString("input_column_" + idx, null);
        		return inputColumn != null ? new StringOrEnum<>(inputColumn) : new StringOrEnum<>(NoneChoice.NONE);
        	}
        	
        	@Override
        	public void save(final StringOrEnum<NoneChoice> value, final Highlightings dto) {
        		dto.m_inputColumn = value;
        	}
        	
        	@Override
        	public String[][] getConfigPaths() {
        		return new String[][]{{"input_column_" + ARRAY_INDEX_PLACEHOLDER}};
        	}
        	
        }
        
        static final class ColorPersistor implements ElementFieldPersistor<Color, Integer, Highlightings> {
        	
        	@Override
        	public Color load(final NodeSettingsRO nodeSettings, final Integer idx) throws InvalidSettingsException {
        		return HighlightingDefinition.interpretColor(nodeSettings.getString("color_" + idx, null));
        	}
        	
        	@Override
        	public void save(final Color value, final Highlightings dto) {
        		dto.m_color = value;
        	}
        	
        	@Override
        	public String[][] getConfigPaths() {
        		return new String[][]{{"color_" + ARRAY_INDEX_PLACEHOLDER}};
        	}
        	
        }
        
        static final class NeighborhoodPersistor implements ElementFieldPersistor<Boolean, Integer, Highlightings> {
        	
        	@Override
        	public Boolean load(final NodeSettingsRO nodeSettings, final Integer idx)
        			throws InvalidSettingsException {
        		return nodeSettings.getBoolean("neighborhood_" + idx, false);
        	}
        	
        	@Override
        	public void save(final Boolean value, final Highlightings dto) {
        		dto.m_neighborhood = value;
        	}
        	
        	@Override
        	public String[][] getConfigPaths() {
        		return new String[][]{{"neighborhood_" + ARRAY_INDEX_PLACEHOLDER}};
        	}
        	
        }
        
        enum HighlightingActivation {
        	
        	@Label("Active")
        	ACTIVE, //
        	@Label("Inactive")
			INACTIVE;
        	
        }
        
	}

	static final class InputColumnAutoGuessProvider extends RDKitMoleculeColumnAutoGuessProvider {

		InputColumnAutoGuessProvider() {
			super(InputColumnNameRef.class, 0);
		}

	}
    
    static final class HighlightingArrayPersistor implements ArrayPersistor<Integer, Highlightings> {
    	
    	@Override
    	public int getArrayLength(final NodeSettingsRO nodeSettings) throws InvalidSettingsException {
    		return nodeSettings.getInt("count", 0);
    	}
    	
    	@Override
    	public Integer createElementLoadContext(final int index) {
    		return index;
    	}
    	
    	@Override
    	public Highlightings createElementSaveDTO(final int index) {
    		return new Highlightings();
    	}
    	
    	@Override
    	public void save(final List<Highlightings> savedElements, final NodeSettingsWO nodeSettings) {
			if (savedElements != null && !savedElements.isEmpty()) {
				nodeSettings.addInt("count", savedElements.size());
				for (int i = 0; i < savedElements.size(); i++) {
					saveElement(nodeSettings, i, savedElements.get(i));
				}
			} else {
				nodeSettings.addInt("count", 1);
				saveElement(nodeSettings, 0, new Highlightings());
			}
    	}
    	
    	private static void saveElement(NodeSettingsWO nodeSettings, int index, Highlightings element) {
    		nodeSettings.addBoolean("select_" + index, element.m_active == HighlightingActivation.ACTIVE);
			nodeSettings.addString("type_" + index, element.m_type.name());
			final var inputColumnChoice = element.m_inputColumn;
			nodeSettings.addString("input_column_" + index, inputColumnChoice.getEnumChoice().isEmpty()
					? inputColumnChoice.getStringChoice()
					: null);
			final var color = element.m_color;
			nodeSettings.addString("color_" + index, color == null ? null :
				"0x" + Integer.toHexString(color.getRGB() & 0xFFFFFF).toUpperCase());
			nodeSettings.addBoolean("neighborhood_" + index, element.m_neighborhood);
		}
    	
    }

}
