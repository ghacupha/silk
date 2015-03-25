---
layout : default
title: Inside the Injector
---

# Inside the Injector

This article explains how the `Injector` container conceptually works. 
This can be very helpful even for pure users of the library as the knowledge 
about the internal model allows a clearer mental image of the consequences
of bindings and their interaction. Reasoning can be based on the understanding 
of the mechanics within the container which are in fact quite simple.

> A `Injector` container is a **sorted list** of instance providers. 
> The first provider in order that fits a dependency yields the instance that is injected. 
> That is all there is to it. 

This simple mental model usually delivers the answers to most common questions like:

- How do certain bindings interact?
- Will certains bindings have the intended effect?

### Why is Instance X injected for Dependency Y ?

The question why a certain instance is injected for a certain dependency is 
easy to answer: Because the provider that provided the instance was the first 
in order that matched the requirements of a dependency. 

This leads to two questions:

1. What determines the order or instance providers within a container?
2. When or why does a instance provider match a dependency?


### Why is Provider X sorted previous to Provider Y ?



### Why does Provider X match Dependency Y ?
