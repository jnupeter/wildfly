/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.as.test.integration.domain.extension;

import java.util.List;
import javax.xml.stream.XMLStreamException;

import org.jboss.as.controller.Extension;
import org.jboss.as.controller.ExtensionContext;
import org.jboss.as.controller.SubsystemRegistration;
import org.jboss.as.controller.parsing.ExtensionParsingContext;
import org.jboss.as.controller.parsing.ParseUtils;
import org.jboss.as.controller.persistence.SubsystemMarshallingContext;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.as.test.integration.management.extension.EmptySubsystemParser;
import org.jboss.dmr.ModelNode;
import org.jboss.staxmapper.XMLElementReader;
import org.jboss.staxmapper.XMLElementWriter;
import org.jboss.staxmapper.XMLExtendedStreamReader;
import org.jboss.staxmapper.XMLExtendedStreamWriter;

/**
 * Fake extension to use in testing extension management.
 *
 * @author Brian Stansberry (c) 2011 Red Hat Inc.
 */
public class TestExtension implements Extension {

    public static final String MODULE_NAME = "org.jboss.as.test.extension";

    private final EmptySubsystemParser parserOne = new EmptySubsystemParser("urn:jboss:test:extension:1:1.0");
    private final EmptySubsystemParser parserTwo = new EmptySubsystemParser("urn:jboss:test:extension:2:1.0");


    @Override
    public void initialize(ExtensionContext context) {
        SubsystemRegistration one = context.registerSubsystem("1", 1, 1, 1);
        one.registerXMLElementWriter(parserOne);
        ManagementResourceRegistration mrrOne = one.registerSubsystemModel(new RootResourceDefinition("1"));

        SubsystemRegistration two = context.registerSubsystem("2", 2, 2, 2);
        two.registerXMLElementWriter(parserTwo);
        ManagementResourceRegistration mrrTwo = two.registerSubsystemModel(new RootResourceDefinition("2"));

    }

    @Override
    public void initializeParsers(ExtensionParsingContext context) {
        context.setSubsystemXmlMapping("1", parserOne.getNamespace(), parserOne);
        context.setSubsystemXmlMapping("2", parserTwo.getNamespace(), parserTwo);
    }

    private static class Parser implements XMLElementReader<List<ModelNode>>, XMLElementWriter<SubsystemMarshallingContext> {

        private final String namespace;

        private Parser(String namespace) {
            this.namespace = namespace;
        }

        @Override
        public void readElement(XMLExtendedStreamReader reader, List<ModelNode> value) throws XMLStreamException {
            ParseUtils.requireNoAttributes(reader);
            ParseUtils.requireNoContent(reader);
        }

        @Override
        public void writeContent(XMLExtendedStreamWriter streamWriter, SubsystemMarshallingContext context) throws XMLStreamException {
            context.startSubsystemElement(namespace, false);
            streamWriter.writeEndElement();
        }
    }
}
