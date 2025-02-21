package org.rdkit.knime.nodes.molecule2rdkit;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataValue;
import org.knime.core.data.StringValue;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.webui.node.dialog.defaultdialog.DefaultNodeSettings;
import org.knime.core.webui.node.dialog.defaultdialog.persistence.api.Persist;
import org.knime.core.webui.node.dialog.defaultdialog.persistence.api.Persistor;
import org.knime.core.webui.node.dialog.defaultdialog.persistence.persistors.settingsmodel.SettingsModelColumnNamePersistor;
import org.knime.core.webui.node.dialog.defaultdialog.persistence.persistors.settingsmodel.EnumSettingsModelStringPersistor;
import org.knime.core.webui.node.dialog.defaultdialog.widget.ChoicesProvider;
import org.knime.core.webui.node.dialog.defaultdialog.widget.ChoicesWidget;
import org.knime.core.webui.node.dialog.defaultdialog.widget.Label;
import org.knime.core.webui.node.dialog.defaultdialog.widget.TextInputWidget;
import org.knime.core.webui.node.dialog.defaultdialog.widget.ValueSwitchWidget;
import org.knime.core.webui.node.dialog.defaultdialog.widget.RadioButtonsWidget;
import org.knime.core.webui.node.dialog.defaultdialog.widget.Widget;
import org.knime.core.webui.node.dialog.defaultdialog.widget.updates.Effect;
import org.knime.core.webui.node.dialog.defaultdialog.widget.updates.Effect.EffectType;
import org.knime.core.webui.node.dialog.defaultdialog.widget.updates.Predicate;
import org.knime.core.webui.node.dialog.defaultdialog.widget.updates.PredicateProvider;
import org.knime.core.webui.node.dialog.defaultdialog.widget.updates.Reference;
import org.knime.core.webui.node.dialog.defaultdialog.widget.updates.ValueReference;
//import org.knime.core.webui.node.dialog.defaultdialog.widget.choices.ChoicesProvider;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Settings for the Molecule2RDKitConverter node using the webui framework.
 * 
 * @author Marc Lehner, KNIME GmbH, Zurich, Switzerland
 */
public class Molecule2RDKitConverterNodeSettings implements DefaultNodeSettings {

    /**
     * Enum representing the parse error handling policy (original: ParseErrorPolicy).
     */
    enum ParseErrorPolicy {
        @Label("Send error rows to second output")
        SPLIT_ROWS,

        @Label("Insert missing values")
        MISS_VAL;
    }

    /**
     * Reference interface for the parse error policy.
     */
    interface ParseErrorPolicyRef extends Reference<ParseErrorPolicy> {}

    /**
     * Predicate: Checks if the SPLIT_ROWS policy is chosen.
     */
    static final class IsSplitRows implements PredicateProvider {
        @Override
        public Predicate init(PredicateInitializer i) {
            return i.getEnum(ParseErrorPolicyRef.class).isOneOf(ParseErrorPolicy.SPLIT_ROWS);
        }
    }

    /**
     * FieldNodeSettingsPersistor for ParseErrorPolicy which was originally stored as a string.
     */
//    private static final class ParseErrorPolicyPersistor implements FieldNodeSettingsPersistor<ParseErrorPolicy> {
//        @Override
//        public ParseErrorPolicy load(NodeSettingsRO settings) throws InvalidSettingsException {
//            String val = settings.getString("separate_fails");
//            if (val != null && val.equalsIgnoreCase("SPLIT_ROWS")) {
//                return ParseErrorPolicy.SPLIT_ROWS;
//            } else {
//                return ParseErrorPolicy.MISS_VAL;
//            }
//        }
//
//        @Override
//        public void save(ParseErrorPolicy obj, NodeSettingsWO settings) {
//            settings.addString("separate_fails", obj.name());
//        }
//
//        @Override
//        public String[] getConfigKeys() {
//            return new String[]{"separate_fails"};
//        }
//    }
	static final class ParseErrorPolicyPersistor extends EnumSettingsModelStringPersistor<ParseErrorPolicy> {
		public ParseErrorPolicyPersistor() {
			super("separate_fails", ParseErrorPolicy.class);
		}
	}

    /**
     * Enum representing replace or append policy (based on original logic).
     */
    enum ReplaceAppendPolicy {
        @Label("Replace source column")
        REPLACE,
        @Label("Append as new column")
        APPEND;
    }

    interface ReplaceAppendRef extends Reference<ReplaceAppendPolicy> {}

    /**
     * Predicate: Checks if APPEND is chosen.
     */
    static final class IsAppend implements PredicateProvider {
        @Override
        public Predicate init(PredicateInitializer i) {
            return i.getEnum(ReplaceAppendRef.class).isOneOf(ReplaceAppendPolicy.APPEND);
        }
    }

    /**
     * Predicate referencing "quick and dirty" setting to show/hide aromatization/stereo.
     */
    interface QuickAndDirtyRef extends Reference<Boolean> {}
    static final class QuickAndDirtyFalse implements PredicateProvider {
        @Override
        public Predicate init(PredicateInitializer i) {
            return i.getBoolean(QuickAndDirtyRef.class).isFalse();
        }
    }

