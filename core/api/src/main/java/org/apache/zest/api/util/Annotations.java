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

package org.apache.zest.api.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.apache.zest.api.util.Classes.interfacesOf;
import static org.apache.zest.api.util.Classes.typeOf;

/**
 * Useful methods for handling Annotations.
 */
public final class Annotations
{
    public static Function<Type, Stream<Annotation>> ANNOTATIONS_OF =
        Classes.forTypes( type -> Arrays.stream( Classes.RAW_CLASS.apply( type ).getAnnotations() ) );
//        Classes.forTypes( type -> Iterables.iterable( Classes.RAW_CLASS.apply( type ).getAnnotations() ) );

    public static Predicate<AnnotatedElement> hasAnnotation( final Class<? extends Annotation> annotationType )
    {
        return element -> element.getAnnotation( annotationType ) != null;
    }

    public static Function<Annotation, Class<? extends Annotation>> type()
    {
        return Annotation::annotationType;
    }

    public static Predicate<Annotation> isType( final Class<? extends Annotation> annotationType )
    {
        return annotation -> annotation.annotationType().equals( annotationType );
    }

    public static <T extends Annotation> T annotationOn( Type type, Class<T> annotationType )
    {
        return annotationType.cast( Classes.RAW_CLASS.apply( type ).getAnnotation( annotationType ) );
    }

    public static Stream<Annotation> findAccessorAndTypeAnnotationsIn( AccessibleObject accessor )
    {
        return Stream.concat(
            Arrays.stream( accessor.getAnnotations() ),
            interfacesOf( typeOf( accessor ) ).flatMap( ANNOTATIONS_OF )
        );
//        return flatten( iterable( accessor.getAnnotations() ),
//                        flattenIterables( map( Annotations.ANNOTATIONS_OF, interfacesOf( typeOf( accessor ) ) ) ) );
    }
}
