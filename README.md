# ServerLogViewer 📜

**ServerLogViewer** is a project that allows you to view a server's logs locally on your client.

Designed with developers in mind, ServerLogViewer allows you to quickly **search and filter** logs by Log Level, Query or by Plugin Origin!

The project is split into the **Paper Plugin** and the **Fabric Mod**. Both are required to use the Log Viewer.

## Installation

To install and use ServerLogViewer, download the latest versions of [ServerLogViewer-Paper](https://modrinth.com/plugin/serverlogviewer-paper) and [ServerLogViewer-Fabric](https://modrinth.com/mod/serverlogviewer).
Add them to your Server and Client, respectively, like you would with any normal Plugin/Mod.

> [!NOTE]
> Authentication for viewing Server Logs is done on the Server side. Server Logs are NOT shared to anyone who isn't configured to be able to view the logs.

> [!IMPORTANT]
> Upon first installation, nobody will be configured to be able to view the server's logs.
> You will need to give explicit permissions to use the Log Viewer. This is done in the Paper Plugin section.

Then, follow these steps for each of the subprojects:
### ServerLogViewer-Paper

You will need to configure the plugin in ``/plugins/ServerLogViewer/config.yml`` in order to use the Log Viewer. This doesn't take long to do. You will get a message explaining what to do when you try to use the Mod for the first time too.

### ServerLogViewer-Fabric
ServerLogViewer requires ``owo-lib`` in order to run.


## Usage

After configuring the Paper Plugin, you will most likely not need to touch the plugin again. (Unless when editing the config)

To open the main Log Viewer screen, press the ``Open Logs`` keybind. By default this is set to ``I``. This is where the magic happens.

You may see one of the following messages:<br>
- ``This server isn't using ServerLogViewer!`` - The server you're on doesn't have the plugin installed, or you don't have permissions to view the logs. You can press the reload button if you have gotten the permissions in the meantime.

- ``Welcome to ServerLogViewer!`` - You need to setup the Paper Plugin config. This menu gives you details on what to do.

A useful keybind to know is the ``View Log Plugins`` keybind. By default, this is set to ``LEFT ALT``. This keybind allows you to view the plugin origin of each log line.

The rest of ServerLogViewer is pretty simple and intuitive to use - you'll be able to figure it out on your own! 😉
