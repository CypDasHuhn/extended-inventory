# Split & Cleanup — Final Report

## Files Changed

### New files (8 files, 426 lines)
| File | Lines | Purpose |
|---|---|---|
| `commands/NavCommands.kt` | 102 | Jump-to, position, mode, direction, cycle builders |
| `commands/ProfileCommands.kt` | 145 | Profile subcommands + `suggestProfileNames()` |
| `commands/AnchorCommands.kt` | 93 | Anchor subcommands + `suggestAnchorNames()` |
| `commands/BufferCommands.kt` | 33 | Buffer subcommands + `suggestBufferNames()` |
| `util/Messages.kt` | 11 | Single MiniMessage instance + `mm()` + `Player.msg()` |
| `util/PlayerUtils.kt` | 10 | Shared `playerId(player: Player)` |
| `util/RegionUtils.kt` | 10 | `Region` data class + `region()` factory |
| `util/TextStyles.kt` | 22 | `T` color/style constants + `positionMsg()` helpers |

### Modified files (17 files, +163 / -623 lines)

## What Improved

### Readability
- **Commands.kt** went from 329 lines to 35 lines — now just the tree skeleton
- Each command domain is self-contained in its own file
- `NavCommands.kt` clearly separates grid navigation from profile/anchor/buffer management
- Typealiases (`RowCache`, `ProfileCache`, `CacheStore`) clarify SlotCache's nested map structure
- `T.green`, `T.red`, etc. catch typos at compile time
- `positionMsg(x, y)` eliminates 6+ copies of the same message pattern

### Architecture
- **SlotCache** now delegates all DB writes to `InventoryManager` (no duplicated queries)
- **InventoryManager** owns all batch operations (`batchRemove`, `batchSetItems`)
- **`playerId()`** extracted from two duplicate copies into one shared utility
- **`MiniMessage`** instance shared from a single source (was 3 separate instances)
- **`Region`** utility eliminates inline coordinate normalization in 4+ files

### Pre-existing bugs fixed
- `PlayerArgument` → `EntitySelectorArgument.OnePlayer` (CommandAPI v11)
- `between` → `SqlExpressionBuilder.between` (Exposed 0.49)
- `InitUi` subpackage shadowing by `dev.rooster.ui.ui`
- Missing `InventoryManager` import in `HotbarManager`
- Null-safety in `HotbarManager.resolveAnchorJump()`
- Gson type mismatch in `ItemManager`
- `openInventory` override final in `ProfileInterface`
- String template `context` resolution in UI files
- Removed empty `cycling/` directory
- Added JUnit 5 dependency

### Remaining (pre-existing)
- `BufferManagerTest` MockPlayer outdated vs Paper 1.21 API — 474-line manual mock needs API update
- `SlotCacheTest` doesn't function without DB — needs integration test setup
- MiniMessage inline tags in UI files (`InventoryInterface.kt`, `AnchorInterfaces.kt`, `ProfileInterfaces.kt`) still use raw strings — readable enough as-is since they're visual styling
- `ProfileInterface.openRefreshed()` is a workaround for `openInventory` being final
