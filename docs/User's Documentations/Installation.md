# Installation
Installing Stonks is pretty easy.

=== "Fabric"
    0. Install [Fabric Loader](https://fabricmc.net/use/installer/) and [Fabric API](https://www.curseforge.com/minecraft/mc-mods/fabric-api)
    0. Download Stonks for Fabric
        + You can find latest releases [here](https://github.com/nahkd123/stonks/releases).
        + You can download development builds [here](https://github.com/nahkd123/stonks/actions/workflows/gradle.yml?query=is:success). Development builds usually contains latest features and bugfixes.
    0. Unzip the artifacts archive and put the mod inside ``mods/`` folder
    0. Start your server or client
    0. Configure Stonks by editing configuration file, placed inside ``config/`` folder
    0. Reload configuration
        + If you are running Stonks on server, restart your server.
        + If you are running Stonks on client, rejoin the world.

    !!! note
        If there are 2 JARs inside the archive, copy the one without ``-sources.jar`` suffix.

    !!! tip
        You might have noticed that Stonks can run in client and configurations can be reloaded by rejoining worlds. You can choose to load Stonks and other server-side mods in your client, which allows you to configure your mods without waiting for your server to initialize.
