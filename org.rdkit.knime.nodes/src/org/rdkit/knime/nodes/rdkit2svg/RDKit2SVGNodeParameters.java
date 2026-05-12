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


package org.rdkit.knime.nodes.rdkit2svg;

import java.util.List;

import org.RDKit.MolDrawOptions;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.node.parameters.NodeParameters;
import org.knime.node.parameters.Widget;
import org.knime.node.parameters.layout.Layout;
import org.knime.node.parameters.layout.Section;
import org.knime.node.parameters.migration.ConfigMigration;
import org.knime.node.parameters.migration.LoadDefaultsForAbsentFields;
import org.knime.node.parameters.migration.Migration;
import org.knime.node.parameters.migration.NodeParametersMigration;
import org.knime.node.parameters.persistence.Persist;
import org.knime.node.parameters.updates.ParameterReference;
import org.knime.node.parameters.updates.ValueProvider;
import org.knime.node.parameters.updates.ValueReference;
import org.knime.node.parameters.widget.choices.ChoicesProvider;
import org.knime.node.parameters.widget.number.NumberInputWidget;
import org.knime.node.parameters.widget.number.NumberInputWidgetValidation.MinValidation;
import org.knime.node.parameters.widget.number.NumberInputWidgetValidation.MaxValidation;
import org.knime.node.parameters.widget.number.NumberInputWidgetValidation.MinValidation.IsNonNegativeValidation;
import org.rdkit.knime.util.RDKitMoleculeColumnAutoGuessProvider;
import org.rdkit.knime.util.RDKitMoleculeColumnChoicesProvider;

/**
 * Node parameters for RDKit Molecule to SVG.
 *
 * @author Magnus Gohm, KNIME GmbH, Konstanz, Germany
 * @author AI Migration Pipeline v1.2
 */
@LoadDefaultsForAbsentFields
final class RDKit2SVGNodeParameters implements NodeParameters {

    private static final MolDrawOptions RDKIT_DEFAULTS = RDKit2SVGNodeDialog.RDKIT_DEFAULT_PARAMETERS;

    @Section(title = "Drawing Options")
    interface DrawingOptionsSection {
    }

    @Widget(title = "RDKit mol column", description = "The input column with RDKit Molecules.")
    @Persist(configKey = "input_column")
    @ChoicesProvider(RDKitMoleculeColumnChoicesProvider.class)
    @ValueProvider(MolColumnAutoGuessProvider.class)
    @ValueReference(MolColumnRef.class)
    String m_inputColumn;

    static final class MolColumnRef implements ParameterReference<String> {
    }

    @Widget(title = "New column name", description = "The name of the new column which will contain the SVG.")
    @Persist(configKey = "new_column_name")
    String m_newColumnName;

    @Widget(title = "Remove source column",
        description = "Set to true to remove the specified source column from the result table.")
    @Persist(configKey = "remove_source_columns")
    boolean m_removeSourceColumns;

    @Layout(DrawingOptionsSection.class)
    @Widget(title = "Prepare before drawing",
        description = """
            Perform a number of cleanup operations on the molecule before drawing it. The operations \
            performed are: <ol> <li>Kekulization</li> <li>Adding chiral Hs</li> \
            <li>Generating 2D coordinates (if necessary)</li> <li>Wedging bonds around chiral centers</li> \
            <li>Adding wavy bonds around chiral centers which have been marked as unspecified</li> </ol>
            """)
    @Persist(configKey = "prepare_before_drawing")
    boolean m_prepareBeforeDrawing = RDKIT_DEFAULTS.getPrepareMolsBeforeDrawing();

    @Layout(DrawingOptionsSection.class)
    @Widget(title = "Center before drawing",
        description = "Centers the molecule at the origin before drawing.")
    @Persist(configKey = "center_before_drawing")
    boolean m_centerBeforeDrawing = RDKIT_DEFAULTS.getCentreMoleculesBeforeDrawing();

