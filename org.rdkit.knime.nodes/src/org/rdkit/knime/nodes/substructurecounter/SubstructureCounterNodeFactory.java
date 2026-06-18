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
 * <code>NodeFactory</code> for the RDKit based "RDKitSubstructureCounter" Node.
 * 
 * @author Swarnaprava Singh
 * @author Manuel Schwarze
 * @author Jannik Semperowitsch, KNIME GmbH, Konstanz, Germany
 * @author AI Migration Pipeline v1.2
 */
public class SubstructureCounterNodeFactory extends NodeFactory<SubstructureCounterNodeModel> implements NodeDialogFactory {

	/**
	 * Creates a model for the RDKitSubstructureCounter functionality
	 * of the RDKit library. The model is derived from the
	 * abstract class AbstractRDKitNodeModel, which provides
	 * common base functionality for RDKit nodes.
	 * {@inheritDoc}
	 *
	 * @see org.rdkit.knime.nodes.AbstractRDKitNodeModel
	 */
	@Override
	public SubstructureCounterNodeModel createNodeModel() {
		return new SubstructureCounterNodeModel();
	}

	/**
	 * This node does not have any views.
	 * 
	 * @return Always null.
	 */
	@Override
	public NodeView<SubstructureCounterNodeModel> createNodeView(
			final int viewIndex,
			final SubstructureCounterNodeModel nodeModel) {
		if (viewIndex != 0) {
			throw new IllegalArgumentException();
		}
		return new RDKitInteractiveView<SubstructureCounterNodeModel>(nodeModel, false, 0);
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

    private static final String NODE_NAME = "RDKit Substructure Counter";
    private static final String NODE_ICON = "default.png";
    private static final String SHORT_DESCRIPTION = """
            Calculates the number of times a query molecule is present in the input molecule.
            """;
    private static final String FULL_DESCRIPTION = """
            This node is used to calculate the number of times a particular query molecule is present in an
                input molecule. The number of times the query molecule present in the input molecule can be
                repeated. It is possible to choose the unique number of times the query molecule is present in the
                input molecule.
            """;
    private static final List<PortDescription> INPUT_PORTS = List.of(
            fixedPort("Input molecule table", """
                Table with input molecule column.
                """),
            fixedPort("Query molecule table", """
                Table with input query column.
                """)
    );
    private static final List<PortDescription> OUTPUT_PORTS = List.of(
            fixedPort("Number of substructures", """
                Input molecule table with one additional column for each row in the query molecule table. The new
                columns added contain the number of times that a query molecule is found in the molecule.
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
        return new DefaultNodeDialog(SettingsType.MODEL, SubstructureCounterNodeParameters.class);
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
            SubstructureCounterNodeParameters.class, //
            VIEWS, //
            NodeType.Manipulator, //
            List.of(), //
            null //
        );
    }
    
}

