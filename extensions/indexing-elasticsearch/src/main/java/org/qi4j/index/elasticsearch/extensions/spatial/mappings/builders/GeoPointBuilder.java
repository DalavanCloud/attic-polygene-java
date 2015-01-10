/*
 * Copyright (c) 2014, Jiri Jetmar. All Rights Reserved.
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

package org.qi4j.index.elasticsearch.extensions.spatial.mappings.builders;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.qi4j.index.elasticsearch.ElasticSearchSupport;
import org.qi4j.index.elasticsearch.extensions.spatial.configuration.SpatialConfiguration;

import java.io.IOException;
import java.util.StringTokenizer;

/**
 * Created by jj on 19.12.14.
 */
public class GeoPointBuilder extends AbstractBuilder {

    public GeoPointBuilder(ElasticSearchSupport support) {
        this.support = support;
    }

    private  String createGeoPointMapping(String field) throws IOException
    {
        XContentBuilder qi4jRootType = XContentFactory.jsonBuilder().startObject().startObject(support.entitiesType());
        StringTokenizer t1 = new StringTokenizer(field, ".");
        String propertyLevel1;
        while (t1.hasMoreTokens())
        {
            propertyLevel1 = t1.nextToken();
            qi4jRootType.startObject("properties").startObject(propertyLevel1);
        }
                qi4jRootType
                        .field("type", "geo_point") // geo_point
                        .field("precision", SpatialConfiguration.getIndexerPrecision(support.spatialConfiguration()))
                        .field("lat_lon", true);

        StringTokenizer t2 = new StringTokenizer(field, ".");
        while (t2.hasMoreTokens())
        {
            t2.nextToken();
            qi4jRootType.endObject();
        }

        qi4jRootType.endObject().endObject().endObject();
        return qi4jRootType.string();
    }

    public boolean create(String field) {
        try {
            return put(field, createGeoPointMapping(field));
        } catch (Exception _ex) {
            _ex.printStackTrace();
        }
        return false;
    }

}
