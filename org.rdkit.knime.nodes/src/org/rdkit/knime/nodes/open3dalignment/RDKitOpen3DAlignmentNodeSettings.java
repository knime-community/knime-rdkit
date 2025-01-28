package org.rdkit.knime.nodes.open3dalignment;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.webui.node.dialog.defaultdialog.DefaultNodeSettings;
import org.knime.core.webui.node.dialog.defaultdialog.DefaultNodeSettings.DefaultNodeSettingsContext;
import org.knime.core.webui.node.dialog.defaultdialog.persistence.field.Persist;
import org.knime.core.webui.node.dialog.defaultdialog.widget.ChoicesProvider;
import org.knime.core.webui.node.dialog.defaultdialog.widget.ChoicesWidget;
import org.knime.core.webui.node.dialog.defaultdialog.widget.ColumnChoicesProvider;
import org.knime.core.webui.node.dialog.defaultdialog.widget.Widget;
import org.knime.core.webui.node.dialog.defaultdialog.widget.choices.ColumnChoicesProviderUtil.AllColumnChoicesProvider;
import org.rdkit.knime.types.RDKitMolValue;
import org.rdkit.knime.util.RDKitMoleculeColumnChoicesProvider;

/**
 * Settings for the RDKit Open3DAlignment node using the webui framework.
 * 
 * @author Marc Lehner, KNIME GmbH, Zurich, Switzerland
 */
public final class RDKitOpen3DAlignmentNodeSettings implements DefaultNodeSettings {
		
	private static final class ReferenceRDKitMoleculeColumnChoicesProvider implements ColumnChoicesProvider {
	    @Override
	    public DataColumnSpec[] columnChoices(final DefaultNodeSettingsContext context) {
			return context
					.getDataTableSpec(1).map(spec -> spec.stream()
							.filter(c -> c.getType().isCompatible(RDKitMolValue.class)).toArray(DataColumnSpec[]::new))
					.orElse(new DataColumnSpec[0]);
	    }
	}
	
    @Persist(configKey = "input_query_column")
    @Widget(
        title = "Query Molecule Column",
        description = "Select the input column from the first table that contains the query molecules (e.g., RDKit Mol, SMILES, or SDF)."
    )
    @ChoicesWidget(choices = RDKitMoleculeColumnChoicesProvider.class)
    String m_queryInputColumn;

    @Persist(configKey = "input_reference_column")
    @Widget(
        title = "Reference Molecule Column",
        description = "Select the input column from the second table that contains the reference molecules (e.g., RDKit Mol, SMILES, or SDF)."
    )
    @ChoicesWidget(choices = ReferenceRDKitMoleculeColumnChoicesProvider.class)
    String m_referenceInputColumn;

    @Persist(configKey = "new_aligned_column_name")
    @Widget(
        title = "Aligned Molecule Column Name",
        description = "Specify the name of the new column that will contain the aligned query molecules."
    )
    String m_newAlignedColumnName;

    @Persist(configKey = "remove_source_columns")
    @Widget(
        title = "Remove Source Column",
        description = "If enabled, the source query molecule column will be removed from the output table."
    )
    boolean m_removeSourceColumns = false;

    @Persist(configKey = "new_refid_column_name")
    @Widget(
        title = "Reference Row ID Column Name",
        description = "Specify the name of the new column that will contain the row IDs of the reference molecules used for alignment."
    )
    String m_newRefIdColumnName;

    @Persist(configKey = "new_rmsd_column_name")
    @Widget(
        title = "RMSD Column Name",
        description = "Specify the name of the new column that will contain the RMSD (Root Mean Square Deviation) values for the alignments."
    )
    String m_newRmsdColumnName;

    @Persist(configKey = "new_score_column_name")
    @Widget(
        title = "Score Column Name",
        description = "Specify the name of the new column that will contain the alignment scores."
    )
    String m_newScoreColumnName;

    @Persist(configKey = "allowReflection")
    @Widget(
        title = "Allow Reflection",
        description = "If enabled, the alignment process will allow reflection of the query molecule.",
        advanced = true
    )
    boolean m_allowReflection = false;

    @Persist(configKey = "maxIterations")
    @Widget(
        title = "Maximum Iterations",
        description = "Specify the maximum number of iterations allowed for the alignment process.",
        advanced = true
    )
    int m_maxIterations = 50;

    @Persist(configKey = "accuracy")
    @Widget(
        title = "Alignment Accuracy",
        description = "Specify the accuracy level for the alignment process (0 = most accurate, 3 = least accurate).",
        advanced = true
    )
    int m_accuracy = 0;
}