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

package org.rdkit.knime.nodes.fingerprintreader;

import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ConfigurableNodeFactory;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeView;
import org.knime.core.node.context.NodeCreationConfiguration;
import org.knime.filehandling.core.port.FileSystemPortObject;

import java.util.Optional;
import org.knime.core.webui.node.dialog.NodeDialog;
import org.knime.core.webui.node.dialog.NodeDialogFactory;
import org.knime.core.webui.node.dialog.NodeDialogManager;
import org.knime.core.webui.node.dialog.SettingsType;
import org.knime.core.webui.node.dialog.defaultdialog.DefaultNodeDialog;
import org.knime.core.node.NodeDescription;
import org.knime.node.impl.description.DefaultNodeDescriptionUtil;
import org.knime.node.impl.description.PortDescription;
import java.util.List;
import static org.knime.node.impl.description.PortDescription.fixedPort;
import static org.knime.node.impl.description.PortDescription.dynamicPort;

/**
 * {@code NodeFactory} for the RDKit based "RDKitFingerprintReader" Node.
 * 
 * @author Manuel Schwarze
 * @author Roman Balabanov
 * @author Magnus Gohm, KNIME GmbH, Konstanz, Germany
 * @author AI Migration Pipeline v1.2
 */
public class RDKitFingerprintReaderV2NodeFactory extends ConfigurableNodeFactory<RDKitFingerprintReaderV2NodeModel> 
	implements NodeDialogFactory {

    private static final String NODE_NAME = "RDKit Fingerprint Reader";
    
    private static final String NODE_ICON = "default.png";
    
    private static final String SHORT_DESCRIPTION = """
            Node to read fingerprints from a FPS file into an output table.
            """;
    
    private static final String FULL_DESCRIPTION = """
            This node reads an FPS file with its fingerprint records into a KNIME table. The format of the .fps
            file is mentioned here: https://jcheminf.springeropen.com/articles/10.1186/1758-2946-5-S1-P36
            """;
    
    private static final List<PortDescription> INPUT_PORTS = List.of(
            dynamicPort("File System Connection", "File system connection", """
                The file system connection.
                """)
    );
    
    private static final List<PortDescription> OUTPUT_PORTS = List.of(
            fixedPort("Fingerprint table", """
                Table containing fingerprints.
                """)
    );
	
	/**
	 * The file system input ports group id.
	 */
	protected static final String INPUT_PORT_GRP_ID_FS_CONNECTION = "File System Connection";

	/**
	 * The fingerprint table output ports group id.
	 */
	protected static final String OUTPUT_PORT_GRP_ID_FINGERPRINT_TABLE = "Fingerprint table";

    /**
     * This node does not have any views.
     *
     * @return Always null.
     */
    @Override
    public NodeView<RDKitFingerprintReaderV2NodeModel> createNodeView(
            final int viewIndex,
            final RDKitFingerprintReaderV2NodeModel nodeModel) {
        return null;
    }

	@Override
	protected Optional<PortsConfigurationBuilder> createPortsConfigBuilder() {
		PortsConfigurationBuilder result = new PortsConfigurationBuilder();
		result.addOptionalInputPortGroup(INPUT_PORT_GRP_ID_FS_CONNECTION, FileSystemPortObject.TYPE);
		result.addFixedOutputPortGroup(OUTPUT_PORT_GRP_ID_FINGERPRINT_TABLE, BufferedDataTable.TYPE);

		return Optional.of(result);
	}

	/**
	 * Creates a model for the RDKitFingerprintReader functionality
	 * of the RDKit library. The model is derived from the
	 * abstract class AbstractRDKitNodeModel, which provides
	 * common base functionality for RDKit nodes.
	 * {@inheritDoc}
	 *
	 * @see org.rdkit.knime.nodes.AbstractRDKitNodeModel
	 */
	@Override
	protected RDKitFingerprintReaderV2NodeModel createNodeModel(NodeCreationConfiguration creationConfig) {
		return new RDKitFingerprintReaderV2NodeModel(creationConfig);
	}

	/**
	 * This node does not have any views.
	 * 
	 * @return Always 0.
	 */
	@Override
	protected int getNrNodeViews() {
		return 0;
	}

	/**
	 * This node possesses a configuration dialog.
	 * 
	 * @return Always true.
	 */
	@Override
	protected boolean hasDialog() {
		return true;
	}

    @Override
    public NodeDialogPane createNodeDialogPane(NodeCreationConfiguration creationConfig) {
        return NodeDialogManager.createLegacyFlowVariableNodeDialog(createNodeDialog());
    }

    @Override
    public NodeDialog createNodeDialog() {
        return new DefaultNodeDialog(SettingsType.MODEL, RDKitFingerprintReaderV2NodeParameters.class);
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
            RDKitFingerprintReaderV2NodeParameters.class, //
            null, //
            NodeType.Source, //
            List.of(), //
            null //
        );
    }

}

