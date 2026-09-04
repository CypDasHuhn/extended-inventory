# Interface

## Main Interface (`InventoryInterface`)

6 rows; content area is rows 1‑5, grounded on the context center `(centerX, centerY)`.
Bottom row holds navigation/action buttons plus the inherited scroller.

### Modes (`InterfaceMode`)

- `NORMAL`
- `EDITING`
- `SETTING_ANCHOR`
- `MATERIALIZING_ANCHOR`
- `GROUP_DELETE_A` / `GROUP_DELETE_B`
- `GROUP_MOVE_A` / `GROUP_MOVE_B` / `GROUP_MOVE_TARGET`

### Normal mode

Clicking a regular item copies it to the cursor without modifying the
inventory. Clicking an anchor item jumps to that anchor's coordinate.

### Edit mode

Items are freely movable: click a filled slot with an empty cursor to pick it
up, or click an empty slot with a filled cursor to place it. Changes are
buffered in the context and resolved on save. A **Save** (slot 48) and
**Discard** (slot 49) button replace the normal actions while editing.
Pending edits are stored as serialized strings so the context survives JSON
persistence.

### Set Anchor mode

The first content slot clicked becomes the anchor position. The interface stays
open while the player types a name in chat; the anchor is created on that slot.

### Materialize Anchor mode

The first content slot clicked produces a materialized anchor item (an
ender pearl with NBT referencing the coordinate). Using the item later jumps to
that coordinate.

### Group Delete

Pick two corners (A then B) via content clicks, then confirm. On confirmation
every item in the region is deleted.

### Group Move

Pick two corners for the source region, then a third slot as the target
corner. On confirmation the region is translated to the target. A green
preview overlays the target region.

## Anchor Interface

### Anchor List

Lists all anchors for the current profile. Clicking an entry opens the anchor
detail interface. A "New Anchor" button creates one at the current position via
chat input.

### Anchor Detail

- Back
- Info
- Jump To
- Rename (chat input)
- Delete
- Materialize (gives a materialized anchor item)

## Profile Interface

### Profile List

Lists profiles accessible to the player. The primary profile is highlighted
(enchanted book). Clicking an entry opens the profile detail interface. A
"New Profile" button creates one via chat input.

### Profile Detail

- Back
- Info
- Switch To (makes it the active profile)
- Set Default
- Rename (chat input)
- Openness (cycles PRIVATE -> PUBLIC_READ -> PUBLIC_WRITE)
- Invitations (opens player invite interface)
- Delete

### Player Invite Interface

Lists players with access to the profile. Clicking a player cycles their
access: READ_ONLY -> WRITE_READ -> READ_ONLY.