    @Layout(DrawingOptionsSection.class)
    @Widget(title = "Clear background",
        description = "Clears the background with the current background color before drawing.")
    @Persist(configKey = "clear_background")
    boolean m_clearBackground = RDKIT_DEFAULTS.getClearBackground();

    @Layout(DrawingOptionsSection.class)
    @Widget(title = "Add atom indices", description = "Includes atom indices in the drawing.")
    @Persist(configKey = "add_atom_indices")
    boolean m_addAtomIndices = RDKIT_DEFAULTS.getAddAtomIndices();

    @Layout(DrawingOptionsSection.class)
    @Widget(title = "Add bond indices", description = "Includes bond indices in the drawing.")
    @Persist(configKey = "add_bond_indices")
    boolean m_addBondIndices = RDKIT_DEFAULTS.getAddBondIndices();

    @Layout(DrawingOptionsSection.class)
    @Widget(title = "Draw explicit methyl groups",
        description = "Draws terminal methyl groups as CH3.")
    @Persist(configKey = "explicit_methyl")
    boolean m_explicitMethyl = RDKIT_DEFAULTS.getExplicitMethyl();

    @Layout(DrawingOptionsSection.class)
    @Widget(title = "Dummies are attachment points",
        description = "Draws dummies as attachment points - wavy lines perpendicular to the bond.")
    @Persist(configKey = "dummies_are_attachments")
    boolean m_dummiesAreAttachments = RDKIT_DEFAULTS.getDummiesAreAttachments();

    @Layout(DrawingOptionsSection.class)
    @Widget(title = "Draw radicals", description = "Draws radical dots.")
    @Persist(configKey = "include_radicals")
    boolean m_includeRadicals = RDKIT_DEFAULTS.getIncludeRadicals();

    @Layout(DrawingOptionsSection.class)
    @Widget(title = "No atom labels", description = "If set, no atom labels will be drawn.")
    @Persist(configKey = "no_atom_labels")
    boolean m_noAtomLabels = RDKIT_DEFAULTS.getNoAtomLabels();

    @Layout(DrawingOptionsSection.class)
    @Widget(title = "Include isotope labels", description = "Includes information about isotopes.")
    @Persist(configKey = "isotope_labels")
    boolean m_isotopeLabels = RDKIT_DEFAULTS.getIsotopeLabels();

    @Layout(DrawingOptionsSection.class)
    @Widget(title = "Include isotope labels on dummies",
        description = "Includes information about isotopes on dummy atoms.")
    @Persist(configKey = "dummy_isotope_labels")
    boolean m_dummyIsotopeLabels = RDKIT_DEFAULTS.getDummyIsotopeLabels();

    @Layout(DrawingOptionsSection.class)
    @Widget(title = "Comic mode",
        description = "Activates \"comic mode\" - this simulates hand-drawn structures.")
    @Persist(configKey = "comic_mode")
    boolean m_comicMode = RDKIT_DEFAULTS.getComicMode();

    @Layout(DrawingOptionsSection.class)
    @Widget(title = "Black & white mode",
        description = "Activates black and white mode. No atom/bond coloring is used.")
    @Persist(configKey = "bw_mode")
    boolean m_bwMode;

    @Layout(DrawingOptionsSection.class)
    @Widget(title = "Single color wedge bonds", description = "Draws wedge bonds as single color.")
    @Persist(configKey = "single_color_wedge_bonds")
    boolean m_singleColorWedgeBonds = RDKIT_DEFAULTS.getSingleColourWedgeBonds();

    @Layout(DrawingOptionsSection.class)
    @Widget(title = "Add stereo annotations",
        description = "Adds R/S labels and enhanced stereo annotations to the drawing.")
    @Persist(configKey = "add_stereo_annotation")
    boolean m_addStereoAnnotation = true; // RDKit default is a bad one; matches old dialog behavior

    @Layout(DrawingOptionsSection.class)
    @Widget(title = "Include chiral flag",
        description = "Adds the chiral flag if set on the molecule.")
    @Persist(configKey = "include_chiral_flag")
    boolean m_includeChiralFlag = RDKIT_DEFAULTS.getIncludeChiralFlagLabel();

