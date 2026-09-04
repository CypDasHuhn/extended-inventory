# Interface

## Main Interface

### Context

- ProfileId
- x
- y
- Mode

### Mode

- Normal
- Editing
- SettingAnchor
- MaterializingAnchor

5 rows of content. interface position is grounded in row 3 col 5 (center).
bottom row has 'back', scrollers, and toggle edit mode button. by default edit mode is off.
anchor interface button, to move to anchor interface.
profile interface button, to move to profile interface.

### Normal mode

normal mode, click events on items result in the user getting a copy of the material in the cursor.
the same goes for hotkeys 1-9.

### Edit mode

when edit mode is on, items are freely movable.
when eidt mode is on, a 'save' button appears.
when the scroller is used while edit mode is on, the content of the interface is buffered into the context, and resolved on save.

### Create Anchor mode

The first slot in the content area to be clicked is deemed the anchor position. interface closed, user types in a name for the anchor in the chat, anchor interface opened with new anchor added.

### Materialize Anchor mode

The first slot in the content area to be clicked is deemed the position of the materialized anchor. it is saved as an materialized-anchor. the item has a specific nbt flag, which makes it resolvable when saving items,
making it possible to copy the item afterwards. when the materialized anchor is clicked in normal mode, it jumps you to the new position.

## Anchor interface

Shows you a list of anchors. on click you go to anchor detail interface.

back button, scrollers, new anchor button. On click you move to the main interface with previous context but setting anchor mode.

### Anchor Detail Interface

- back button
- jump-to button
- rename button: needs confirmation and write access, user types in a new name for the anchor in the chat, interface is opened again.
- delete button: needs confirmation and write access
- add-materialized-anchor button: needs write access, moves to extended inventory interface. next click

## Profile Interface

Shows you a list of profiles you have subscribed to. on click you turn to profile detail interface.
the currently selected profile is enchanted.

back button, scrollers, new profile button. on click interface closes, user types in a name for the new profile in the chat, profile interface is opened with new profile added.
Button to turn datasource from subscribed profiles, to accessible-unsubscribed ones. on click you subscribe to that profile.

### Profile Detail Interface

back button.
if you are the creator of the profile:

- delete button: needs confirmation
- rename button: needs confirmation, user types in a new name for the profile in the chat, interface is opened again.
- openness button: State cycler between public-write, public-read, private

if you have write access to the profile:

- invitations button: Moves to player invite interface
- turn to primary button, changes your profile to primary

if you have read access to the profile:

- switch-to button, changes your profile to that one

#### Player invite Interface

list of players, back button. each player can be cycled between uninvited, read-only and full access. You cannot change access of the owner of the profile.
