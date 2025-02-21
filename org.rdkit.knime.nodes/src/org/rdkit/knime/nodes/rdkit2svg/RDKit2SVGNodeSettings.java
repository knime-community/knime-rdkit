package org.rdkit.knime.nodes.rdkit2svg;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.webui.node.dialog.defaultdialog.DefaultNodeSettings;
import org.knime.core.webui.node.dialog.defaultdialog.layout.Section;
import org.knime.core.webui.node.dialog.defaultdialog.layout.After;
import org.knime.core.webui.node.dialog.defaultdialog.layout.Layout;
import org.knime.core.webui.node.dialog.defaultdialog.widget.Widget;
import org.knime.core.webui.node.dialog.defaultdialog.widget.ValueSwitchWidget;
import org.knime.core.webui.node.dialog.defaultdialog.widget.NumberInputWidget;
import org.knime.core.webui.node.dialog.defaultdialog.widget.TextInputWidget;
import org.knime.core.webui.node.dialog.defaultdialog.widget.updates.BooleanReference;
import org.knime.core.webui.node.dialog.defaultdialog.widget.updates.ValueReference;
import org.knime.core.webui.node.dialog.defaultdialog.persistence.api.Persist;
import org.knime.core.webui.node.dialog.defaultdialog.widget.Label;
import org.knime.core.webui.node.dialog.defaultdialog.widget.updates.Effect;
import org.knime.core.webui.node.dialog.defaultdialog.widget.updates.Effect.EffectType;

/**
 * Settings for the RDKit2SVG node using the webui framework.
 * 
 * @author Marc Lehner, KNIME GmbH, Zurich, Switzerland
 */
public final class RDKit2SVGNodeSettings implements DefaultNodeSettings {

    ///////////////////////////////////////////////////////////////////////////
    // FIRST SECTION: "Options"
    ///////////////////////////////////////////////////////////////////////////
    @Section(title = "Options")
    interface OptionsSection {
    }

    @Persist(configKey = "input_column")
    @Widget(
        title = "Input Molecule Column",
        description = 
            "Select the input column that contains RDKit molecules. " +
            "It must be compatible with SMILES, SDF, or RDKit Mol data.",
        advanced = false
    )
    @TextInputWidget
    @Layout(OptionsSection.class)
    public String m_inputColumn = "";

    @Persist(configKey = "new_column_name")
    @Widget(
        title = "Output SVG Column Name",
        description = 
            "Choose a name for the newly generated SVG column.",
        advanced = false
    )
    @TextInputWidget
    @Layout(OptionsSection.class)
    public String m_newColumnName = "";

    @Persist(configKey = "remove_source_columns")
    @Widget(
        title = "Remove Source Column",
        description =
            "If checked, the original source molecule column will be removed " +
            "from the output table.",
        advanced = false
    )
    @ValueReference(RemoveSourceColumnsRef.class)

    @Layout(OptionsSection.class)
    public boolean m_removeSourceColumns = false;

    interface RemoveSourceColumnsRef extends BooleanReference {}

    ///////////////////////////////////////////////////////////////////////////
    // SECOND SECTION: "Drawing Options"
    ///////////////////////////////////////////////////////////////////////////
    @Section(title = "Drawing Options", advanced = true)
    @After(OptionsSection.class)
    interface DrawingOptionsSection {
    }

    @Persist(configKey = "clear_background")
    @Widget(
        title = "Clear Background",
        description =
            "If enabled, the molecule is drawn on a white background. " +
            "Otherwise, the background may remain transparent."
    )
    @Layout(DrawingOptionsSection.class)
    public boolean m_clearBackground = false;

    @Persist(configKey = "dummies_are_attachments")
    @Widget(
        title = "Treat Dummies as Attachment Points",
        description =
            "If enabled, dummy atoms are drawn as attachment points instead of placeholders."
    )
    @Layout(DrawingOptionsSection.class)
    public boolean m_dummiesAreAttachments = false;

    @Persist(configKey = "add_atom_indices")
    @Widget(
        title = "Show Atom Indices",
        description =
            "If enabled, atom indices will be drawn next to each atom for identification."
    )
    @Layout(DrawingOptionsSection.class)
    public boolean m_addAtomIndices = false;

    @Persist(configKey = "add_bond_indices")
    @Widget(
        title = "Show Bond Indices",
        description =
            "If enabled, bond indices will be drawn next to each bond for identification."
    )
    @Layout(DrawingOptionsSection.class)
    public boolean m_addBondIndices = false;

    @Persist(configKey = "isotope_labels")
    @Widget(
        title = "Include Isotope Labels",
        description =
            "If enabled, isotope labels will be included in the drawing if present in the molecule."
    )
    @Layout(DrawingOptionsSection.class)
    public boolean m_isotopeLabels = false;

    @Persist(configKey = "dummy_isotope_labels")
    @Widget(
        title = "Isotope Labels on Dummy Atoms",
        description =
            "If enabled, isotope labels will also be rendered on dummy atoms if they have isotopes set."
    )
    @Layout(DrawingOptionsSection.class)
    public boolean m_dummyIsotopeLabels = false;