    @Layout(DrawingOptionsSection.class)
    @Widget(title = "Use simplified stereo groups",
        description = "Simplifies the drawing of molecules where all specified chiral centers are in one stereo group.")
    @Persist(configKey = "simplified_stereo_groups")
    boolean m_simplifiedStereoGroups = RDKIT_DEFAULTS.getSimplifiedStereoGroupLabel();

    @Layout(DrawingOptionsSection.class)
    @Widget(title = "Line width for bonds",
        description = "The line width to be used when drawing bonds.")
    @Persist(configKey = "bond_line_width_double")
    @Migration(BondLineWidthMigration.class)
    @NumberInputWidget(minValidation = BondLineWidthMin.class, maxValidation = BondLineWidthMax.class)
    double m_bondLineWidthDouble = RDKIT_DEFAULTS.getBondLineWidth();

    static final class BondLineWidthMin extends MinValidation {
    	
        @Override
        public double getMin() {
            return 0.05;
        }
        
    }
    
    static final class BondLineWidthMax extends MaxValidation {
    	
        @Override
        public double getMax() {
            return 100.0;
        }
        
    }

    static final class BondLineWidthMigration implements NodeParametersMigration<Double> {

        private static final String LEGACY_INT_KEY = "bond_line_width";

        @Override
        public List<ConfigMigration<Double>> getConfigMigrations() {
            return List.of(ConfigMigration //
                .builder(BondLineWidthMigration::loadLegacyInt) //
                .withMatcher(BondLineWidthMigration::hasLegacyInt) //
                .withDeprecatedConfigPath(LEGACY_INT_KEY) //
                .build());
        }

        private static boolean hasLegacyInt(final NodeSettingsRO settings) {
            try {
                return settings.containsKey(LEGACY_INT_KEY) && settings.getInt(LEGACY_INT_KEY) >= 0;
            } catch (InvalidSettingsException e) {
                return false;
            }
        }

        private static Double loadLegacyInt(final NodeSettingsRO settings) throws InvalidSettingsException {
            return (double) settings.getInt(LEGACY_INT_KEY);
        }
        
    }

    @Layout(DrawingOptionsSection.class)
    @Widget(title = "Min font size", description = "The minimum font size.")
    @Persist(configKey = "min_font_size")
    @NumberInputWidget(minValidation = IsNonNegativeValidation.class, maxValidation = FontSizeMax.class)
    int m_minFontSize = RDKIT_DEFAULTS.getMinFontSize();

    @Layout(DrawingOptionsSection.class)
    @Widget(title = "Max font size", description = "The maximum font size.")
    @Persist(configKey = "max_font_size")
    @NumberInputWidget(minValidation = IsNonNegativeValidation.class, maxValidation = FontSizeMax.class)
    int m_maxFontSize = RDKIT_DEFAULTS.getMaxFontSize();

    static final class FontSizeMax extends MaxValidation {
    	
        @Override
        public double getMax() {
            return 100.0;
        }
        
    }

    @Layout(DrawingOptionsSection.class)
    @Widget(title = "Annotation font scale",
        description = """
            The annotation font size is set as a fraction of the symbol font size. \
            This determines that fraction.
            """)
    @Persist(configKey = "annotation_font_scale")
    @NumberInputWidget(minValidation = IsNonNegativeValidation.class, maxValidation = AnnotationFontScaleMax.class, 
    	stepSize = 0.05)
    double m_annotationFontScale = RDKIT_DEFAULTS.getAnnotationFontScale();

    static final class AnnotationFontScaleMax extends MaxValidation {
    	
        @Override
        public double getMax() {
            return 2.0;
        }
        
    }
    
    static final class MolColumnAutoGuessProvider extends RDKitMoleculeColumnAutoGuessProvider {
    	
        protected MolColumnAutoGuessProvider() {
            super(MolColumnRef.class, 0);
        }
        
    }
    
}
