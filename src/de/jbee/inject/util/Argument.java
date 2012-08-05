package de.jbee.inject.util;

import de.jbee.inject.Dependency;
import de.jbee.inject.DependencyResolver;
import de.jbee.inject.Instance;
import de.jbee.inject.Parameter;
import de.jbee.inject.Type;

public final class Argument<T>
		implements Parameter<T> {

	public static <T> Argument<T> argumentFor( Parameter<T> parameter ) {
		if ( parameter instanceof Argument<?> ) {
			return (Argument<T>) parameter;
		}
		if ( parameter instanceof Instance<?> ) {
			return new Argument<T>( (Instance<T>) parameter );
		}
		if ( parameter instanceof Type<?> ) {
			return new Argument<T>( Instance.anyOf( (Type<T>) parameter ) );
		}
		if ( parameter instanceof Dependency<?> ) {
			return new Argument<T>( (Dependency<T>) parameter );
		}
		throw new IllegalArgumentException( "Unknown parameter type:" + parameter );
	}

	public static <T> Parameter<T> constant( Type<T> type, T constant ) {
		return new Argument<T>( constant, type, null, null );
	}

	public static <S, T extends S> Parameter<S> asType( Type<S> supertype, Parameter<T> parameter ) {
		Argument<T> arg = argumentFor( parameter );
		return new Argument<S>( arg.constant, supertype, arg.instance, arg.dependency );
	}

	private final T constant;
	private final Type<? super T> asType;
	private final Instance<? extends T> instance;
	private final Dependency<? extends T> dependency;

	private Argument( Instance<T> instance ) {
		this( null, instance.getType(), instance, null );
	}

	private Argument( Dependency<T> dependency ) {
		this( null, dependency.getType(), null, dependency );
	}

	private Argument( T constant, Type<? super T> asType, Instance<? extends T> instance,
			Dependency<? extends T> dependency ) {
		super();
		this.constant = constant;
		this.asType = asType;
		this.instance = instance;
		this.dependency = dependency;
	}

	public T resolve( Dependency<?> constructed, DependencyResolver context ) {
		if ( constant != null ) {
			return constant;
		}
		if ( dependency != null ) {
			return context.resolve( dependency );
		}
		return context.resolve( constructed.instanced( instance ) );
	}

	@Override
	public boolean isAssignableTo( Type<?> type ) {
		return asType.isAssignableTo( type );
	}

}
