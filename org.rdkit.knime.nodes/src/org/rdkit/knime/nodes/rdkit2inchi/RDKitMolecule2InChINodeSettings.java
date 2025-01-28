package org.rdkit.knime.nodes.rdkit2inchi;

import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentLabel;
import org.knime.core.node.defaultnodesettings.DialogComponentMultiLineString;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.webui.node.dialog.defaultdialog.DefaultNodeSettings;
import org.knime.core.webui.node.dialog.defaultdialog.layout.Section;
import org.knime.core.webui.node.dialog.defaultdialog.layout.After;
import org.knime.core.webui.node.dialog.defaultdialog.layout.Layout;
import org.knime.core.webui.node.dialog.defaultdialog.persistence.field.Persist;
import org.knime.core.webui.node.dialog.defaultdialog.widget.Label;
import org.knime.core.webui.node.dialog.defaultdialog.widget.ValueSwitchWidget;
import org.knime.core.webui.node.dialog.defaultdialog.widget.Widget;
import org.knime.core.webui.node.dialog.defaultdialog.widget.updates.PredicateProvider.PredicateInitializer;
import org.knime.core.webui.node.dialog.defaultdialog.widget.updates.Reference;
import org.rdkit.knime.util.RDKitMoleculeColumnChoicesProvider;
import org.knime.core.webui.node.dialog.defaultdialog.widget.updates.Predicate;
import org.knime.core.webui.node.dialog.defaultdialog.widget.updates.PredicateProvider;
import org.knime.core.webui.node.dialog.defaultdialog.widget.ChoicesWidget;
import org.knime.core.webui.node.dialog.defaultdialog.widget.updates.BooleanReference;
import org.knime.core.webui.node.dialog.defaultdialog.widget.updates.Effect;
import org.knime.core.webui.node.dialog.defaultdialog.widget.updates.Effect.EffectType;
import org.knime.core.data.DataTableSpec;
import org.knime.core.webui.node.dialog.defaultdialog.widget.updates.ValueReference;

/**
 * Settings for the new WebUI-based "RDKitMolecule2InChI" Node
 * using the {@link DefaultNodeSettings} approach.
 * 
 * It replaces the old style node dialog and node settings.
 *
 * @author Marc Lehner, KNIME GmbH, Zurich, Switzerland
 */
public final class RDKitMolecule2InChINodeSettings implements DefaultNodeSettings {

   /** The sections in which we group our settings. */
   @Section(title = "Input")
   interface InputSection {}

   @Section(title = "InChI Code Generation")
   @After(InputSection.class)
   interface InChICodeSection {}

   @Section(title = "InChI Key Generation")
   @After(InChICodeSection.class)
   interface InChIKeySection {}

   @Section(title = "Extra InChI Generation Information")
   @After(InChIKeySection.class)
   interface ExtraInChISection {}

   @Section(title = "Advanced", advanced = true)
   @After(ExtraInChISection.class)
   interface AdvancedSection {}

   // ------------------------------------------------------------------------
   // INPUT SECTION
   // ------------------------------------------------------------------------

   @Persist(configKey = "input_column")
   @Widget(title = "Input Mol column",
      description = "Select the input column containing the RDKit molecule data.")
   @ChoicesWidget(choices = RDKitMoleculeColumnChoicesProvider.class)
   @Layout(InputSection.class)
   String m_inputColumn;

   @Persist(configKey = "remove_source_columns")
   @Widget(title = "Remove source column",
      description = "If enabled, the source RDKit column is removed from the result table.")
   @Layout(InputSection.class)
   boolean m_removeSourceColumn = false;

   // ------------------------------------------------------------------------
   // INCHI CODE
   // ------------------------------------------------------------------------

   @Persist(configKey = "new_inchi_code_column_name")
   @Widget(title = "InChI code column name",
      description = "The name of the output column that will contain the generated InChI codes.")
   @Layout(InChICodeSection.class)
   String m_newInChICodeColumnName;

   // ------------------------------------------------------------------------
   // INCHI KEY
   // ------------------------------------------------------------------------

   // If the "generate_inchi_keys" is false, the new_inchi_key_column_name is hidden
   interface GenerateInChIKeysRef extends Reference<Boolean> {}
   static final class GenerateInChIKeysIsTrue implements PredicateProvider {
      @Override
      public Predicate init(PredicateInitializer i) {
         return i.getBoolean((Class<? extends Reference<Boolean>>) GenerateInChIKeysRef.class).isTrue();
      }
   }

   @Persist(configKey = "generate_inchi_keys")
   @Widget(title = "Generate InChI keys",
      description = "If enabled, the node will also generate InChI keys for each molecule.")
   @ValueReference(GenerateInChIKeysRef.class)
   @Layout(InChIKeySection.class)
   boolean m_generateInChIKeys = false;

   @Persist(configKey = "new_inchi_key_column_name")
   @Widget(title = "InChI key column name",
      description = "The name of the output column that will contain the generated InChI keys.")
   @Effect(type = EffectType.SHOW, predicate = GenerateInChIKeysIsTrue.class)
   @Layout(InChIKeySection.class)
   String m_newInChIKeyColumnName;

   // ------------------------------------------------------------------------
   // EXTRA
   // ------------------------------------------------------------------------

   @Persist(configKey = "new_extra_info_column_name_prefix")
   @Widget(title = "Extra info column prefix",
      description = "Prefix for columns that contain extra InChI generation information (return code, aux info, etc.)")
   @Layout(ExtraInChISection.class)
   String m_newExtraInfoPrefix;

   @Persist(configKey = "generate_return_code")
   @Widget(title = "Generate Return Code Column",
      description = "If enabled, an additional column will contain the numeric InChI return code.")
   @Layout(ExtraInChISection.class)
   boolean m_generateReturnCode;

   @Persist(configKey = "generate_aux_info")
   @Widget(title = "Generate Aux Info Column",
      description = "If enabled, an additional column will contain the InChI auxiliary information.")
   @Layout(ExtraInChISection.class)
   boolean m_generateAuxInfo;

   @Persist(configKey = "generate_message")
   @Widget(title = "Generate Message Column",
      description = "If enabled, an additional column will contain InChI library message information.")
   @Layout(ExtraInChISection.class)
   boolean m_generateMessage;

   @Persist(configKey = "generate_log")
   @Widget(title = "Generate Log Column",
      description = "If enabled, an additional column will contain InChI library log information.")
   @Layout(ExtraInChISection.class)
   boolean m_generateLog;

   // ------------------------------------------------------------------------
   // ADVANCED
   // ------------------------------------------------------------------------

   @Persist(configKey = "advanced_opions")
   @Widget(title = "Advanced InChI options",
      description = "Specify advanced InChI generation switches (e.g. /SR, /SUCF,-[fixedH], etc.).")
   @Layout(AdvancedSection.class)
   String m_advancedOptions;
}