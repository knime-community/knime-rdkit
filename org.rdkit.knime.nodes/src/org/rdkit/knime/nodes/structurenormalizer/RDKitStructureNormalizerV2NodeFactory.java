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
package org.rdkit.knime.nodes.structurenormalizer;

import org.knime.core.node.*;
import org.knime.core.node.context.NodeCreationConfiguration;
import org.knime.filehandling.core.port.FileSystemPortObject;

import java.util.Optional;
import org.knime.core.node.NodeDialogPane;
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
 * {@code NodeFactory} for the RDKit based "RDKitStructureNormalizer" Node.
 *
 * @author Manuel Schwarze
 * @author Roman Balabanov
 * @author Magnus Gohm, KNIME GmbH, Konstanz, Germany
 * @author AI Migration Pipeline v1.2
 */
public class RDKitStructureNormalizerV2NodeFactory extends ConfigurableNodeFactory<RDKitStructureNormalizerV2NodeModel> 
	implements NodeDialogFactory {

    /**
     * The file system ports group id.
     */
    protected static final String INPUT_PORT_GRP_ID_FS_CONNECTION = "File System Connection";

    /**
     * The input table ports group id.
     */
    protected static final String INPUT_PORT_GRP_ID_INPUT_TABLE = "Input table with Molecules";

    /**
     * The passed molecules output table ports group id.
     */
    protected static final String OUTPUT_PORT_GRP_ID_PASSED_MOLECULES = "Passed Molecules";

    /**
     * The failed molecules output table ports group id.
     */
    protected static final String OUTPUT_PORT_GRP_ID_FAILED_MOLECULES = "Failed Molecules";
    
    private static final String NODE_NAME = "RDKit Structure Normalizer";
    
    private static final String NODE_ICON = "default.png";
    
    private static final String SHORT_DESCRIPTION = """
            Checks structures and tries to normalize them, if necessary.
            """;
    
    private static final String FULL_DESCRIPTION = """
            Checks structures and tries to normalize them, if necessary. Structures that are normalized already
                will appear in the first output table. Structures, which need to be normalized will be corrected and
                also put in the first output table. Information about the normalization is made available as bit
                mask (flags) as well as warning messages. Structures, which cannot be normalized or have been
                normalized causing a certain warning flag that the user wants to treat as error are put in the
                second table ("Failed Molecules"). <br /><br /> The following flags and messages are currently used:
                <ul> <li>1 - BAD_MOLECULE, Unable to recognize a molecule (ERROR)</li> <li>2 -
                ALIAS_CONVERSION_FAILED, The atom alias conversion failed (ERROR)</li> <li>4 - TRANSFORMED,
                Structure has been transformed</li> <li>8 - FRAGMENTS_FOUND, Multiple fragments have been found</li>
                <li>16 - EITHER_WARNING, A wiggly bond has been removed</li> <li>32 - STEREO_ERROR, Stereo chemistry
                is ambiguously defined (ERROR)</li> <li>64 - DUBIOUS_STEREO_REMOVED, A stereo bond has been
                removed</li> <li>128 - ATOM_CLASH, There are two atoms or bonds are too close to each other
                (ERROR)</li> <li>256 - ATOM_CHECK_FAILED, The atom environment is not correct(ERROR)</li> <li>512 -
                SIZE_CHECK_FAILED, The molecule is too big (ERROR)</li> <li>1024 - RECHARGED, Structure has been
                recharged</li> <li>2048 - STEREO_FORCED_BAD, Structure has failed: Bad stereo chemistry (ERROR)</li>
                <li>4096 - STEREO_TRANSFORMED, Stereo chemistry has been modified</li> <li>8192 -
                TEMPLATE_TRANSFORMED, Structure has been modified using a template</li> </ul>
            """;
    
    private static final List<PortDescription> INPUT_PORTS = List.of(
            dynamicPort(INPUT_PORT_GRP_ID_FS_CONNECTION, "File system connection", """
                The file system connection.
                """),
            fixedPort("Input table with molecules", """
                Input table with SDF, SMILES or RDKit Molecules.
                """)
    );
    
    private static final List<PortDescription> OUTPUT_PORTS = List.of(
            fixedPort("Passed molecules", """
                Passed molecules and corrected structures.
                """),
            fixedPort("Failed molecules", """
                Failed molecules and error information.
                """)
    );

    @Override
    protected Optional<PortsConfigurationBuilder> createPortsConfigBuilder() {
        PortsConfigurationBuilder result = new PortsConfigurationBuilder();
        result.addOptionalInputPortGroup(INPUT_PORT_GRP_ID_FS_CONNECTION, FileSystemPortObject.TYPE);
        result.addFixedInputPortGroup(INPUT_PORT_GRP_ID_INPUT_TABLE, BufferedDataTable.TYPE);
        result.addFixedOutputPortGroup(OUTPUT_PORT_GRP_ID_PASSED_MOLECULES, BufferedDataTable.TYPE);
        result.addFixedOutputPortGroup(OUTPUT_PORT_GRP_ID_FAILED_MOLECULES, BufferedDataTable.TYPE);

        return Optional.of(result);
    }

    /**
     * Creates a model for the RDKitStructureNormalizer functionality
     * of the RDKit library. The model is derived from the
     * abstract class AbstractRDKitNodeModel, which provides
     * common base functionality for RDKit nodes.
     * {@inheritDoc}
     *
     * @see org.rdkit.knime.nodes.AbstractRDKitNodeModel
     */
    @Override
    public RDKitStructureNormalizerV2NodeModel createNodeModel(NodeCreationConfiguration creationConfig) {
        return new RDKitStructureNormalizerV2NodeModel(creationConfig);
    }
    
    /**
     * This node does not have any views.
     * 
	 * @return Always null.
     */
    @Override
    public NodeView<RDKitStructureNormalizerV2NodeModel> createNodeView(
            final int viewIndex,
            final RDKitStructureNormalizerV2NodeModel nodeModel) {
        return null;
    }
    
    /**
     * This node does not have any views.
     * 
	 * @return Always 0.
     */
    @Override
    public int getNrNodeViews() {
        return 0;
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

    @Override
    public NodeDialogPane createNodeDialogPane(NodeCreationConfiguration creationConfig) {
        return NodeDialogManager.createLegacyFlowVariableNodeDialog(createNodeDialog());
    }

    @Override
    public NodeDialog createNodeDialog() {
        return new DefaultNodeDialog(SettingsType.MODEL, RDKitStructureNormalizerV2NodeParameters.class);
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
            RDKitStructureNormalizerV2NodeParameters.class, //
            null, //
            NodeType.Manipulator, //
            List.of(), //
            null //
        );
    }

}

