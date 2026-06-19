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

package org.rdkit.knime.nodes.fingerprintwriter;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.StringValue;
import org.knime.core.data.vector.bitvector.BitVectorValue;
import org.knime.node.parameters.NodeParameters;
import org.knime.node.parameters.NodeParametersInput;
import org.knime.node.parameters.Widget;
import org.knime.node.parameters.layout.After;
import org.knime.node.parameters.layout.Layout;
import org.knime.node.parameters.layout.Section;
import org.knime.node.parameters.migration.LoadDefaultsForAbsentFields;
import org.knime.node.parameters.modification.Modification;
import org.knime.node.parameters.modification.Modification.WidgetGroupModifier;
import org.knime.node.parameters.persistence.Persist;
import org.knime.node.parameters.persistence.Persistor;
import org.knime.node.parameters.updates.ParameterReference;
import org.knime.node.parameters.updates.ValueProvider;
import org.knime.node.parameters.updates.ValueReference;
import org.knime.node.parameters.legacy.persistence.PersistWithin;
import org.knime.node.parameters.legacy.updates.ColumnNameAutoGuessValueProvider;
import org.knime.node.parameters.legacy.widget.file.LegacyFileWriterWithOverwritePolicyOptions;
import org.knime.node.parameters.legacy.widget.file.LegacyFileWriterWithOverwritePolicyOptions.OverwritePolicy;
import org.knime.node.parameters.widget.choices.ChoicesProvider;
import org.knime.node.parameters.widget.choices.RowIDChoice;
import org.knime.node.parameters.widget.choices.StringOrEnum;
import org.knime.node.parameters.widget.choices.util.ColumnSelectionUtil;
import org.knime.node.parameters.widget.choices.util.CompatibleColumnsProvider;
import org.knime.node.parameters.widget.file.FileWriterWidget;
import org.rdkit.knime.util.RDKitAdapterCellSupport;
import org.rdkit.knime.util.RDKitLegacyPersistors.LegacyColumnNamePersistor;
import org.rdkit.knime.util.RDKitLegacyPersistors.StringOrEnumColumnNameAutoGuessProvider;

/**
 * Node parameters for RDKit Fingerprint Writer.
 *
 * @author Magnus Gohm, KNIME GmbH, Konstanz, Germany
 * @author AI Migration Pipeline v1.2
 */
@LoadDefaultsForAbsentFields
final class RDKitFingerprintWriterV2NodeParameters implements NodeParameters {

    @Section(title = "Output File")
    interface OutputFileSection {
    }

    @Section(title = "Column Selection")
    @After(OutputFileSection.class)
    interface ColumnSelectionSection {
    }
    
    @Layout(OutputFileSection.class)
    @Persist(configKey = "output_file")
    @Modification(OutputFileModifier.class)
    LegacyFileWriterWithOverwritePolicyOptions m_outputFile = new LegacyFileWriterWithOverwritePolicyOptions();

    @Layout(ColumnSelectionSection.class)
    @Persist(configKey = "fps_column")
    @Widget(title = "Fingerprint column", 
    	description = "The input column containing fingerprints (DenseBitVector cells).")
    @ChoicesProvider(FingerprintColumnChoicesProvider.class)
    @ValueProvider(FingerPrintAutoGuessProvider.class)
    @ValueReference(FingerprintColumnRef.class)
    String m_fingerprintColumn = "";

    static final class FingerprintColumnRef implements ParameterReference<String> {
    }
    
    @Layout(ColumnSelectionSection.class)
    @Widget(title = "ID column",
        description = "The input column containing IDs that shall be written as the second column into the FPS file."
            + " It is possible to use Row IDs.")
    @PersistWithin("id_column")
    @Persistor(IdColumnPersistor.class)
    @ChoicesProvider(IdColumnChoicesProvider.class)
    @ValueProvider(IdColumnAutoGuessProvider.class)
    @ValueReference(IdColumnRef.class)
    StringOrEnum<RowIDChoice> m_idColumn = new StringOrEnum<>(RowIDChoice.ROW_ID);
    
    static final class IdColumnRef implements ParameterReference<StringOrEnum<RowIDChoice>> {
    }
    
    // Hidden test-only setting: suppresses the timestamp in the FPS file header.
    // Intended to be set via flow variable only (e.g. for file comparison tests).
    @Persist(configKey = "suppress_time")
    int m_suppressTime = 0;
    
    static final class OutputFileModifier implements LegacyFileWriterWithOverwritePolicyOptions.Modifier {
    	
        @Override
        public void modify(final WidgetGroupModifier group) {
            findFileSelection(group).modifyAnnotation(Widget.class)
                .withProperty("title", "Output file")
                .withProperty("description", """
                		The location of the output file where the FPS file will be created.
                		""")
                .modify();
            findFileSelection(group).modifyAnnotation(FileWriterWidget.class)
                .withProperty("fileExtension", "fps")
                .modify();

            findCreateMissingFolders(group).modifyAnnotation(Widget.class)
                .withProperty("description", """
                		Select if the folders of the selected output location should be created if they do not already 
                		exist. If this option is unchecked, the node will fail if a folder does not exist.
                        """)
                .modify();

            restrictOverwritePolicyOptions(group, FpsOverwritePolicyChoicesProvider.class);
        }
        
    }

    static final class FpsOverwritePolicyChoicesProvider 
    	extends LegacyFileWriterWithOverwritePolicyOptions.OverwritePolicyChoicesProvider {
        
    	@Override
        protected List<OverwritePolicy> getChoices() {
            return List.of(OverwritePolicy.fail, OverwritePolicy.overwrite);
        }
    	
    }
    
    static final class FingerPrintAutoGuessProvider extends ColumnNameAutoGuessValueProvider {

		protected FingerPrintAutoGuessProvider() {
			super(FingerprintColumnRef.class);
		}

		@Override
		protected Optional<DataColumnSpec> autoGuessColumn(NodeParametersInput parametersInput) {
			return ColumnSelectionUtil.getFirstCompatibleColumnOfFirstPort(parametersInput, 
					RDKitAdapterCellSupport.expandByAdaptableTypes(BitVectorValue.class));
		}
    	
    }
    
    static final class FingerprintColumnChoicesProvider extends CompatibleColumnsProvider {

		protected FingerprintColumnChoicesProvider() {
			super(Arrays.asList(RDKitAdapterCellSupport.expandByAdaptableTypes(BitVectorValue.class)));
		}
        
    }
    
    static final class IdColumnAutoGuessProvider extends StringOrEnumColumnNameAutoGuessProvider<RowIDChoice> {

		protected IdColumnAutoGuessProvider() {
			super(IdColumnRef.class);
		}

		@Override
		protected Optional<DataColumnSpec> autoGuessColumn(NodeParametersInput parametersInput) {
			return ColumnSelectionUtil.getFirstCompatibleColumnOfFirstPort(parametersInput, 
					RDKitAdapterCellSupport.expandByAdaptableTypes(StringValue.class));
		} 
    	
    }
    
    static final class IdColumnChoicesProvider extends CompatibleColumnsProvider {

		protected IdColumnChoicesProvider() {
			super(Arrays.asList(RDKitAdapterCellSupport.expandByAdaptableTypes(StringValue.class)));
		}
    	
    }
    
    static final class IdColumnPersistor extends LegacyColumnNamePersistor {
    	
    	IdColumnPersistor() {
		}
    	
    }

}
