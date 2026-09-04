# UI Slot Map — Rooster ScrollInterface (6 rows, slot indices 0‑53)

## Coordinate System

- `atSlot(row, col)` where `row` is 1‑based and `col` is 0‑based (0‑8).
- Slot index = `(row - 1) × 9 + col`.
- Row 1 = slots 0‑8, Row 2 = 9‑17, …, Row 6 = 45‑53.

## Inherited from ScrollInterface (every interface gets these)

| Item | Slots | Priority | Condition |
|------|-------|----------|-----------|
| **ContentItem** (data display) | 0‑44 (contentArea) | -1 (default) | only when content data exists |
| **ClickInArea** (AIR filler) | 45‑53 (bottom row) | -1 | only when no content data at that slot |
| **Scroller** (COMPASS) | 53 (bottom-right) | -1 (default) | always visible |

Bottom-row inheritance: `ClickInArea` fills 45‑53 with AIR at priority -1. The
Scroller sits at slot 53. Explicit items from `getInterfaceItems()` are added
later and override same-priority inherited items at the same slot.

---

## 1. InventoryInterface

**Size**: 6 rows (54 slots). **Content area**: rows 1‑5 (slots 0‑44).

### Overlay Items (placed on all content-area slots via `atSlots`)

| Item | Slots | Priority | Condition |
|------|-------|----------|-----------|
| Corner A indicator (RED_STAINED_GLASS_PANE) | 0‑44 | 10 | cornerA set & slot maps to it |
| Corner B indicator (BLUE_STAINED_GLASS_PANE) | 0‑44 | 10 | cornerB set & slot maps to it |
| Target preview (GREEN_STAINED_GLASS_PANE) | 0‑44 | 9 | targetPreviewPositions contains slot |

### Bottom Row (row 6, slots 45‑53)

| Slot | Index | Item | Priority | Condition |
|------|-------|------|----------|-----------|
| (6,0) | 45 | **Profiles** (PLAYER_HEAD) | -1 | always |
| (6,1) | 46 | **Back** (BARRIER) | -1 | always |
| (6,2) | 47 | **Group Delete** (LAVA_BUCKET) | -1 | NORMAL, !inGroupMode, !cornersSet |
| (6,3) | 48 | **Group Move** (PISTON) | -1 | NORMAL, !inGroupMode, !cornersSet, !targetSet |
| (6,3) | 48 | **Save** (WRITABLE_BOOK) | -1 | EDITING |
| (6,4) | 49 | **Edit Mode** (BOOK) | -1 | NORMAL, !inGroupMode, !cornersSet, !targetSet |
| (6,4) | 49 | **Discard** (BARRIER) | -1 | EDITING |
| (6,5) | 50 | **Cancel Group** (BARRIER) | -1 | inGroupMode |
| (6,5) | 50 | **Set Anchor** (ENDER_PEARL) | -1 | NORMAL, !inGroupMode, !cornersSet, !targetSet |
| (6,6) | 51 | **Materialize Anchor** (ITEM_FRAME) | -1 | NORMAL, !inGroupMode, !cornersSet, !targetSet |
| (6,6) | 51 | **Delete Confirm** (LAVA_BUCKET) | 10 | cornersSet, !targetSet, !groupDeleteConfirmed |
| (6,6) | 51 | **Delete Final** (LAVA_BUCKET) | 11 | cornersSet, !targetSet, groupDeleteConfirmed |
| (6,6) | 51 | **Move Confirm** (PISTON) | 10 | targetSet, !groupMoveConfirmed |
| (6,6) | 51 | **Move Final** (PISTON) | 11 | targetSet, groupMoveConfirmed |
| (6,7) | 52 | **Anchors** (NAME_TAG) | -1 | always |
| (6,8) | 53 | **Scroller** (COMPASS) | -1 | always |

No conflicts. Slot 53 is free for the Scroller (Profiles moved to slot 45).
Slot 51 hosts six mutually-exclusive items separated by condition and priority.
Slots 48/49/50 swap between NORMAL/EDITING/group-mode variants. ✅

---

## 2. AnchorListInterface

**Size**: 6 rows (54 slots). **Content area**: rows 1‑5 (slots 0‑44). Content
lists all anchors for the current profile.

| Slot | Index | Item | Condition |
|------|-------|------|-----------|
| (6,1) | 46 | **Back** (BARRIER) -> InventoryInterface | always |
| (6,4) | 49 | **New Anchor** (WRITABLE_BOOK) | always |
| (6,8) | 53 | **Scroller** (COMPASS) | always |

---

## 3. AnchorDetailInterface

**Size**: 6 rows. No content (contentProvider returns null).

| Slot | Index | Item |
|------|-------|------|
| (3,4) | 31 | **Info** (ENDER_PEARL) |
| (3,5) | 32 | **Jump To** (COMPASS) |
| (3,6) | 33 | **Rename** (NAME_TAG) |
| (3,7) | 34 | **Delete** (LAVA_BUCKET) |
| (4,5) | 41 | **Materialize** (ITEM_FRAME) |
| (6,1) | 46 | **Back** (BARRIER) -> AnchorListInterface |
| (6,8) | 53 | **Scroller** (COMPASS) — no content to scroll |

---

## 4. ProfileInterface

**Size**: 6 rows. **Content area**: rows 1‑5. Content lists profiles accessible
to the player.

| Slot | Index | Item | Condition |
|------|-------|------|-----------|
| (6,1) | 46 | **Back** (BARRIER) | always |
| (6,4) | 49 | **New Profile** (WRITABLE_BOOK) | always |
| (6,8) | 53 | **Scroller** (COMPASS) | always |

---

## 5. ProfileDetailInterface

**Size**: 6 rows. No content (contentProvider returns null).

| Slot | Index | Item |
|------|-------|------|
| (2,4) | 22 | **Info** (BOOK) |
| (2,5) | 23 | **Switch To** (ENDER_PEARL) |
| (2,6) | 24 | **Set Default** (BOOKSHELF) |
| (2,7) | 25 | **Rename** (NAME_TAG) |
| (3,4) | 31 | **Openness** (REPEATER) |
| (3,5) | 32 | **Invitations** (PLAYER_HEAD) |
| (3,6) | 33 | **Delete** (LAVA_BUCKET) |
| (6,1) | 46 | **Back** (BARRIER) -> ProfileInterface |
| (6,8) | 53 | **Scroller** (COMPASS) — no content to scroll |

---

## 6. PlayerInviteInterface

**Size**: 6 rows. **Content area**: rows 1‑5. Content lists players with access
to the profile (status cycled on click).

| Slot | Index | Item | Condition |
|------|-------|------|-----------|
| (6,1) | 46 | **Back** (BARRIER, closeInventory) | always |
| (6,8) | 53 | **Scroller** (COMPASS) | always |

---

## Summary

| Interface | Bottom Row Items | Conflicts |
|-----------|-----------------|-----------|
| InventoryInterface | 6 used + 6 conditional | none (Profiles at 45, Scroller at 53) |
| AnchorListInterface | 3 | none |
| AnchorDetailInterface | 2 | none |
| ProfileInterface | 3 | none |
| ProfileDetailInterface | 2 | none |
| PlayerInviteInterface | 2 | none |
