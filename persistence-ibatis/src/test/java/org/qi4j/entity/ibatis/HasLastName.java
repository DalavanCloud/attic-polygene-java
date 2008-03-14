package org.qi4j.entity.ibatis;

import org.qi4j.composite.Mixins;
import org.qi4j.library.framework.entity.PropertyMixin;
import org.qi4j.property.Property;

/**
 * @author edward.yakop@gmail.com
 * @since 0.1.0
 */
@Mixins( PropertyMixin.class )
public interface HasLastName
{
    Property<String> lastName();
}