package org.rdkit.knime.util;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.webui.node.dialog.defaultdialog.DefaultNodeSettings.DefaultNodeSettingsContext;
import org.knime.core.webui.node.dialog.defaultdialog.widget.ColumnChoicesProvider;
import org.rdkit.knime.types.RDKitMolValue;

public final class RDKitMoleculeColumnChoicesProvider implements ColumnChoicesProvider {
    @Override
    public DataColumnSpec[] columnChoices(final DefaultNodeSettingsContext context) {
		return context
				.getDataTableSpec(0).map(spec -> spec.stream()
						.filter(c -> c.getType().isCompatible(RDKitMolValue.class)).toArray(DataColumnSpec[]::new))
				.orElse(new DataColumnSpec[0]);
    }
}