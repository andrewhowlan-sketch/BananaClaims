# Banana Claims Development

## Project Information

Project: Banana Claims

Type:
Server-side Fabric Mod

Minecraft:
1.21.6

Fabric Loader:
0.16.x

Fabric API:
0.129+ (26.2)

Development Philosophy

- One logical feature per commit
- Large implementation batches
- Complete replacement Java files
- Zip packages for every update
- Minimize rebuilds
- Everything compiles before moving on
- Everything is tested in-game before the next feature
- Clean architecture
- Vanilla-quality implementation
- Performance first
- Event-driven whenever possible
- Avoid ticking systems unless absolutely necessary

---

# Current Status

## Estimated Completion

Approximately 90%

Renderer V2 is considered feature-complete architecturally and is now moving into polish and optimization.

---

# Completed Systems

## Claims

- Create
- Delete
- Rename
- Description
- Info
- List
- Expand
- Shrink
- Multi-chunk claims
- Area selection
- JSON persistence

---

## Notifications

- Enter detection
- Leave detection
- Action Bar
- Title
- Chat
- Hex colors
- Manual gradients
- Sounds
- Placeholders
- Preview commands

---

## Protection

- Break protection
- Place protection
- Claim flags
- Protection disabled by default

---

## BlueMap

- Runtime detection
- Polygon rendering
- Incremental updates
- Description support
- Member display

---

## Members

- Member model
- Add
- Remove
- List
- Remote commands
- Local commands
- Members bypass protections

---

## Ownership

- Ownership transfer
- Subowner framework

---

## Invitations

- Invite
- Accept
- Deny
- Timeout
- Expiration
- Ownership-safe

---

## Renderer V2

Completed

- Terrain following
- Existing claim preview
- Selection preview
- Automatic preview
- Irregular claim outlines
- Tree filtering
- Ignore logs
- Ignore leaves
- Ground border
- Corner anchors
- Corner columns
- Columns to build limit
- Columns to minimum build height
- Guide columns
- Preview stop
- Direct entity spawning

Current renderer is considered the production renderer.

---

# Current Development

Current milestone

Renderer Configuration System

Current goals

Move every renderer value into configuration.

Create

PreviewV2Config.java

PreviewV2ConfigManager.java

PreviewV2Materials.java

Modify

DisplayPreviewV2Manager.java

Bananaclaims.java

Generate

config/bananaclaims-preview.json

Configuration should include

- Duration
- View range
- Glow color
- Border material
- Border thickness
- Border height
- Terrain offset
- Corner material
- Corner size
- Corner height
- Corner columns
- Guide material
- Guide spacing
- Guide width
- Ignore logs
- Ignore leaves
- Future animation settings

---

# Roadmap

## Renderer

- Renderer Configuration ← CURRENT
- Client-only packet rendering
- Fade animation
- Pulse animation
- Replace legacy particle renderer

---

## Claim System

- Claim leave
- Ownership polish
- Subowner permissions

---

## Protection

- Containers
- Doors
- Trapdoors
- Fence gates
- Buttons
- Levers
- Pressure plates
- Item frames
- Armor stands
- Villagers
- Animals
- Minecarts
- Boats
- PvP
- Mob interaction
- Explosions

---

## Administration

- Admin commands
- Force transfer
- Force delete
- Reload
- Diagnostics

---

## 1.0 Polish

- Config cleanup
- Documentation
- Permissions
- Localization
- BlueMap polish
- Performance optimization

---

## Post-1.0

Book GUI

The Book GUI becomes the primary management interface.

Sections

- Notifications
- Members
- Permissions
- Appearance
- Preview
- Transfer

Preview customization will use the exact same PreviewV2Config system.

---

# Coding Standards

Always

- Think long-term
- Avoid duplicate utilities
- Prefer reusable systems
- Favor composition over duplication
- Never break existing functionality
- Keep classes organized
- Keep commits focused

---

# Testing Workflow

Every commit must include

Files replaced

Build command

```
.\gradlew.bat build
```

In-game testing checklist

Next milestone

No feature moves forward until current milestone passes testing.

---

# Session Handoff

Whenever a new chat is started:

I will upload

- DEVELOPMENT.md
- Any modified Java files

The assistant should read DEVELOPMENT.md first.

The assistant should continue from the current milestone exactly where development stopped.

The assistant should preserve the existing workflow:

- Large implementation batches
- Zip file delivery
- Complete replacement files
- Build instructions
- Test instructions
- Next milestone

Whenever I say

"Create a prompt for a new chat"

the assistant should:

1. Update DEVELOPMENT.md to reflect all completed work.
2. Update the roadmap.
3. Update the current milestone.
4. Generate a new continuation prompt.
5. Assume DEVELOPMENT.md becomes the new source of truth.