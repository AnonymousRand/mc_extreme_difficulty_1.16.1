# MC-Extreme-Difficulty-1.16.1

Spigot plugin for Minecraft 1.16.1. Plugin trailer [on Iseburg's channel](https://www.youtube.com/watch?v=7Kl9jtCHIGg).

# WARNING

- Good hardware required
- I suggest turning off dynamic FOV (foreshadowing…)

# Installation guide:

1. Click on "Releases" near the right side of the screen
2. Download the jar file from the release you want under the "Assets" tab (unlike with modded clients, you do not need to download the source code zip files; those only provide the code for the mod and not the runnable file itself)
3. Put the jar file into your plugin folder in your server folder
4. Run your server

### Java 8 setup guide:
1. Go to [Adoptium's release archives](https://adoptium.net/temurin/archive) and select version 8
2. Scroll down. Find your operating system and download the "JDK" option under the "Installer" column for your operating system (if you are using Linux, the installation process is different but I trust that you know what you're doing if you're using Linux).
3. Click on the downloaded file and follow the installation steps.
4. At a certain step during the installation, you will see options on the left side of the window for both "set JAVA_HOME variable" and "Add to PATH". Click the icon next to both of these and select "local drive only" (wording not exact)   .
    * If you don't see this, then it should automatically have done this for you.
5. Finish the installation process, and you should now be running Java 8. If you want to confirm that you are actually running the correct version, open a terminal or command prompt and type "java --version".

# Developer guide (IntelliJ):

1. `git clone`
2. Build `spigot-1.16.1.jar` using [BuildTools](https://www.spigotmc.org/wiki/buildtools/), and copy it to both this repo's base and to a folder in `.minecraft/` (I apparently can't distribute it due to legal issues?)
3. This step should be automatically applied by IntelliJ via the [.idea/](.idea/) stuff, but it case it hasn't: go to **File > Project Structure** and:
   * Use a Java 8 JDK
   * Use language level 8
   * In Modules on the left, select the main module, go to **Dependencies > + > JARs or Directories...**, and select the copy of `spigot-1.16.1.jar` compiled by BuildTools
   * In Artifacts on the left, go to **+ > JAR > From modules with dependencies... > OK**
      * Then, click the **+** icon on the right panel (which should have a little arrow under it), click **File**, and select [src/plugin.yml](src/plugin.yml)
4. To create a Spigot 1.16.1 server, navigate to the folder in `.minecraft/` where `spigot-1.16.1.jar` had been copied. In that folder, create `start.bat` with the following:
   ```
   @echo off
   java -jar spigot-1.16.1.jar nogui
   pause
   ```
   and run it. You will probably also need to create a `plugins/` folder manually.
5. To build the plugin JAR, go to **Build > Build Artifacts... > Build**. By default, this artifact should be located in the output directory [out/artifacts/extreme-difficulty-1.16.1-jar/extreme_difficulty_1.16.1.jar](out/artifacts/extreme-difficulty-1.16.1-jar/extreme_difficulty_1.16.1.jar). Copy this into the `plugins/` folder in `.minecraft/[server folder]/`.
