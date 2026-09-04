# UI Slot Map — Rooster ScrollInterface (6 rows, slot indices 0‑53)

## Coordinate System

- `atSlot(row, slotWithinRow)` where slotWithinRow is 0‑based (0‑8)
- Slot index = `(row - 1) × 9 + slotWithinRow`
- Row 1 = slots 0‑8, Row 2 = 9‑17, …, Row 6 = 45‑53

## Inherited from ScrollInterface (every interface gets these)

| Item | Slots | Priority | Condition |
|------|-------|----------|-----------|
| **ContentItem** (data display) | 0‑44 (contentArea.allValidSlots) | -1 (default) | only when content data exists |
| **ClickInArea** (AIR filler) | 45‑53 (bottom row) | -1 | only when no content data at that slot |
| **Scroller (Solo)** | 53 (bottom-right) | -1 (default) | always visible |

**Bottom row inheritence**: ClickInArea fills 45‑53 with AIR items at priority -1. Scroller sits at slot 53. Explicit items from `getInterfaceItems()` are added later and override same-priority inherited items at same slots (last-added wins).

---

## 1. InventoryInterface

**Size**: 6 rows (54 slots). **Content area**: rows 1‑5 (slots 0‑44).

### Overlay Items (placed on all content-area slots via `atSlots`)

| Item | Slots | Priority | Condition |
|------|-------|----------|-----------|
| Corner A indicator | 0‑44 | 10 | cornerA set & matches slot |
| Corner B indicator | 0‑44 | 10 | cornerB set & matches slot |
| Target preview | 0‑44 | 9 | targetPreviewPositions contains slot |

### Bottom Row (row 6, slots 45‑53)

| Slot | Index | Item | Priority | Condition |
|------|-------|------|----------|-----------|
| (6,0) | 45 | *(empty)* | — | ClickInArea shows AIR |
| (6,1) | 46 | **Back** (BARRIER) | -1 | always |
| (6,2) | 47 | **Group Delete** (LAVA_BUCKET) | -1 | !inGroupMode && !cornersSet |
| (6,3) | 48 | **Group Move** (PISTON) | -1 | !inGroupMode && !cornersSet && !targetSet |
| (6,4) | 49 | **Edit Mode** (BOOK/WRITABLE_BOOK) | -1 | !inGroupMode && !cornersSet && !targetSet |
| (6,5) | 50 | **Cancel Group** (BARRIER) | -1 | inGroupMode |
| (6,5) | 50 | **Set Anchor** (ENDER_PEARL) | -1 | !inGroupMode && !cornersSet && !targetSet |
| (6,6) | 51 | **Materialize Anchor** (ITEM_FRAME) | -1 | !inGroupMode && !cornersSet && !targetSet |
| (6,6) | 51 | **Delete Confirm** (LAVA_BUCKET) | 10 | cornersSet && !targetSet && !deleteConfirmed |
| (6,6) | 51 | **Delete Final** (LAVA_BUCKET) | 11 | cornersSet && !targetSet && deleteConfirmed |
| (6,6) | 51 | **Move Confirm** (PISTON) | 10 | targetSet && !moveConfirmed |
| (6,6) | 51 | **Move Final** (PISTON) | 11 | targetSet && moveConfirmed |
| (6,7) | 52 | **Anchors** (NAME_TAG) | -1 | always |
| (6,8) | 53 | **Profiles** (PLAYER_HEAD) | -1 | always |
| *(inherited)* | 53 | **Scroller** (COMPASS) | -1 | always |

### ⚠️ Conflicts Found

| Slot | Conflict | Severity |
|------|----------|----------|
| **53** | `Profiles` vs inherited `Scroller` — both priority -1, both always visible. Profiles wins (added later) → **scroller is hidden**. Player cannot scroll inventory grid. | **CRITICAL** |
| **50** | `Cancel Group` vs `Set Anchor` — mutually exclusive conditions (inGroupMode vs !inGroupMode). No actual overlap. | OK |
| **51** | 5 items share same slot with priority tiers (9/10/11) + mutually exclusive conditions. No actual overlap. | OK |

### Bottom Row Item Count
8 explicit items across 5 slots (46-53), plus inherited Scroller at 53 → **6 non-empty slots**.
Slot 45 (6,0) is empty (no explicit item, just ClickInArea AIR).

---

## 2. AnchorListInterface

**Size**: 6 rows (54 slots). **Content area**: rows 1‑5 (slots 0‑44).

### Bottom Row

