---
layout : userguide
title : Bindings
---
# Bindings

<table class='toc'>
	<tr><th>#<a href="#basics">Basics</a></th><td>The basics of <code>bind(...)</code> and <code>to(...)</code>.</td></tr>
	<tr><th>#<a href="#array">Array-Bindings</a></th><td>The role of array types and how to bind arrays and elements.</td></tr>
	<tr><th>#<a href="#multi">Multi-Bindings</a></th><td>How to create collections of instances with the same base-type.</td></tr>
	<tr><th>#<a href="#star">Star-Bindings</a></th><td>Wildcard binds and what they are good for.</td></tr>
	<tr><th>#<a href="#auto">Auto-Bindings</a></th><td>Binding all implemented types of a class automatically.</td></tr>
	<tr><th>#<a href="#require">Plugin-Bindings</a></th><td>Loosely connect service consumer (<code>require</code>) with a service provider (<code>provide</code>) like plug-ins.</td></tr>
	<tr><th>#<a href="#targeting">Targeting</a></th><td>How to make binds just apply in some (special) cases.</td></tr>
	<tr><th>#<a href="#binder">Binder API</a></th><td>General concepts of the fluent binder interface.</td></tr>
	<tr><th>#<a href="#inspect">Semi-Automate Bindings<br/>or Instance Factory-Bindings</a></th><td>How to advice the binder to inspect classes and derive binds from it semi-automatically.</td></tr>
	<tr><th>#<a href="#config">Config-Bindings</a></th><td>How to use bindings to dynamically inject different instances dependent on a configuration value that changes during runtime.</td></tr>
</table>

