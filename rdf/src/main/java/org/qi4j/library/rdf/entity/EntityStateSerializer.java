/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.qi4j.library.rdf.entity;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.openrdf.model.BNode;
import org.openrdf.model.Graph;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.GraphImpl;
import org.openrdf.model.vocabulary.XMLSchema;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.library.rdf.Rdfs;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityType;
import org.qi4j.spi.entity.ManyAssociationState;
import org.qi4j.spi.entity.association.AssociationType;
import org.qi4j.spi.entity.association.ManyAssociationType;
import org.qi4j.spi.entity.helpers.json.JSONException;
import org.qi4j.spi.entity.helpers.json.JSONWriter;
import org.qi4j.spi.property.PropertyType;
import org.qi4j.spi.value.AbstractStringType;
import org.qi4j.spi.value.ValueType;

/**
 * JAVADOC
 */
public class EntityStateSerializer
{

    private Map<String, URI> dataTypes = new HashMap<String, URI>();

    public EntityStateSerializer()
    {
        // TODO A ton more types need to be added here
        dataTypes.put( String.class.getName(), XMLSchema.STRING );
        dataTypes.put( Integer.class.getName(), XMLSchema.INT );
        dataTypes.put( Boolean.class.getName(), XMLSchema.BOOLEAN );
        dataTypes.put( Byte.class.getName(), XMLSchema.BYTE );
        dataTypes.put( BigDecimal.class.getName(), XMLSchema.DECIMAL );
        dataTypes.put( Double.class.getName(), XMLSchema.DOUBLE );
        dataTypes.put( Long.class.getName(), XMLSchema.LONG );
        dataTypes.put( Short.class.getName(), XMLSchema.SHORT );
        dataTypes.put( Date.class.getName(), XMLSchema.DATETIME );
    }

    public URI createEntityURI( ValueFactory valueFactory, EntityReference identity )
    {
        return valueFactory.createURI( identity.toURI() );
    }

    public Iterable<Statement> serialize( final EntityState entityState )
    {
        return serialize( entityState, true );
    }

    public Iterable<Statement> serialize( final EntityState entityState,
                                          final boolean includeNonQueryable )
    {
        Graph graph = new GraphImpl();
        serialize( entityState, includeNonQueryable, graph );
        return graph;
    }

    public void serialize( final EntityState entityState,
                           final boolean includeNonQueryable,
                           final Graph graph )
    {
        ValueFactory values = graph.getValueFactory();
        EntityReference identity = entityState.identity();
        URI entityUri = createEntityURI( values, identity );

        EntityType entityType = entityState.entityType();
        graph.add( entityUri, Rdfs.TYPE, values.createURI( entityType.uri() ) );

        serializeProperties( entityState,
                             graph,
                             entityUri,
                             entityType,
                             includeNonQueryable
        );

        serializeAssociations( entityState, graph, entityUri, entityType.associations(), includeNonQueryable );
        serializeManyAssociations( entityState, graph, entityUri, entityType.manyAssociations(), includeNonQueryable );
    }

    private void serializeProperties( final EntityState entityState,
                                      final Graph graph,
                                      final Resource subject,
                                      final EntityType entityType,
                                      final boolean includeNonQueryable )
    {
        final ValueFactory valueFactory = graph.getValueFactory();

        try
        {
            // Properties
            StringWriter stringWriter = new StringWriter();
            JSONWriter jsonStringer = new JSONWriter( stringWriter );
            jsonStringer.array();
            for( PropertyType propertyType : entityType.properties() )
            {
                if( !( includeNonQueryable || propertyType.queryable() ) )
                {
                    continue; // Skip non-queryable
                }
                Object property = entityState.getProperty( propertyType.qualifiedName() );
                if( property == null || property.equals( "null" ) )
                {
                    continue; // Skip properties with null values
                }
                ValueType valueType = propertyType.type();

                String stringProperty = null;
                valueType.toJSON( property, jsonStringer );
                stringProperty = stringWriter.toString();


                if( valueType instanceof AbstractStringType ) // Remove "" around strings
                {
                    stringProperty = stringProperty.substring( 2, stringProperty.length() - 1 );
                } else
                {
                    stringProperty = stringProperty.substring( 1 );
                }

                URI predicate = valueFactory.createURI( propertyType.qualifiedName().toURI() );

                final Literal object = valueFactory.createLiteral( stringProperty );
                graph.add( subject, predicate, object );
                stringWriter.getBuffer().setLength( 0 );
            }

        }
        catch( JSONException e )
        {
            throw new IllegalArgumentException( "Could not JSON serialize value", e );
        }
    }

    private void serializeAssociations( final EntityState entityState,
                                        final Graph graph, URI entityUri,
                                        final Iterable<AssociationType> associations,
                                        final boolean includeNonQueryable )
    {
        ValueFactory values = graph.getValueFactory();

        // Associations
        for( AssociationType associationType : associations )
        {
            if( !( includeNonQueryable || associationType.queryable() ) )
            {
                continue; // Skip non-queryable
            }

            EntityReference associatedId = entityState.getAssociation( associationType.qualifiedName() );
            if( associatedId != null )
            {
                URI assocURI = values.createURI( associationType.qualifiedName().toURI() );
                URI assocEntityURI = values.createURI( associatedId.toURI() );
                graph.add( entityUri, assocURI, assocEntityURI );
            }
        }
    }

    private void serializeManyAssociations( final EntityState entityState,
                                            final Graph graph,
                                            final URI entityUri,
                                            final Iterable<ManyAssociationType> associations,
                                            final boolean includeNonQueryable )
    {
        ValueFactory values = graph.getValueFactory();

        // Many-Associations
        for( ManyAssociationType associationType : associations )
        {
            if( !( includeNonQueryable || associationType.queryable() ) )
            {
                continue; // Skip non-queryable
            }

            BNode collection = values.createBNode();
            graph.add( entityUri, values.createURI( associationType.qualifiedName().toURI() ), collection );
            graph.add( collection, Rdfs.TYPE, Rdfs.SEQ );

            ManyAssociationState associatedIds = entityState.getManyAssociation( associationType.qualifiedName() );
            for( EntityReference associatedId : associatedIds )
            {
                URI assocEntityURI = values.createURI( associatedId.toURI() );
                graph.add( collection, Rdfs.LIST_ITEM, assocEntityURI );
            }
        }
    }
}