    /**
     * Predicate referencing generateCoordinates to show/hide forceGenerateCoordinates.
     */
    interface GenCoordsRef extends Reference<Boolean> {}
    static final class GenCoordsTrue implements PredicateProvider {
        @Override
        public Predicate init(PredicateInitializer i) {
            return i.getBoolean(GenCoordsRef.class).isTrue();
        }
    }

    /**
     * Predicate referencing generateErrorInformation to show/hide errorInfoColumnName.
     */
    interface ErrorInfoRef extends Reference<Boolean> {}
    static final class ErrorInfoEnabled implements PredicateProvider {
        @Override
        public Predicate init(PredicateInitializer i) {
            return i.getBoolean(ErrorInfoRef.class).isTrue();
        }
    }

    /**
     * Provider to list compatible input columns (SMILES, SMARTS, SDF).
     */
    private static final class SmilesSmartsSdfChoices implements ChoicesProvider {
        @Override
        public String[] choices(DefaultNodeSettingsContext context) {
            DataTableSpec spec = context.getDataTableSpec(0).orElse(null);
            if (spec == null) return new String[0];
            return Arrays.stream(spec.getColumnNames())
                .filter(name -> {
                    DataColumnSpec cs = spec.getColumnSpec(name);
                    // This is simplified logic: original node accepted SMILES, SMARTS, SDF.
                    // Assume StringValue covers them if adapted by knime chem types.
                    return cs.getType().isCompatible(StringValue.class);
                })
                .toArray(String[]::new);
        }
    }

    // Settings fields with annotations

    @Persist(configKey="input_column")
    @Widget(title = "Input column",
        description = "Column containing SMILES/SMARTS/SDF molecules.")
    @ChoicesWidget(choices = SmilesSmartsSdfChoices.class)
    String m_inputColumn = "";

    @Persist(configKey="treat_as_query")
    @Widget(title = "Treat as query",
        description = "If selected, treat input as query (for SMILES/SDF).")
    boolean m_treatAsQuery = false;

    @Persist(configKey="new_column_name")
    @Widget(title="New column name",
        description="The output RDKit column name.")
    String m_newColumnName = "RDKit Mol";

    @Persist(configKey = "remove_source_columns")
    @Widget(title="Remove source column",
        description="If selected, remove the original input column from output.")
    boolean m_removeSourceColumns = false;

    @Persist(configKey = "bad_rows_to_port1")//customPersistor = ParseErrorPolicyPersistor.class) //configKey = "bad_rows_to_port1"
    @Widget(title="Error handling",
        description="If parsing fails, send row to second output or insert missing?",
        advanced=false)
    @ValueSwitchWidget
    @ValueReference(ParseErrorPolicyRef.class)
    ParseErrorPolicy m_errorPolicy = ParseErrorPolicy.SPLIT_ROWS;

    @Persist(configKey = "generateErrorInfo")
    @Widget(title="Generate error information column",
        description="If selected, an error info column is added.")
    @ValueReference(ErrorInfoRef.class)
    boolean m_generateErrorInformation = false;

    @Persist(configKey = "errorInfoColumnName")
    @Widget(title="Error info column name",
        description="Name of the error info column.")
    @Effect(type=EffectType.SHOW, predicate=ErrorInfoEnabled.class)
    String m_errorInfoColumnName = "RDKit Error Info";

    @Persist(configKey = "generateCoordinates")
    @Widget(title="Generate coordinates",
        description="If selected, generate 2D coordinates if missing.")
    @ValueReference(GenCoordsRef.class)
    boolean m_generateCoordinates = false;

    @Persist(configKey = "forceGenerateCoordinates")
    @Widget(title="Force generation of coordinates",
        description="If selected, always recompute coordinates.")
    @Effect(type=EffectType.SHOW, predicate=GenCoordsTrue.class)
    boolean m_forceGenerateCoordinates = false;

    @Persist(configKey = "skip_sanitization")
    @Widget(title="Quick and dirty (skip sanitization)",
        description="If selected, no full sanitization is done.")
    @ValueReference(QuickAndDirtyRef.class)
    boolean m_quickAndDirty = false;

    @Persist(configKey = "do_aromaticity")
    @Widget(title="Aromatization",
        description="If selected and not quick/dirty, do aromatization.")
    @Effect(type=EffectType.SHOW, predicate=QuickAndDirtyFalse.class)
    boolean m_aromatization = true;

    @Persist(configKey = "do_stereochem")
    @Widget(title="Stereochemistry",
        description="If selected and not quick/dirty, assign stereochemistry.")
    @Effect(type=EffectType.SHOW, predicate=QuickAndDirtyFalse.class)
    boolean m_stereoChem = true;

    @Persist(configKey = "keepHs")
    @Widget(title="Keep explicit hydrogens",
        description="If selected, keep explicit Hs.")
    boolean m_keepHs = false;

    @Persist(configKey = "strict_parsing")
    @Widget(title="Strict parsing for SDF",
        description="If selected, use strict parsing for SDF.")
    boolean m_strictParsing = true;

    // No special logic here. The DefaultNodeSettings system handles loading/saving.

}