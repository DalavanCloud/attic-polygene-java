package org.qi4j.library.framework.caching;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import org.qi4j.composite.AppliesTo;
import org.qi4j.composite.Invocation;
import org.qi4j.composite.SideEffectFor;
import org.qi4j.composite.ThisCompositeAs;

/**
 * Cache result of @Cached method calls.
 */
@AppliesTo( Cached.class )
public class CacheInvocationResultSideEffect
    implements InvocationHandler
{
    @ThisCompositeAs private InvocationCache cache;
    @Invocation private Method method;
    @SideEffectFor private InvocationHandler next;

    public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable
    {
        // Get value
        // if an exception is thrown, don't do anything
        Object result = next.invoke( proxy, method, args );
        if( result == null )
        {
            result = Void.TYPE;
        }

        String cacheName = method.getName();
        if( args != null )
        {
            cacheName += Arrays.asList( args );
        }
        Object oldResult = cache.getCachedValue( cacheName );
        if( oldResult == null || !oldResult.equals( result ) )
        {
            cache.setCachedValue( cacheName, result );
        }
        return result;
    }
}
