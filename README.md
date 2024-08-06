# Proxy-Portals
A minecraft proxy plugin that allows players to transport between servers with nether portals.

## Installation
This plugin works on the backend. No need for a proxy plugin. Simply place the jar file in the plugins folder of any server you want to use portals in, and restart your server. (Note, this plugin does not need to be on the server the player is teleporting to! If you have one central lobby server that has all of the portals, that is the only server that needs the plugin.)

## Usage
To register an ordinary into a server switching portal, use the command `/register-server *server*`, in which you replace `*server*` with the name of the server you want the portal to transport you to. The `*server*` parameter must be the exact same that it appears in your `velocity.toml` (you can find a list of available servers and their exact names with the `/server` command). You will be promped to walk into the portal you want to register. After you do so, the entire portal (size and shape doesn't matter) will be registered to redirect the player to that server.
**Multiple portals can be registered to one server**!

## Building
I use gradle to build this plugin. Ensure you have Java 21 or older and run `./gradle` build on Linux & MacOS, or `.\gradle.bat` build on Windows. Given that the build succeeds, the jar will spawn under the `ProxyPortalsPaper/build/libs/` directory.
