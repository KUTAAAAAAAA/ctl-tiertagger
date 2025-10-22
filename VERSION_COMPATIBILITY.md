# Version Compatibility Guide

## Supported Minecraft Versions

This mod is built to support **Minecraft 1.21 through 1.21.10**:
- ✅ Minecraft 1.21
- ✅ Minecraft 1.21.1
- ✅ Minecraft 1.21.2
- ✅ Minecraft 1.21.3
- ✅ Minecraft 1.21.4
- ✅ Minecraft 1.21.5
- ✅ Minecraft 1.21.6
- ✅ Minecraft 1.21.7
- ✅ Minecraft 1.21.8
- ✅ Minecraft 1.21.9
- ✅ Minecraft 1.21.10

## How Version Compatibility Works

The mod uses the `>=1.21.1 <=1.21.10` version constraint in `fabric.mod.json`, which means:
- The mod will work with any Minecraft version from 1.21.1 to 1.21.10 (inclusive)
- It will NOT work with 1.21.0, 1.20.x, 1.21.11+, or 1.22.x (if/when released)
- This provides explicit version range control for maximum compatibility

## Technical Details

### Build Configuration
- **Base Version**: 1.21.1 (for compatibility)
- **Yarn Mappings**: 1.21.1+build.3
- **Fabric Loader**: 0.16.0+
- **Fabric API**: 0.102.0+1.21.1 (or any 1.21.x compatible version)

### Version String Format
The mod declares: `"minecraft": ">=1.21.1 <=1.21.10"`

This uses Fabric's version range syntax:
- `>=1.21.1 <=1.21.10` = 1.21.1 through 1.21.10 (inclusive range)
- `~1.21` = 1.21.x (any patch version)
- `>=1.21` = 1.21 and above (including 1.22+)
- `1.21.1` = exactly 1.21.1

## Testing Compatibility

The mod has been built against Minecraft 1.21.1 which provides maximum compatibility with:
- Earlier 1.21.x versions (forward compatible)
- Later 1.21.x versions (backward compatible)

## Future Minecraft Versions

### For 1.21.x updates
The mod should work without recompilation for future 1.21.x releases.

### For 1.22+ (when released)
A new version of the mod will need to be built with:
- Updated `minecraft_version` in gradle.properties
- Updated `yarn_mappings` for the new version
- Potentially updated `fabric_version` for the new Minecraft version
- Update version constraint to `~1.22` or `>=1.21`

## Fabric API Compatibility

The mod declares `"fabric-api": "*"` which means:
- Any version of Fabric API is accepted
- Users should install the Fabric API version matching their Minecraft version
- For 1.21.1 → Use Fabric API 0.102.0+1.21.1 or compatible
- For 1.21.4 → Use Fabric API 0.110.0+1.21.4 or compatible

## Loader Version

**Minimum**: Fabric Loader 0.16.0
**Recommended**: Latest Fabric Loader for your Minecraft version

## Java Version

**Minimum**: Java 21
**Recommended**: Java 21 or Java 22

Minecraft 1.21+ requires Java 21, so this mod follows the same requirement.

## Installation for Different Versions

### For Minecraft 1.21.1:
1. Install Fabric Loader 0.16.0+
2. Install Fabric API 0.102.0+1.21.1
3. Install CTL TierTagger 1.0.0
4. Launch game

### For Minecraft 1.21.4:
1. Install Fabric Loader 0.16.9+
2. Install Fabric API 0.110.0+1.21.4
3. Install CTL TierTagger 1.0.0 (same file)
4. Launch game

### For Other 1.21.x versions:
1. Install Fabric Loader 0.16.0+
2. Install Fabric API for your Minecraft version
3. Install CTL TierTagger 1.0.0 (same file)
4. Launch game

## Troubleshooting

### "Incompatible mod set!" error
- Check that you're using Minecraft 1.21.x (not 1.20.x or older)
- Verify Java 21+ is installed
- Ensure Fabric Loader 0.16.0+ is installed

### Missing Fabric API
- Download the correct Fabric API version for your Minecraft version
- Place it in the mods folder alongside CTL TierTagger

### Mixin errors
- Ensure no conflicting mods are installed
- Update Fabric Loader to the latest version
- Check logs for specific conflicts

## Version History

### v1.0.0
- Initial release
- Supports Minecraft ~1.21 (all 1.21.x versions)
- Built against 1.21.1 for maximum compatibility
- Requires Fabric Loader 0.16.0+
- Requires Java 21+
