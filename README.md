# 1.16.1-Extreme-Difficulty

**Plugin trailer on Iseburg's channel: https://www.youtube.com/watch?v=7Kl9jtCHIGg**

To reduce the volume of explosion noises without affecting everything else, use the "/explosionvolume set [decimal between 0 and 1]" command (you can also use "/explosionvolume get" to check the current volume).

WARNING: plugin is pretty heavyweight, do not run on a server with low RAM allocation

# Installation instructions:

**Spigot servers, version 1.16.1**

**Java version 8 (see below for setup guide)**

1. Click on "Releases" near the right side of the screen
2. Download the jar file from the release you want under the "Assets" tab (unlike with modded clients, you do not need to download the source code zip files; those only provide the code for the mod and not the runnable file itself)
3. Put the jar file into your plugin folder in your server folder
4. Run your server
5. Join the server in creative mode because every player after the first one that joins gets debuffed for 15 seconds to prevent relog exploits

# Java 8 setup guide:

Follow this guide if the plugin doesn't seem to be doing anything and the server log has an error about mismatched/unsupported Java versions

1. Go to [Adoptium's release archives](https://adoptium.net/temurin/archive) and select version 8
2. Scroll down. Find your operating system and download the "JDK" option under the "Installer" column for your operating system (if you are using Linux, the installation process is different but I trust that you know what you're doing if you're using Linux).
3. Click on the downloaded file and follow the installation steps.
4. At a certain step during the installation, you will see options on the left side of the window for both "set JAVA_HOME variable" and "Add to PATH". Click the icon next to both of these and select "local drive only" (wording not exact)   .
    * If you don't see this, then it should automatically have done this for you.
5. Finish the installation process, and you should now be running Java 8. If you want to confirm that you are actually running the correct version, open a terminal or command prompt and type "java --version".

# Developer guide (IntelliJ):

1. `git clone`
2. In **File > Project Structure**:
   * Use a Java 8 JDK
   * Use language level 8
   * In Modules on the left, select the main module, go to **Dependencies > + > JARs or Directories...**, and select [spigot-1.16.1.jar](spigot-1.16.1.jar)
   * In Artifacts on the left, go to **+ > JAR > From modules with dependencies... > OK**
      * Then, click the **+** icon on the right panel (which should have a little arrow under it), click **File**, and select [src/plugin.yml](src/plugin.yml)
* To create a Spigot 1.16.1 server, copy [spigot-1.16.1.jar](spigot-1.16.1.jar) (or download it from their site) into a folder in `.minecraft/`. In that folder, create `start.bat` with the following:
   ```
   @echo off
   java -jar spigot-1.16.1.jar nogui
   pause
   ```
  and run it. You will probably also need to create a `plugins/` folder manually.
* To build the plugin JAR, go to **Build > Build Artifacts... > Build**. By default, this artifact should be located in the output directory [out/artifacts/1_16_1_Extreme_Difficulty_jar/1.16.1-Extreme-Difficulty.jar](out/artifacts/1_16_1_Extreme_Difficulty_jar/1.16.1-Extreme-Difficulty.jar). Copy this into the `plugins/` folder in `.minecraft/[server folder]/`.

# Finally done

This is a plugin made with Spigot's deobfuscation mappings, as well as Bukkit API and its underlying CraftBukkit and CraftBukkit's underlying NMS (vanilla server code). Why so many layers?