| Slot | Index | Item | Priority | Condition |
|------|-------|------|----------|-----------|
| (6,0) | 45 | *(empty)* | — | ClickInArea AIR |
| (6,1) | 46 | **Back** (BARRIER) → InventoryInterface | -1 | always |
| (6,2) | 47 | *(empty)* | — | ClickInArea AIR |
| (6,3) | 48 | *(empty)* | — | ClickInArea AIR |
| (6,4) | 49 | **New Anchor** (WRITABLE_BOOK) | -1 | always |
| (6,5) | 50 | *(empty)* | — | ClickInArea AIR |
| (6,6) | 51 | *(empty)* | — | ClickInArea AIR |
| (6,7) | 52 | *(empty)* | — | ClickInArea AIR |
| *(inherited)* | 53 | **Scroller** (COMPASS) | -1 | always |

No conflicts. Bottom row: 3 non-empty slots. ✅

---

## 3. AnchorDetailInterface

**Size**: 6 rows (54 slots). **Content area**: rows 1‑5 (slots 0‑44).
**Content**: none (contentProvider returns null). This interface is a fixed detail view; ScrollInterface usage is gratuitous.

### Non-Bottom-Row Items (in content area rows 3-4)

| Slot | Index | Item | Priority |
|------|-------|------|----------|
| (3,4) | 31 | **Info** (ENDER_PEARL) | -1 |
| (3,5) | 32 | **Jump To** (COMPASS) | -1 |
| (3,6) | 33 | **Rename** (NAME_TAG) | -1 |
| (3,7) | 34 | **Delete** (LAVA_BUCKET) | -1 |
| (4,5) | 41 | **Materialize** (ITEM_FRAME) | -1 |

### Bottom Row

| Slot | Index | Item | Condition |
|------|-------|------|-----------|
| (6,0) | 45 | *(empty)* | ClickInArea AIR |
| (6,1) | 46 | **Back** (BARRIER) → AnchorListInterface | always |
| (6,2‑8) | 47‑53 | *(empty)* | ClickInArea AIR |
| *(inherited)* | 53 | **Scroller** (COMPASS) | always (but no content to scroll) |

No conflicts. Bottom row: 2 non-empty slots. ✅
**Note**: Scroller is useless here — no content to scroll. Could be removed.

---

## 4. ProfileInterface

**Size**: 6 rows (54 slots). **Content area**: rows 1‑5 (slots 0‑44).

### Bottom Row — NORMAL mode

| Slot | Index | Item | Priority | Condition |
|------|-------|------|----------|-----------|
| (6,0) | 45 | **Profiles** (PLAYER_HEAD) | -1 | always |
| (6,1) | 46 | **Back** (BARRIER) | -1 | always |
| (6,2) | 47 | **Group Delete** (LAVA_BUCKET) | -1 | NORMAL mode, !inGroupMode, !cornersSet |
| (6,3) | 48 | **Group Move** (PISTON) | -1 | NORMAL mode, !inGroupMode, !cornersSet, !targetSet |
| (6,4) | 49 | **Edit Mode** (BOOK) | -1 | NORMAL mode, !inGroupMode, !cornersSet, !targetSet |
| (6,5) | 50 | **Cancel Group** (BARRIER) | -1 | inGroupMode |
| (6,5) | 50 | **Set Anchor** (ENDER_PEARL) | -1 | NORMAL mode, !inGroupMode, !cornersSet, !targetSet |
| (6,6) | 51 | **Materialize Anchor** (ITEM_FRAME) | -1 | NORMAL mode, !inGroupMode, !cornersSet, !targetSet |
| (6,6) | 51 | **Delete Confirm** (LAVA_BUCKET) | 10 | cornersSet, !targetSet, !deleteConfirmed |
| (6,6) | 51 | **Delete Final** (LAVA_BUCKET) | 11 | cornersSet, !targetSet, deleteConfirmed |
| (6,6) | 51 | **Move Confirm** (PISTON) | 10 | targetSet, !moveConfirmed |
| (6,6) | 51 | **Move Final** (PISTON) | 11 | targetSet, moveConfirmed |
| (6,7) | 52 | **Anchors** (NAME_TAG) | -1 | always |
| *(inherited)* | 53 | **Scroller** (COMPASS) | -1 | always |

### Bottom Row — EDITING mode

| Slot | Index | Item | Priority | Condition |
|------|-------|------|----------|-----------|
| (6,0) | 45 | **Profiles** (PLAYER_HEAD) | -1 | always |
| (6,1) | 46 | **Back** (BARRIER) | -1 | always |
| (6,2) | 47 | *(empty)* | — | ClickInArea AIR |
| (6,3) | 48 | **Save** (WRITABLE_BOOK) | -1 | EDITING mode |
| (6,4) | 49 | **Discard** (BARRIER) | -1 | EDITING mode |
| (6,5) | 50 | *(empty)* | — | ClickInArea AIR |
| (6,6) | 51 | *(empty)* | — | ClickInArea AIR |
| (6,7) | 52 | **Anchors** (NAME_TAG) | -1 | always |
| *(inherited)* | 53 | **Scroller** (COMPASS) | -1 | always |