    @Persist(configKey = "add_stereo_annotation")
    @Widget(
        title = "Add Stereo Annotations",
        description =
            "If enabled, stereochemistry annotations will be printed on the drawing."
    )
    @Layout(DrawingOptionsSection.class)
    public boolean m_addStereoAnnotation = true;

    @Persist(configKey = "center_before_drawing")
    @Widget(
        title = "Center Molecule Before Drawing",
        description =
            "If enabled, the molecule is centered on the drawing canvas before being drawn."
    )
    @Layout(DrawingOptionsSection.class)
    public boolean m_centerBeforeDrawing = true;

    @Persist(configKey = "prepare_before_drawing")
    @Widget(
        title = "Prepare Molecule Before Drawing",
        description =
            "If enabled, the molecule is prepared (e.g. kekulized, layout updated) before being drawn."
    )
    @Layout(DrawingOptionsSection.class)
    public boolean m_prepareBeforeDrawing = true;

    @Persist(configKey = "explicit_methyl")
    @Widget(
        title = "Draw Explicit Methyl Groups",
        description =
            "If enabled, the depiction will show methyl groups explicitly " +
            "rather than condensing or abbreviating them."
    )
    @Layout(DrawingOptionsSection.class)
    public boolean m_explicitMethyl = false;

    @Persist(configKey = "include_radicals")
    @Widget(
        title = "Draw Radicals",
        description =
            "If enabled, radical sites in the molecule will be rendered in the output drawing."
    )
    @Layout(DrawingOptionsSection.class)
    public boolean m_includeRadicals = false;

    @Persist(configKey = "comic_mode")
    @Widget(
        title = "Comic Mode",
        description =
            "If enabled, the depiction uses a more 'comic book' style rendering, " +
            "e.g., with wiggled bond lines, etc."
    )
    @Layout(DrawingOptionsSection.class)
    public boolean m_comicMode = false;

    @Persist(configKey = "bw_mode")
    @Widget(
        title = "Use B/W Palette",
        description =
            "If enabled, a black/white palette is used for rendering atoms, " +
            "which is useful for grayscale depiction."
	)
    @Layout(DrawingOptionsSection.class)
    public boolean m_bwMode = false;

    @Persist(configKey = "no_atom_labels")
    @Widget(
        title = "No Atom Labels",
        description =
            "If enabled, atoms will be drawn without their element labels, " +
            "which is sometimes desirable for cluttered depictions."
    )
    @Layout(DrawingOptionsSection.class)
    public boolean m_noAtomLabels = false;

    @Persist(configKey = "include_chiral_flag")
    @Widget(
        title = "Include Chiral Flag",
        description =
            "If enabled, indicates the presence of a chiral flag next to the drawing of the molecule."
    )
    @Layout(DrawingOptionsSection.class)
    public boolean m_includeChiralFlag = false;

    @Persist(configKey = "simplified_stereo_groups")
    @Widget(
        title = "Use Simplified Stereo Groups",
        description =
            "If enabled, stereochemical groups (e.g. Sgroup data) are depicted in a simplified manner, " +
            "rather than fully expanded."
    )
    @Layout(DrawingOptionsSection.class)
    public boolean m_simplifiedStereoGroups = false;

    @Persist(configKey = "single_color_wedge_bonds")
    @Widget(
        title = "Single Color Wedge Bonds",
        description =
            "If enabled, wedge bonds are drawn using a single color, which can reduce confusion in color-coded structures."
    )
    @Layout(DrawingOptionsSection.class)
    public boolean m_singleColorWedgeBonds = false;

    @Persist(configKey = "bond_line_width_double")
    @Widget(
        title = "Bond Line Width",
        description =
            "The thickness of the bonds in the drawing. " +
            "A small double-precision value controlling bond line width in drawing units."
    )
    @NumberInputWidget
    @Layout(DrawingOptionsSection.class)
    public double m_bondLineWidth = 1.0; // default is read from RDKit2SVGNodeDialog's param or RDKitMol defaults

    @Persist(configKey = "min_font_size")
    @Widget(
        title = "Minimum Font Size",
        description =
            "The smallest allowable font size for annotations in the drawing. " +
            "Used to prevent text from becoming too illegible."
    )
    @NumberInputWidget
    @Layout(DrawingOptionsSection.class)
    public int m_minFontSize = 6;

    @Persist(configKey = "max_font_size")
    @Widget(
        title = "Maximum Font Size",
        description =
            "The largest allowable font size for annotations in the drawing."
    )
    @NumberInputWidget
    @Layout(DrawingOptionsSection.class)
    public int m_maxFontSize = 40;

    @Persist(configKey = "annotation_font_scale")
    @Widget(
        title = "Annotation Font Scale",
        description =
            "A scaling factor controlling the size of stereo or other textual annotation in the depiction. " +
            "E.g. 0.05 means the annotation text is 5% the standard font size."
    )
    @NumberInputWidget
    @Layout(DrawingOptionsSection.class)
    public double m_annotationFontScale = 0.1;

}