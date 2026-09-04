# Extended Inventory — Plugin Spec

A Paper plugin providing a persistent, spatially-organized item storage system optimized for keyboard-macro-driven builder workflows.

---

## Core Concept

The extended inventory is a 2D grid of item slots, identified by integer coordinates `(x, y)` with `(0, 0)` at the center. The grid is sparse and unbounded in all directions. Each coordinate holds one item slot.

The primary interaction model assumes rapid command dispatch via client-side keyboard macros. Mouse-driven use is secondary.

---

## Hotbar Integration

The player's hotbar is a live 9-slot window into the extended inventory's current row.

- **Slot 5** (index 4) is the positional marker — it holds the item at the current position `(x, y)`.
- Slots to the left show `(x-1, y)`, `(x-2, y)`, `(x-3, y)`, `(x-4, y)`.
- Slots to the right show `(x+1, y)`, `(x+2, y)`, `(x+3, y)`, `(x+4, y)`.

The hotbar is a **read-out** of the extended inventory, not a live sync. Edits to the hotbar do not update the extended inventory.

### Navigation

Commands move the current position:

- `up` / `down` — change `y`
- `left` / `right` — change `x`

When navigating to a new position, the current hotbar contents (even if modified) are pushed to the **buffer** (see below), and the hotbar is replaced with the contents loaded from the new position in the extended inventory.

### Hotbar Anchor

The hotbar can be **anchored**, preventing navigation commands from changing the current position. Useful when you want to hold a fixed context while building.

---

## Buffer

When the hotbar is displaced by a navigation event, its contents are saved as a timestamped buffer entry rather than discarded.

- Buffers are stored as a stack/history, each with a creation timestamp.
- **TTL is configurable** — buffer entries expire after a set duration.
- A command allows loading a previous buffer back into the hotbar.

The buffer exists as a safety net and a lightweight clipboard history, not as primary storage.

---

## Interface (Chest GUI)

Opening the GUI displays a chest inventory centered on the player's current `(x, y)` position.

### Modes

**Normal mode** (default): Clicking a slot copies the material to the player's cursor. The extended inventory contents are not modified.

**Edit mode**: Items can be moved in and out of the extended inventory slots freely.

Toggling between modes is done via a command or a designated button in the GUI.

When the GUI is opened, the view is linked to the player's current hotbar position.

---

## Anchor Nodes

An anchor node is a special slot type in the extended inventory. Instead of holding an item, it holds a **named reference to another coordinate**.

- Clicking an anchor node in the GUI (or via a hotbar command) jumps the player's current position to the referenced coordinate.
- Anchor nodes are used to define themed regions — e.g. "flowers", "walls", "stone variants" — and to navigate between them quickly.
- Named anchors also exist as a flat registry (`name → coordinate`) that can be jumped to by name via command.

---

## Material Cycling

A command takes the item in the currently selected hotbar slot and cycles through all other positions in the extended inventory that contain the same material.

- Default traversal order: by grid distance (closest coordinate first).
- Intended use: a material appears in multiple build contexts; spam the command to find the right instance.

---

## Command Summary

| Command | Action |
|---|---|
| `up` / `down` / `left` / `right` | Navigate position in the extended inventory |
| `anchor` | Toggle hotbar anchor on/off |
| `cycle` | Cycle through all instances of the current material |
| `jump <name>` | Jump to a named anchor |
| `buffer` | Load the most recent buffer entry into the hotbar |
| `mode` | Toggle GUI between normal and edit mode |
