# Minecraft VoiceAttack Integration Mod

## Overview

The Minecraft VoiceAttack Integration Mod is a client-side utility designed to enhance accessibility and gameplay experience by allowing VoiceAttack to communicate directly with Minecraft. This mod enables players to execute keybind actions using voice commands, making it easier to navigate the game without relying solely on traditional input methods. It is particularly beneficial for players using controller mods, as it eliminates the need to remember complex button combinations to access various mods and UIs.

## Features

- **Seamless Integration**: Works out of the box with controller mods, allowing for easy access to game functions without the need for complicated button combinations.
- **Voice Command Execution**: Use VoiceAttack to trigger keybind actions in Minecraft, enhancing accessibility for players with different needs.
- **Automatic Port Configuration**: The mod automatically establishes a connection to VoiceAttack on a specific static port, generating a unique port for communication during the handshake process.

## How It Works

1. **Connection Establishment**: Upon launching Minecraft with the mod, it attempts to connect to VoiceAttack on a predefined static port.
2. **Handshake Process**: The mod sends a handshake request to VoiceAttack, along with a randomly generated port number for future communication.
3. **Key Command Execution**: Once the handshake is successful, VoiceAttack will use the provided port to send key commands to the mod, allowing for real-time execution of keybind actions.

4. Keybinds for Socket Management:
- Restart Socket Server: A keybind to restart the socket server, ensuring a stable connection with VoiceAttack. Default key is `R`.
 - Update Mappings: A keybind to export a `voiceattack_translation_keys.json` file to the configs folder, which contains the translation keys for any bound key at the time of export. Default key is `U`.

## Setup Instructions

### Minecraft Mod

- **No Setup Required**: Simply install the mod in your Minecraft client, and it will automatically handle the connection to VoiceAttack.

### VoiceAttack Configuration

To set up VoiceAttack for use with this mod, you need to configure each command with the following parameters:

1. **Translation Key**: Create a text variable named `keybind` that corresponds to the action you want to trigger in Minecraft. This variable should hold the translation key for the desired keybind.
   
2. **Execute Command**: Use the Minecraft plugin for VoiceAttack to execute the command. Pass the context `execute_keybind` along with the `keybind` variable you created earlier.

### Example Command Setup

1. Create a new command in VoiceAttack.
2. Add a text variable named `keybind` with the value of the desired Minecraft action (e.g., `key.inventory`).
3. Add an action to execute the Minecraft plugin with the context `execute_keybind` and reference the `keybind` variable.

### Exporting Translation Keys
To export the translation keys for any bound key:

1. Use the designated keybind to export the `voiceattack_translation_keys.json` file.
2. Locate the exported file in the configs folder.
3. Reference this file to accurately send data to Minecraft via VoiceAttack.

## Use Cases

- **Accessibility**: Ideal for players with mobility challenges who may find traditional controls difficult to use.
- **Hands-Free Gameplay**: Allows players to execute commands without needing to use their hands, making it easier to multitask or play in a more relaxed position.
- **Enhanced Gameplay**: Streamlines the process of accessing various mods and UIs, improving overall gameplay efficiency.

## Limitations

- The mod does not support all keybind methods, including:
  - Player movement controls (e.g., walking, running).
  - Mouse look functionality.
  - Keybinds that require holding down a key (e.g., zooming into a world map).