No conflicts. Bottom row: 3 non-empty slots. ✅

---

## 5. ProfileDetailInterface

**Size**: 6 rows (54 slots). **Content area**: rows 1‑5 (slots 0‑44).
**Content**: none (contentProvider returns null). ScrollInterface usage is gratuitous.

### Non-Bottom-Row Items

| Slot | Index | Item | Priority |
|------|-------|------|----------|
| (2,4) | 22 | **Info** (BOOK) | -1 |
| (2,5) | 23 | **Switch To** (ENDER_PEARL) | -1 |
| (2,6) | 24 | **Set Default** (BOOKSHELF) | -1 |
| (2,7) | 25 | **Rename** (NAME_TAG) | -1 |
| (3,4) | 31 | **Openness** (REPEATER) | -1 |
| (3,5) | 32 | **Invitations** (PLAYER_HEAD) | -1 |
| (3,6) | 33 | **Delete** (LAVA_BUCKET) | -1 |

### Bottom Row

| Slot | Index | Item | Condition |
|------|-------|------|-----------|
| (6,0) | 45 | *(empty)* | ClickInArea AIR |
| (6,1) | 46 | **Back** (BARRIER) → ProfileInterface | always |
| (6,2‑8) | 47‑53 | *(empty)* | ClickInArea AIR |
| *(inherited)* | 53 | **Scroller** (COMPASS) | always (useless — no content) |

No conflicts. Bottom row: 2 non-empty slots. ✅

---

## 6. PlayerInviteInterface

**Size**: 6 rows (54 slots). **Content area**: rows 1‑5 (slots 0‑44).

### Bottom Row

| Slot | Index | Item | Priority | Condition |
|------|-------|------|----------|-----------|
| (6,0) | 45 | *(empty)* | — | ClickInArea AIR |
| (6,1) | 46 | **Back** (BARRIER, closeInventory) | -1 | always |
| (6,2‑8) | 47‑53 | *(empty)* | — | ClickInArea AIR |
| ## Resolved

### 1. Slot 53 Conflict (Profiles vs Scroller) — FIXED
Moved Profiles from (6,8) [slot 53] to (6,0) [slot 45]. Scroller at slot 53 is now always visible.

### 2. slotToGrid Position Double-Counting — FIXED
The framework's `slotToId` already adds `position * 9` to the content ID. `slotToGrid` was adding `position` again, causing the view to shift by 2 rows per scroll step instead of 1. Removed the extra `+ ctx.position`.

### 3. Implicit Save in Edit Mode — FIXED
Replaced the toggle-style Edit Mode button with explicit **Save** (WRITABLE_BOOK, slot 48) and **Discard** (BARRIER, slot 49) buttons that only appear in EDITING mode. Group Delete, Group Move, Set Anchor, and Materialize Anchor are now hidden in EDITING mode (`usedWhen` checks `context.mode == InterfaceMode.NORMAL`).

### Remaining: Gratuitous ScrollInterface
`AnchorDetailInterface` and `ProfileDetailInterface` extend `ScrollInterface` but have no scrollable content (contentProvider returns null). Should extend `RoosterInterface` directly. Not a functional bug.

---

## Summary

| Interface | Bottom Row Items | Conflicts | Needs Overhaul |
|-----------|-----------------|-----------|----------------|
| InventoryInterface | 6 used + 3 conditional (9 total at slot 50‑51) | **slot 53: Profiles hides Scroller** | ⚠️ needs slot reassignment |
| AnchorListInterface | 3 | none | ✅ clean |
| AnchorDetailInterface | 2 | none | ⚠️ gratuitous ScrollInterface |
| ProfileInterface | 3 | none | ✅ clean |
| ProfileDetailInterface | 2 | none | ⚠️ gratuitous ScrollInterface |
| PlayerInviteInterface | 2 | none | ✅ clean |

### Critical Bug
**InventoryInterface slot 53**: The Profiles item and the Scroller (needed for grid navigation) both occupy the same slot. The Profiles item wins (added later), hiding the scroller. Players cannot scroll the inventory grid via UI.

### Gratuitous ScrollInterface Usage
`AnchorDetailInterface` and `ProfileDetailInterface` extend `ScrollInterface` but have no scrollable content (contentProvider returns null). They should extend a simpler base class or `PagelessInterface`.

### Bottom Row > 9
No interface exceeds 9 items in the bottom row. InventoryInterface has 6 visible + 3 conditional (which are mutually exclusive), fitting in 6 physical slots.
