# Introduction
Stonks Core is a platform-independent library that can be used outside Fabric or Bukkit. It was originally created to share code between Fabric mod and Bukkit plugin, but because there's no code that's related to Minecraft, it can be used outside the context of a block game.

## The Stonks "architecture"
!!! todo
    Add graph for "visual learners".

- At the heart of Stonks, we have **services**. Their job is to process requests from **front-ends** and response to them.
    + In code, Stonks services is just an interface with methods that returns **task info**. On front-ends side, you'll have to listen for any updates on those task, including response and errors.
- Those front-ends connect to a single service, make requests to service (we'll call them "service calls" from now on) when users requested and response to users with visual feedback, which is converted from service call results.
- There can be multiple front-ends running that connects to 1 single service.
    + Some skilled developers might be able to create "distributed system" that shares states between each service.

## Use cases
Other than making Stonks for \*insert platform name here\*, you can use Stonks in your "virtual stocks market", or if your brain is big enough: adding service interface to an actual stocks market so you can use Minecraft as market terminal.

## Okay, how do I use it?
You can take a look at [Using Stonks Core](<Using Stonks Core.md>) documentation.
