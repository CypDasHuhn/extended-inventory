# Code Cleanup — Final Report

## Summary
- **16 files modified**, 168 insertions, 294 deletions (-126 net lines)
- **3 new utility files created** under `src/main/kotlin/.../util/`
- **Main build compiles successfully** (all pre-existing compile errors fixed)
- **Test compilation remains broken** (MockPlayer outdated vs Paper 1.21 API — pre-existing)

## Completed Changes

### 1. Shared Utilities (`src/main/kotlin/.../util/`)

| File | Purpose |
|---|---|
| `Messages.kt` | Single `MiniMessage.miniMessage()` instance, `mm()` deserializer, `Player.msg()` extension |
| `PlayerUtils.kt` | Shared `playerId(player: Player)` — was duplicated in both `PlayerProfileManager.kt` and `ProfileManager.kt` |
| `RegionUtils.kt` | `Region` data class + `region()` factory for coordinate normalization |

### 2. SlotCache Deduplication
- **Before**: `SlotCache` duplicated all DB write logic from `InventoryManager` (identical transaction blocks in `setItem`, `setAnchor`, `batchRemove`, `batchSetItems`)
- **After**: `SlotCache` delegates all DB writes to `InventoryManager` and only maintains the in-memory cache
- Added `batchRemove()` and `batchSetItems()` to `InventoryManager` as the canonical DB write methods
- Added typealiases `RowCache`, `ProfileCache`, `CacheStore` for the nested map types

### 3. Command Deduplication
- Replaced four identical direction builders (`buildUpNode`, `buildDownNode`, `buildLeftNode`, `buildRightNode`) with a single parameterized `buildDirectionNode(name, dx, dy)`

### 4. Region Normalization
- Replaced inline `minOf/maxOf` region calculations in `Actions.kt`, `InventoryManager.kt`, `InventoryInterface.kt` with the shared `region()` utility

### 5. Message Infrastructure
- Merged 3 separate `MiniMessage.miniMessage()` instances into a single shared instance
- Removed duplicate `mm()` and `Player.msg()` from `Helpers.kt` and `UiHelpers.kt`

### 6. Pre-existing Compile Errors Fixed

| Error | Fix |
|---|---|
| `Commands.kt`: `PlayerArgument` unresolved (CommandAPI v11 removed it) | Changed to `EntitySelectorArgument.OnePlayer` |
| `InventoryManager.kt`: `between` unresolved (Exposed 0.49 API change) | Changed import to `SqlExpressionBuilder.between` |
| `InitUi.kt`: `inventory`, `anchor`, `profile` subpackages unresolved | Used explicit fully-qualified imports (avoided shadowing by `dev.rooster.ui.ui`) |
| `HotbarManager.kt`: `InventoryManager` not imported | Added missing import |
| `HotbarManager.kt`: null-safety on `resolveAnchorJump` | Added `?.` safe call |
| `ItemManager.kt`: Gson type mismatch `Map<*,*>` vs `Map<String, Any>` | Used `gson.fromJson<Map<String, Any>>()` |
| `ProfileInterfaces.kt`: `openInventory` override final in ScrollInterface | Changed to `openRefreshed()` helper method |
| `AnchorInterfaces.kt` / `ProfileInterfaces.kt`: `context` resolution in string templates | Extracted to local `ctx` variable |

### 7. Other
- Added JUnit 5 dependency to `build.gradle.kts`
- Removed empty `cycling/` directory
- Added `.md` file import for `ImportManager` in `HotbarManager`

## Remaining Issues (Pre-existing)

| Issue | Location |
|---|---|
| Test compilation broken | `BufferManagerTest.kt` — `MockPlayer` outdated against Paper 1.21 API |
| Test compilation broken | `SlotCacheTest.kt` — `MockPlayer` + no DB available |
| `ProfileInterface` `routeTo` can't auto-refresh | Workaround: `openRefreshed()` — `routeTo` calls use `onClick` with manual refresh |
| No linting configured | Add ktlint Gradle plugin |
