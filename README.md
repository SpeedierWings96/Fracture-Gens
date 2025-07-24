# Fracture-Gens

Advanced generator plugin for Minecraft 1.21.3 with sleek GUI and high performance.

## Features

- **Advanced Generator System**: Create and manage custom item generators with ease
- **Sleek GUI Interface**: Intuitive graphical interface for generator configuration
- **High Performance**: Optimized for minimal server impact with async processing
- **Flexible Configuration**: Customizable spawn rates, items, and directions
- **Permission System**: Comprehensive permission controls for different user levels
- **Auto-Save & Backup**: Automatic data protection with configurable backup system

## Requirements

- **Java 21** or higher
- **Minecraft 1.21.3**
- **Spigot/Paper** server

## Installation

1. Download the latest release
2. Place the JAR file in your server's `plugins` folder
3. Restart your server
4. Configure the plugin using `/fracturegens` commands

## Commands

- `/fracturegens help` - Show command help
- `/fracturegens list` - List your generators
- `/fracturegens reload` - Reload configuration (admin only)
- `/fracturegens stats` - Show plugin statistics

**Aliases**: `/fgens`, `/fg`

## Usage

1. **Creating Generators**: Shift + Right-click any solid block to create a generator
2. **Configuring Generators**: Shift + Right-click an existing generator to configure it
3. **Removing Generators**: Break a generator block to remove it

## Permissions

- `fracturegens.*` - Access to all features (default: op)
- `fracturegens.create` - Create new generators (default: op)
- `fracturegens.configure` - Configure existing generators (default: op)
- `fracturegens.remove` - Remove generators (default: op)
- `fracturegens.admin` - Administrative functions (default: op)

## Configuration

The plugin includes comprehensive configuration options for:
- Performance settings (max generators, tick intervals, async processing)
- Generator defaults (spawn rates, items, directions)
- GUI customization (update intervals, sounds, titles)
- Storage settings (auto-save, backups)
- Custom messages and colors

## Support

For issues, feature requests, or questions, please open an issue on GitHub.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
