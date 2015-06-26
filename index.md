---
layout: default
---

# silk

_java dependency injection through code_

### What happened?

As always the detailed documentation provided earlier got outdated when the code
evolved. The following is not much but it will be accurate at least. 
If something is not covered just ask [me](http://jbee.github.io).

### What it is
Dependency management through code. 
No XML. No Annotations. No code dependencies in the wrong direction. 
Application code is written as if there is no DI library. 
Indeed, this is the goal: wiring up the application had become so simple that no 
sophisticated library is needed to aid it. 
Silk will make sophisticated wiring easier until it is no longer needed and
gracefully disappears as simplicity emerges.

### Why it came to be
The classic struggle: How hard could it be to write something better than 
the so called "mature" crap mostly used in the enterprise? 
As often it turned out: not that complicated.
More importantly: while the major players are all addictive frameworks the silk
library is intended as a substitute that in the end gets rid of itself.
Confused? It's just saying: [Small is beautiful](http://www.infoq.com/presentations/small-large-systems).
We don't need a library for that. So I'm not using it.

### How it is used
* Checkout the [sources](https://github.com/jbee/silk) or pick a 
[release](https://github.com/jbee/silk/releases),
* use `ant jar` or `ant release` to build a jar and add it to the classpath,
* declare some bindings in a module
{%highlight java %}
class RobotLegsProblem extends BinderModule {

	@Override
	protected void declare() {
		construct( Robot.class );
		//...
		bind( left, Leg.class ).toConstructor();
		bind( right, Leg.class ).toConstructor();
		injectingInto( left, Leg.class ).bind( Foot.class ).to( left, Foot.class );
		injectingInto( right, Leg.class ).bind( Foot.class ).to( right, Foot.class );
	}
}
{%endhighlight%}
* bootstrap the container
{%highlight java %}
Injector injector = Bootstrap.injector( RobotLegsProblem.class );
{%endhighlight%}
* resolve the root instance
{%highlight java %}
Robot robot = injector.resolve( dependency( Robot.class ) ); 
{%endhighlight%}

### How to learn it
The [tests](https://github.com/jbee/silk/tree/master/src/test/se/jbee/inject/bind)
for the binding API do illustrate what can be done and how to do it. 
It's all ordinary code. Read it.

Not very handy but it doesn't lie. A tour could be to look at

* [most basic](https://github.com/jbee/silk/blob/master/src/test/se/jbee/inject/bind/TestInstanceBinds.java)
* [constants](https://github.com/jbee/silk/blob/master/src/test/se/jbee/inject/bind/TestConstantBinds.java)
* [primitives](https://github.com/jbee/silk/blob/master/src/test/se/jbee/inject/bind/TestPrimitiveBinds.java)
* [arrays](https://github.com/jbee/silk/blob/master/src/test/se/jbee/inject/bind/TestElementBinds.java)
* [primitive arrays](https://github.com/jbee/silk/blob/master/src/test/se/jbee/inject/bind/TestPrimitiveArrayBinds.java)
* [collections](https://github.com/jbee/silk/blob/master/src/test/se/jbee/inject/bind/TestCollectionBinds.java)
* [parent dependent binds](https://github.com/jbee/silk/blob/master/src/test/se/jbee/inject/bind/TestParentTargetBinds.java)
* [package dependent binds](https://github.com/jbee/silk/blob/master/src/test/se/jbee/inject/bind/TestPackageLocalisedBinds.java)
* [auto-binds](https://github.com/jbee/silk/blob/master/src/test/se/jbee/inject/bind/TestAutobindBinds.java)
* [multi-binds](https://github.com/jbee/silk/blob/master/src/test/se/jbee/inject/bind/TestMultibindBinds.java)
* [require-provide](https://github.com/jbee/silk/blob/master/src/test/se/jbee/inject/bind/TestRequiredProvidedBinds.java)
* [loggers](https://github.com/jbee/silk/blob/master/src/test/se/jbee/inject/bind/TestLoggerBinds.java)
* [parameter hinting 1](https://github.com/jbee/silk/blob/master/src/test/se/jbee/inject/bind/TestSpecificImplementationBinds.java)
* [robots legs problem](https://github.com/jbee/silk/blob/master/src/test/se/jbee/inject/bind/TestRobotLegsProblemBinds.java)
* [parameter hinting 2](https://github.com/jbee/silk/blob/master/src/test/se/jbee/inject/bind/TestConstructorParameterBinds.java)
* [providers](https://github.com/jbee/silk/blob/master/src/test/se/jbee/inject/bind/TestProviderBinds.java)
* [plug-ins](https://github.com/jbee/silk/blob/master/src/test/se/jbee/inject/bind/TestPluginBinds.java)

Looking at the other tests in the same folder will also be useful. 
There is more to discover. Most likely silk allows to do what you want.
Otherwise just ask [me](http://jbee.github.io) for help.

### How it works
At the core is a quite simple instance container. 
It consists of a list of injectrons sorted by precision (most precise first) 
that is assembled from bindings during initialization and then never changes again.
Each injectron is a factory like typed source of instances. When a dependency is resolved
the first injectorn that matches the requested dependency is asked to yield
the instance for it. That's it. 

#### Utilities
Most of the library is just a utility build around the container.
The fluent API is a utility to describe bindings (assemblies) to create a
container from. Modules and bundles are utilities to organize and assemble
sets of such bindings to allow configuration and customization during the
bootstrapping of a container. 
Suppliers, repositories and scopes are abstractions to create and manage 
instances and their life-cycles. Providers and factories make this even more
convenient. Finally actions utilize several features to build a 
input-processing-output abstraction onto methods.

#### Precision?
Both instance providers (injectrons) as well as dependencies (instance requirements)
do described through data what they require/provide. 
Such descriptions can be more or less specific or precise.
A wild-card type is less precise than a fully specified one. 
A named instance more precise than a unnamed one, 
one specifically meant for a particular package or parent instance is again more 
precise than one that doesn't specify these. 
There is no particular logic other than: the more we know about when/where
something applies the more specific or precise it is. 
All in all it is fairly intuitive - one just needs to remember the sequence 
in which properties are compared: type, name, hierarchy, package.

