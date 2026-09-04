# Tables

## Imported Tables

None persisted locally. Player identity is resolved via the rooster player
manager (`ExtendedInventoryPlugin.playerManager`); player ids are stored as
plain integers, not foreign keys.

## Profile (`ei_profiles`)

- id: Int PK (auto)
- name: String (varchar 64)
- createdByPlayerId: Int
- openness: enum (PUBLIC_READ | PUBLIC_WRITE | PRIVATE), default PRIVATE

## Player Profiles (`ei_player_profiles`)

- playerId: Int
- profileId: Int
- status: enum (PRIMARY | WRITE_READ | READ_ONLY), default READ_ONLY

## Inventory (`ei_inventory`)

- id: Int PK (auto)
- profileId: Int
- x: Int
- y: Int
- itemId: Int? (FK -> ei_items)
- anchorId: Int? (FK -> ei_anchors)

## Item (`ei_items`)

- id: Int PK (auto)
- serializedItem: text (base64-encoded Bukkit item bytes)
- materialName: varchar 128

## Anchor (`ei_anchors`)

- id: Int PK (auto)
- profileId: Int
- x: Int
- y: Int
- name: varchar 64

## Buffers

Buffers are not persisted; they live in memory only as a per-player stack in
`BufferManager` with a configurable TTL.