## A\. Concept
In Silk bindings are just a convenient way to create `Suppliable`s (we don't need to know that those are now). 
Think of them as the minimal data record needed to describe a _injectable resource_. All different kinds of bindings described below are themselves just a 
convenient way to produce calls to the `Bindings#add`-method that we see below:

{% highlight java %}
public interface Bindings {

	<T> void add( Resource<T> resource, Supplier<? extends T> supplier, Scope scope, Source source );
}
{% endhighlight %}

So a `Binding` is noting more than a 4-tuple consisting of

- `Resource` : Describes when (for which `Dependency`) to use the `Supplier`
- `Supplier` : Describes how to supply (resolve/create) an instance
- `Scope` : Describes how created instances are managed (how often new instances are created and how many exist beside each other)
- `Source` : Describes the origin of the binding (the `Module` that defined it) and the binding-type (_multi_, _auto_, ...)

Again: all the different bindings described below just result in such a 4-tuple. Some will create 
more than one but in a simplified inspection it is correct to expect a 1:1 correlation.

### <a id="precision"></a> Precision of Bindings
When the `Injector` has to decide which binding should be taken there might be several known that fit the case.
If so the binding that is most specific will be used preferential to satisfy a `Dependency`. 
But what does it mean to be more or most specific or precise ? Generally speaking it could be said, 
that the binding is more narrow, hence applies to less cases, is less general then others. 
Such a bind is therefore more specific or precise. The following order is chosen to satisfy a _naturl_ expectation when using it: 

When matching a `Resource` to a `Dependency` this is the sequence of importance (strongest to weakest): 

1. `Instance` (injected) is more precise
	1. `Type` is more precise (e.g. `ArrayList` is more precise than `List`)
	2. `Name` is more precise (e.g. `foo` is more precise than `fo*`)
3. `Target` is more precise
	1. `Packages` are more precise (smaller set)
	2. `Instance` (receiver) is more precise (has again `Type` and `Name`)

Following this rules there can be just **one** binding that is the one chosen because bindings have to be unambiguous. 
This means: 

**There cannot be 2 bindings with the same precision matching the same case!**

There can be as many quite similar bindings as long as non of them describe exactly the same `Resource`,
that is the same `Instance` within the same `Target` (see below how targeting makes binds more specific). 
If Silk encounters 2 or more bindings having the exact same `Resource` this will raise an exception 
during the bootstrapping process.

## <a id="basics"></a>1\. Interface-Implementation Bindings
The most common and simple form of binding describes pairs of what implementation should be used for a particular interface. Here is an example:

{% highlight java %}
protected void declare() {
	bind( Interface.class ).to ( Implementation.class );
}
{% endhighlight %}

This tells Silk to use `Implementation` whenever a dependency of type `Interface` is encountered. 
In case we don't have such an abstraction we could also advice to directly construct our `Implementation`.

{% highlight java %}
protected void declare() {
	construct( Implementation.class );
}
{% endhighlight %}

What is just a shortcut for this declaration:

{% highlight java %}
protected void declare() {
	bind( Implementation.class ).toConstructor();
}
{% endhighlight %}

### <a id="parameters"></a>Adding Constructor Parameter Descriptions
Usually we don't (need to) provide any additional information how Silk should construct an instance.
The bootstrapper will ask the `Inspector` to pick the `Constructor` to use for our `Implementation`.
All dependencies needed to instanciate it are resolved in the same way.

In some cases we want a special instance injected into the constructor selected. Therefore we can add additional 
_hints_ so Silk knows what to do. Generally we have 2 options to achieve this:

- Use targeting (see <a href="#targeting">end of page</a>)
- Add `Parameter`s to the `toConstructor` call

Lets have a look at the latter solution. Assuming our `Implementation` needs a instance of `Foo` as constructor
argument and a special instance of `Foo` should be used. This can be described like below:

{% highlight java %}
protected void declare() {
	Instance<Foo> foo = instance( named( "specialOne" ), raw( Foo.class ) );
	bind( Implementation.class ).toConstructor( foo );
}
{% endhighlight %}

We refer to this special `Foo` instance using a `Instance` descriptor that we pass as argument to `toConstructor` call.
Now Silk knows what should be resolved here. To make this run some module has to bind `Foo` so it becomes constructable as well (e.g. by using `construct( foo )`). 

**Note that the `Parameter`s (like an `Instance`) do not describe the signature of the constructor!**

`Parameter`s are used to help out when needed. All dependencies that are resolved correctly without such a _hint_ don't have to be added.
Therefore sequence also don't play a role. It just becomes important in case a `Parameter` given matches more than one constructor dependency.
Than the first parameter is used for the first matching, the second for the second and so on.

See also `TestConstructorParameterBinds` and `TestDependencyParameterBinds` for more detailed examples.

### The `to`-Clause / Suppliers
We have already seen a few different `to`-clauses in the above examples. The `TypedBinder` has about a dozen of different ones.
The most common are:

- Binding to a `Class` or `Instance` (link or create to an instance of it)
- Binding to a `Constructor` (use it to construct an instance)
- Binding to a `Factory` (a simplified `Supplier` interface)  
- Binding to a constant value `T` (just uses the supplied instance)

All of them are just convenient methods to construct a appropriate `Supplier`. 
The utility class `SuppliedBy` contains most of the implementations used in the `Binder`.
Unlike other frameworks in Silk all `bind`-declarations need a `to`-clause in order to construct a binding.
As seen above the method `construct` is a shortcut to the common case that we want to bind to the constructor that is found automatically. 
  

## <a id="array"></a>2\. Array-Bindings
There is a build in support in Silk that when binding something to a class `X` the 1-dimensional array type `X[]` is implicitly defined as well.
The array contains all known `X` instances. There can be more than one because for type `X` `multibind`s (see below) have been used 
or those are instances with different precision so they normally apply to different injections.

So as soon as we do a usual bind like this: 
{% highlight java %}
protected void declare() {
	bind( Integer.class ).to(42);
}
{% endhighlight %}
We can inject `Integer` as well as `Integer[]`. With just this above definition alone the injected array would be equal to `new Integer[]{42}`.
This behaviour works on the raw-type. Currently there is no support for something similar with generic types. 
But it is possible to bind directly to a generic array type and define all the members. Of cause this can also be used to explicitly
define what elements should be contained in e.g. `Integer[]`
{% highlight java %}
protected void declare() {
	bind( Integer[].class ).toElements( 4, 2 );
}
{% endhighlight %}
When this binding can be used (matches) to inject a dependency of type `Integer[]` just the defined 
elements `4` and `2` will be contained independent of any of the binds done for `Integer`. Together 
with targeting (<a href="#targeting">see below</a>) this can be used to _replace_ the meaning of `Integer[]` just for special
situations. 

Together with _bridge_-`Supplier`s (see `BuildinBundle`) the build in array-support can be used to also easily get `List`s,
`Set`s or your custom collection type injected containing all the elements from the binds on the 
element type or a special bind for the array type. Note that even though you receive the collection
as `List` (or something else) the binds are defined like above binding to the array type. This keeps
them independently from the different forms of collection asked for by different receivers. We don't
need to explicitly define a bind to `List<Integer>` (even though this would work as well what can be 
used to replace a general behaviour in some cases).

## <a id="multi"></a>3\. Multi-Bindings
A `multibind` is used to create collections of instances that should be injected together. All of them are bound to the same super-type they all have in common.

When using a `multibind` we explicitly want multiple instances to coexist for the same resource because the resource models a collection of something.
Notice that you cannot combine this with any other kind of `bind` because there we want all resources to be unambiguous.

Let's assume we have a service that depends on some settings that can emerge from different sources. Each represented by a instance of type `Settings`. One is reading the from a `File` and the other one receives them remotely.  
{% highlight java %}
protected void declare() {
	multibind(Settings.class).to(FileSettings.class);
	multibind(Settings.class).to(RemoteSettings.class);
}
{% endhighlight %}

The consumer of this `Settings` can asked for them by simply asking for the array type `Settings[]`. This is a build in functionality that you get out of the box. For all types you declared any bind (also normal ones) you can ask for the array type as well. 
{% highlight java %}
class SettingDependentService {
  SettingDependentService(Settings[] settings) { /*...*/  }
}
{% endhighlight %}

Often the usage of `List` is preferred. I don't see a real advantage using them here but in case we want them anyway we once `install` a `Bundle` that makes `List`s available in general. Now we can use `List<X>` as a replacement for `X[]` everywhere. 
{% highlight java %}
protected void bootstrap() {
	install( BuildinBundle.LIST );
}
{% endhighlight %}
Like `List`s here we could also add `Set<X>`s as equivalent to `X[]`. This kind of _bridges_ from an array-type to any kind of collection can be added easily to Silk with a _one-liner_ by extending the `ArrayBridgeSupplier`.  


## <a id="star"></a>4\. Star-Bindings
Like `autobind` a `starbind` is just a convenient method on top of the `bind` API.
It models a _wildcard_-binding and is nothing more than the below (from `Binder`):

{% highlight java %}
public <T> TypedBinder<T> starbind( Class<T> type ) {
	return bind( Instance.anyOf( Type.raw( type ) ) );
}
{% endhighlight %}

Wildcard bindings are used when a bind should explicitly match any dependency that
has the raw `type` given (also any `Name` is matched).

This is used e.g. to _forward_ all dependencies on `Provider` to the `Supplier`
that builds the _bridge_ to the type provided by it. The declaration in the `ProviderBridgeModule` looks like this: 

{% highlight java %}
protected void declare() {
	per( DEPENDENCY ).starbind( Provider.class ).to( SuppliedBy.PROVIDER_BRIDGE );
}
{% endhighlight %}

Even though the `starbind` doesn't provide that much functionality it is a slightly
different intention to use one in comparison to a `bind`. Beside the `Provider` 
all other kinds of _bridges_ like `List`s and `Set`s, `ServiceMethod`s or your custom service
make use of one `starbind` declaration.


## <a id="auto"></a>5\. Auto-Bindings
A `autobind` is a convenient helper method on top of normal `bind`s. 
It is nothing else than binding the same class (from `to`-clause) to all its 
super-types and -interfaces (that would be the `bind`-clause).

Think of a design where one class (instance of that) serves as implementation 
for a hole suite of interfaces. A general setting service could look like this: 
{% highlight java %}
class SettingService 
	implements UserSettings, AdminSettings, EmailSettings { /* ... */ } 
{% endhighlight %}
Now a single `autobind` is done:
{% highlight java %}
protected void declare() {
	autobind( SettingService.class ).toConstructor();
}
{% endhighlight %}
What is the same as doing:
{% highlight java %}
protected void declare() {
	bind( SettingService.class ).toConstructor();
	bind( UserSettings.class).to( SettingService.class );
	bind( AdminSettings.class).to( SettingService.class );
	bind( EmailSettings.class).to( SettingService.class );
}
{% endhighlight %}
In some cases this could become quite messy. Therefore the `autobind` is 
provided even though it might be useful quite seldom. 

Note that `autobind` will also bind parameterized super-types like `Compareable<Integer>` for `Integer`.


## <a id="require"></a>6\. Plugin-Bindings
One task of dependency injection is to _connect_ loosely coupled modules of an application (**OBS!** this is not about Silk `Module`s!). 
Typically an application has _core_ modules and _extension_ modules. Often there are different _extension_ modules but neither should the core module _know_ what module will be connected nor should the _extension_ _know_ where exactly it should incorporate within the _core_ module(s). 
For the (technical) reason that a service _provider_ and a service _consumer_ need to have a common _contract_ those get coupled together.
Plugin-bindings allow to keep this coupling as loose as possible. 

A resource can take one of two roles:

- As a service provider it communicates that it is able to `provide` functionality.
- As a service consumer is communicates that it `require`s functionality (a certain `Type`).

The connection is than draws automatically by the `Injector` context. 

{% highlight java %}
protected void declare() { // in the core module
	require( SomeServiceInterface.class );
}

protected void declare() { // in a plugin module
	provide( SomeServicImpl.class );
}

class SomeServicImpl implements SomeServiceInterface, ...
{% endhighlight %}
The two modules do not have to _know_ each other or specific specifically how they want to _connect_.
In this example there is just the possibility that the `SomeServicImpl` is used for the required `SomeServiceInterface` but a providing class could be used for many different `require`ments. 

### How _collisions_ are resolved
While plugin-bindings allow for a loose form of coupling there is a good chance that there is more than one matching _provider_ for a _requirement_ because the overall composition of the application could be seen as chaotic (modules don't know about each other). 

Therefore **none** of the _providers_ is used (for that particular requirement), as soon as there are two or more that could serve an implementation. They are said to be _nullified_. There are two options to handle this:

- Not define a default, what results in an eager bootstrapping exception because of a missing requirement. Thereby the developer can directly notice that a particular configuration clashes and resolve it by redefining composition or add more precise binds to one of the involved modules. 
- A default bind can be made using `asDefault()` so that whenever _providers_ collide for a _requirement_ the default is used.  
{% highlight java %}
protected void declare() { // in the requiring module
	require( SomeServiceInterface.class );	
	asDefault().bind( SomeServiceInterface.class ).to( DefaultServicImpl.class );
}
{% endhighlight %}
<small style="color:red">(this does not work correctly in v0.5 since `DEFAULT` was considered less precise than `PROVIDED` - it can be worked around by using `autobind` instead)</small>

In general it is important to think of the <a href="#precision">precision of binds</a> when working with plugin-bindings. 
All explicit made `bind`s will replace a possible `provide`d implementation. So providing will never cause trouble for usual binds but at the same time it also cannot replace them.

**OBS!** Plugin-bindings should just be used to connect the most abstract level of application modules. They are not a autowiring supertool. Used like that they most likely will cause a lot of headache and pain as soon as the application has grown to a certain size. 

### Where is the difference to autobind ?
In contrast to an <a href="#auto">autobind</a> all bindings resulting from a `provide` that are not needed to fulfill a requirement will be removed **before** the `Injector` is created. So they do not clutter the context with unneeded bindings, while `autobind` would keep at least those type-bindings that are not bound by a more precise binding. 

## <a id="targeting"></a>7\. Targeting Bindings
All of the above forms of bindings can be made more specific by describing the `Target` it should be used for.
This narrows the cases in which it matches a dependency but also makes it more suitable. 

The `Target` describes the `Instance` in which something should be injected as well as the `Packages` in which the binding is valid. 
Both techniques can be used to make a binding very precise and therefore more significant in all cases where it matches.

### Localised Bindings
Bindings can be made more specific by narrowing down the packages where the binding applies. The less 
packages are member of a `Packages` set the more precise it becomes. The most specific `Packages` set
therefore contains just a single specific `Package`.

The below bindings taken from `TestPackageLocalisedBinds` show the different possibilities:
{% highlight java linenos %}
protected void declare() {
	bind( String.class ).to( "default" );
	inPackageOf( TestPackageLocalisedBinds.class ).bind( String.class ).to( "test" );
	inSubPackagesOf( Object.class ).bind( String.class ).to( "java-lang.*" );
	inPackageAndSubPackagesOf( List.class ).bind( String.class ).to( "java-util.*" );
}
{% endhighlight %}

Description from the most to the least specific binding given above:

- Line `3` is the most local bind that is just valid in the package of the test class
- Line `4` matches in all sub-packages of `java.lang` 
- Line `5` matches in `java.util` and all its sub-packages
- Line `2` has no localisation

All bindings above can coexist because of their different precision. When supplying a `Dependency` 
the most specific in that respective case will be used. This are different ones in different cases.

### Parent Dependent Bindings
Beside the `Packages` where a binding can be used it is also possible to target a specific `Instance`. 
The _Robot-Legs-Problem_ (shown in `TestRobotLegsProblemBinds`) is a good example of where such instance specific bindings can be useful.

A robot has 2 legs, both quite similar but the left `Leg` should get the left `Foot` while the right `Leg` gets another instance - the right `Foot`.
In Silk this could be described like so:
{% highlight java %}
protected void declare() {
	bind( left, Leg.class ).toConstructor();
	bind( right, Leg.class ).toConstructor();
	injectingInto( left, Leg.class ).bind( Foot.class ).to( left, Foot.class );
	injectingInto( right, Leg.class ).bind( Foot.class ).to( right, Foot.class );
}
{% endhighlight %}		
Here `left` and `right` are instances of `Name`s that are used to point out what `Instance` is meant. 
We use the same names for the `Leg`s and `Feet`s but could also have chosen different ones like `leftFoot`, `leftLeg` and so forth.

The `injectingInto`-method describes what parent we are targeting with the `bind` that is called subsequently.  

### Instance Of Dependent Bindings
When using parent dependent bindings (see above) it is also possible to use a interface as the parent's `Instance`-`Type`. 
Of cause a parent cannot be a interface since there are no instances of the interface types itself. 
So when using interface types it means the parent should be a _instance of_ the interface provided.

{% highlight java %}
protected void declare() {
	injectingInto( Serializable.class ).bind( Integer.class ).to( 42 );
}
{% endhighlight %}	

Using the above declaration a dependency of type `Integer` would be injected as `42` into all parents that are `Serializable`.
This is a very powerful tool. Use it with caution! Otherwise it becomes hard to foresee what will be injected. 
Especially don't follow this example and use it with something that generic and wide spread like the `Serializable` type.

See also `TestTargetedBinds` for another example.   

## <a id="binder"></a>8\. Fluent-Binder-Interface
In Silk almost everything tends to be immutable. The binder itself is another example. 
When using the fluent interface we can always assign an intermediate partial declaration to a 
variable and finish it with different ends. Here is a simple example:

{% highlight java %}
protected void declare() {
	TargetedBinder binder = injectingInto( Serializable.class );
	binder.bind( Foo.class ).to( FOO_IN_SERIALIZABLE );
	binder.bind( Bar.class ).to( BAR_IN_SERIALIZABLE );
}
{% endhighlight %}
 
 The above declaration will create 2 bindings both targeted to `Serializable`. 
 They cannot effect each other and thereby cause conflicts or unforeseen behaviour.
 The complete `Binder`-API can be used like this. So we could have assigned after a `bind` as well.
 The result would just be another type of binder (here bound to a type already).
 
 To benefit from this feature the sequence in which the parts of a binding can or has to be defined is slightly different from other frameworks with similar fluent binder APIs. It is:
 
 	[(per) Scope] -> [Target.Instance] -> [(in) Target.Packages] -> (bind) Instance -> (to) Supplier 

### Defaults
 The first 3 parts in square brackets are optional and can be left out. This are the defaults use:
 
 - `Scope` : Singleton per application
 - `Target` (`Instance` + `Packages`) : _any_ instance in all packages  
 
 
## <a id="inspect"></a>9\. Semi-Automate Bindings through Inspections
Silk encourages making bindings explicit using the different options described above. But in some
cases (most of all allowing to shift progressive from an annotation based DI framework) it might make 
sense to partially automate the binding process. But there have to be limits otherwise automation 
turns into pain of not knowing or understanding what is going on. So this still has to be very clear
to the programmer, just that there is a tool making a _obvious connection_ more convenient to express 
when using the fluent binder interface.   

The `Inspector` interface is the abstraction for such a tool. It allows to use custom strategies that 
_derive_ the constructor to use or the methods that act as _factory methods_ (like guice's `@Provides` annotation).

The `Inspect` `Inspector` that comes with Silk offers different common ways to select 
`Constructor`s or `Method`s that can be combined using a own fluent interface. In general inspection
 binds have the form
 
    bind(Inspector).in(Class...)
   
where `Class` can be one or more implementation classes.

{% highlight java %}
protected void declare() {
	bind( all().methods().annotatedWith( Factory.class ) ).in( Implementor.class );
}
{% endhighlight %}
See `TestInspectorBinds` for more examples.

Through this it is possible to integrate _legacy_ code from a previously used framework changing it
smoothly bit by bit while new code can benefit from Silk directly. So a migration from Guice or Spring 
does not require doing all changes up-front. 

### Bind Methods as Instance Factories
When DI is also used to create  _throw away_ instances from factory methods the `Inspector` can be used
to bind usual methods as such factories. 
{% highlight java %}
protected void declare() {
	per( Scoped.INJECTION ).bind( all().methods() ).in( Implementor.class );
}
{% endhighlight %}
It is important to use the `INJECTION` scope so that the method will be called for each injection. 

## <a id="config"></a>10\. Config-Bindings
### What is Configuration Dependent Implementation Injection (CDI²)
Dependency Injection is used to decouple a _function consumer_ from the _function provider_. 
Such a _function_ is a _behavioural_ part of an application. Often applications allow to 
change its _behaviour_ during runtime by changing a setting or configuration. 
As a consequence the _behaviour_ needs to change, in OOP this means the implementation class needs to
be different one. CDI² is a way to solve this problem without writing error prone `if-else` code but
leave this task also to the DI tool.

### How does it work ?
Actually CDI² isn't as complicated as it seams in the first observation. It is a _linked_ binding
where the _link_ (usually from a super-type to a subtype or specific instance) is not hard wired but
instead resolved differently depending on the current value of the `Configuring` instance. 
So it is a indirection step like a _switch_ to multiple actual implementations controlled by whatever
other bound instance's actual value. To determine the current instance a `Name` is derived from the
current control value using a `Naming` strategy. 

The example uses the enum `ValidationStrength` as _controlling_ instance (but it could be any `Instance` 
used, there is just a simpler short-cut for enums). With the `on`-method it is described what 
implementation is associated with a specific value. Through the `onOther` method a default can be 
given that is used for e.g. `null` values or in case no bind has been made for the current actual value.

{% highlight java %}
protected void declare() {
	configbind( Validator.class ).on( ValidationStrength.PERMISSIVE ).to( Permissive.class );
	configbind( Validator.class ).on( ValidationStrength.STRICT ).to( Strict.class );
	configbind( Validator.class ).onOther( ValidationStrength.class ).to( Permissive.class );
}
{% endhighlight %}

When using configuration dependent binds it is important to understand that the `Instance` used
as the configuring one (here it is the default instance of `ValidationStrength` class) also has to be bound.
To allow it to change during runtime it is required to **not use the singleton** `Scope` for the
controlling instance. As an example the bind makes use of the `Inspector` to bind against a 
method defined in the `Configuration` object. 

{% highlight java %}
protected void declare() {
	per( Scoped.INJECTION ).bind( methodsReturn( raw( ValidationStrength.class ) ) ).in(Configuration.class );
}
{% endhighlight %}

Now whenever the method returning the current `ValidationStrength` value is changed and a injection of the
`Validator` occurs the implementation associated with the current value will be injected. To use this at
a place when no further injection takes place a `Provider<Validator>` allows to access the changing instance as well.  

See `TestConfigurationDependentBinds` for different complete examples including the use of a `Provider` to 
directly work with a dynamically changing implementation controlled by another bound value.


 <a class='next' href="scopes.html"><span class="fa fa-chevron-right"></span>Scopes</a>
