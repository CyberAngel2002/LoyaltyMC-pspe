LoyaltyMC-pspe
===============

### warning this is a bleeding edge source and optimsed for my server

Support 1.13, 1.12, 1.11, 1.10, 1.9, 1.8, 1.7, 1.6, 1.5, 1.4.7 and bedrock 1.11, 1.10 ,1.9 clients on Spigot 1.13.2

WIP branch for adding MCPE clients support

Important notes:
* Only latest version of this plugin is supported
* This plugin can't be reloaded or loaded not at server startup
* This plugin doesn't work with netty native transport

Known issues:
* [Anything that is not latest] Items in creative mode may not work as expected, or may not work at all
* Online Mode set to true will generate errors or completely stop your server


Known wontfix issues:
* [1.12 and earlier] Chests are seen as enderchests. (Intentional to prevent rendering glitches!)  
[Check this plugin if you want different behaviour](https://www.spigotmc.org/resources/protocolsupportchestfix.59314/)
* [1.8 and earlier] Thrown potion texture is invalid
* [1.8 and earlier] Can't control vehicle
* [1.6 and earlier] Stats are not sent
* [1.4.7] Server shows up as "incompatible" in the server list, impossible to fix due to the lack of an way to verify the client version 
