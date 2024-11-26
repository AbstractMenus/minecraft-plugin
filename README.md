# AbstractMenus

<a href="https://github.com/AbstractMenus/plugin/blob/master/LICENSE"><img src="https://img.shields.io/badge/License-MIT-red.svg" alt="license"/></a>
<a href="https://github.com/AbstractMenus/plugin/blob/master/LICENSE"><img src="https://img.shields.io/badge/version-1.17.5--beta-blue" alt="license"/></a>

AbstractMenus is a GUI plugin for SpigotMC servers that allows server owners and developers to create custom GUIs with ease. The plugin is designed to be user-friendly and easy to use, with a wide range of features and options to customize the GUIs to your liking. The plugin is also highly customizable, allowing you to create GUIs that fit your server's theme and style.

Due to the fact that Mojang often changes things on the server side, and the fact that many servers are often updated to the latest versions at short notice, we have decided to support only a few of the latest versions of Minecraft. We currently support versions from 1.20.6 onwards.
This allows us to keep the code clean without using a bunch of wrappers for backwards compatibility, and also reduces the time to test the plugin functionality.

## 🔭Table of contents
- [Links](#links)
- [Supported versions](#supported-versions)
- [Installation and build](#installation-and-build)
- [Contributions and feedback](#contributions-and-feedback)
- [Licence](#licence)
---

## 🔗Links
- [SpigotMC](https://www.spigotmc.org/resources/abstract-menus-an-advanced-gui-plugin.75107/)
- [Discord](https://discord.gg/kt4P9Cgw)
- [Documentation](https://abstractmenus.github.io/docs/index.html)

## 💊Supported versions
- ❓1.21.3
- ✅1.21.1
- ❌<1.21.x

## Installation and build
Requirements:

* Gradle 8.5+
* JDK 21+

1. Clone the repository using Git:
```bash
git clone https://github.com/AbstractMenus/minecraft-plugin.git AbstractMenus
```

2. Navigate to the project folder:
```bash
cd AbstractMenus
```

3. Install Dependencies:
```
./gradlew build
```

4. Build project
```bash
./gradlew shadowJar
```

## 👪Contributions and feedback
If you have suggestions for improvement or want to report a bug, feel free to create an issue or make a pull request in the project repository.

## 📜Licence
This project is distributed under the **MIT** licence. You are free to use, modify and redistribute this code under the terms of the licence.