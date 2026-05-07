/*
 * ------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright (C)2013-2023
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
package org.rdkit.knime.nodes.smilesheaders;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;
import org.rdkit.knime.nodes.RDKitInteractiveView;
import org.knime.core.webui.node.dialog.NodeDialog;
import org.knime.core.webui.node.dialog.NodeDialogFactory;
import org.knime.core.webui.node.dialog.NodeDialogManager;
import org.knime.core.webui.node.dialog.SettingsType;
import org.knime.core.webui.node.dialog.defaultdialog.DefaultNodeDialog;
import org.knime.core.node.NodeDescription;
import org.knime.node.impl.description.DefaultNodeDescriptionUtil;
import org.knime.node.impl.description.PortDescription;
import java.util.List;
import org.knime.node.impl.description.ViewDescription;
import static org.knime.node.impl.description.PortDescription.fixedPort;

/**
 * <code>NodeFactory</code> for the RDKit based "RDKitSmilesHeaders" Node.
 * 
 * @author Manuel Schwarze
 
 * @author Jannik Semperowitsch, KNIME GmbH, Konstanz, Germany
 
 * @author AI Migration Pipeline v1.2
 */
public class RDKitSmilesHeadersNodeFactory extends NodeFactory<RDKitSmilesHeadersNodeModel> implements NodeDialogFactory {

	/**
	 * Creates a model for the RDKitSmilesHeaders functionality
	 * of the RDKit library. The model is derived from the
	 * abstract class AbstractRDKitNodeModel, which provides
	 * common base functionality for RDKit nodes.
	 * {@inheritDoc}
	 *
	 * @see org.rdkit.knime.nodes.AbstractRDKitNodeModel
	 */
	@Override
	public RDKitSmilesHeadersNodeModel createNodeModel() {
		return new RDKitSmilesHeadersNodeModel();
	}

	/**
	 * This node has an RDKit Interactive View attached.
	 * 
	 * @return RDKit Interactive View.
	 */
	@Override
	public NodeView<RDKitSmilesHeadersNodeModel> createNodeView(
			final int viewIndex,
			final RDKitSmilesHeadersNodeModel nodeModel) {
		return new RDKitInteractiveView<RDKitSmilesHeadersNodeModel>(nodeModel, false, 0);
	}

	/**
	 * This node does not have any views.
	 * 
	 * @return Always 0.
	 */
	@Override
	public int getNrNodeViews() {
		return 1;
	}

	/**
	 * This node possesses a configuration dialog.
	 * 
	 * @return Always true.
	 */
	@Override
	public boolean hasDialog() {
		return true;
	}

    private static final String NODE_NAME = "RDKit SMILES Headers";
    private static final String NODE_ICON = "default.png";
    private static final String SHORT_DESCRIPTION = """
            Sets, changes and retrieves SMILES structures of RDKit table header properties, which can be made
                visible using the RDKit Interactive View.
            """;
    private static final String FULL_DESCRIPTION = """
            Sets, changes and/or retrieves SMILES structures of RDKit table header properties, which can be made
                visible using the RDKit Interactive View. <br /><br /> Beside the column title a KNIME column can
                have properties assigned. Use this node to manipulate the property that controls the SMILES value
                used by the RDKit Interactive View to render a molecule in the column header. This node will not
                change any of the column titles / names. <br /><br /> This node takes a Data Table as first input -
                the data of the table remains unchanged, only the column properties get affected by the change. You
                may either use existing column titles of the Data Table as SMILES properties (if they are valid
                SMILES), or read SMILES values from a SMILES Definition Table that is connected to the second input
                port. <br /><br /> If a SMILES Definition Table is connected it needs to contain at least two
                columns: One defines a SMILES value and the other the name of the target column that should receive
                the SMILES value in the header property. The node will now walk through all columns of the Data
                Table and checks, if a SMILES value is available in the SMILES Definition Table for that column. If
                the name of the column is found in the SMILES Definition Table it will set the associated SMILES
                value as a column property, and the SMILES will show up in subsequent RDKit Interactive Views. If
                there is no SMILES value but an empty cell defined for that column, the SMILES property gets removed
                instead. <br /><br /> The Data Table is available with the manipulated column properties on the
                first output port. The second output table contains information about all columns that have SMILES
                values as properties attached. It can be used to manipulate these structure (e.g. canonicalizing
                them) and to reassign them afterwards again to make these changes visible in the column headers. <br
                /><br /> Optionally, it is possible to remove all SMILES information from the column properties
                before applying any new SMILES values to them.
            """;
    private static final List<PortDescription> INPUT_PORTS = List.of(
            fixedPort("Data table", """
                Data Table whose header properties (not titles) shall be manipulated.
                """),
            fixedPort("SMILES definition table", """
                Defines target column names of the Data Table and SMILES values to be set as column header properties.
                """)
    );
    private static final List<PortDescription> OUTPUT_PORTS = List.of(
            fixedPort("Result data table", """
                Same table as input data table, but with potentially changed column header properties.
                """),
            fixedPort("Result SMILES definition table", """
                Table with all column header SMILES properties in Result Data Table.
                """)
    );
    private static final List<ViewDescription> VIEWS = List.of(
            new ViewDescription("Interactive table view", """
                Displays the data in a table view. Has the capability to show chemical structures in the headers of the
                substructure count columns.
                """)
    );

    @Override
    public NodeDialogPane createNodeDialogPane() {
        return NodeDialogManager.createLegacyFlowVariableNodeDialog(createNodeDialog());
    }

    @Override
    public NodeDialog createNodeDialog() {
        return new DefaultNodeDialog(SettingsType.MODEL, RDKitSmilesHeadersNodeParameters.class);
    }

    @Override
    public NodeDescription createNodeDescription() {
        return DefaultNodeDescriptionUtil.createNodeDescription( //
            NODE_NAME, //
            NODE_ICON, //
            INPUT_PORTS, //
            OUTPUT_PORTS, //
            SHORT_DESCRIPTION, //
            FULL_DESCRIPTION, //
            List.of(), //
            RDKitSmilesHeadersNodeParameters.class, //
            VIEWS, //
            NodeType.Manipulator, //
            List.of(), //
            null //
        );
    }
    
}

