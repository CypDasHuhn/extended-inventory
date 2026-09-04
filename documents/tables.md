# Tables

## Imported Tables

- Player from PlayerManager

## Profile

- id: Int PK
- name: String
- createdByPlayerId: Int FK
- opennessId: Int

## Profile Openness

- id: Int PK

(VALUES: PUBLIC-READ, PUBLIC-WRITE, PRIVATE)

## Player Profiles

- playerId: Int FK
- profileId: Int
- statusId: Int FK

## Player Profile Status

- id: Int PK

(VALUES: PRIMARY, WRITE-READ, READ-ONLY)

## Inventory

- PK (profileId, x, y)
- profileId
- x
- y
- itemId: Int? FK
- anchorId Int? FK

## Item

- id: Int PK
- materialId: Int FK
- nbt: String?

## Materials

- id: Int PK
- name: String

## Anchor

- id: Int PK
- profileId: Int
- x: Int
- y: Int
- name: String

## Buffers

Buffers are not persisted, they live in memory only.
