# Streaks Plugin

A RuneLite plugin that tracks streak-based activities in Old School RuneScape, including:

- **Pickpocketing**
- **Farming harvest**
  - Herb patches  
  - Allotments
  - Bushes
  - Cacti

The plugin records how many successful actions you perform in a row, and saves the best streaks for each target.

When a new best streak is achieved, a popup overlay appears at the top of the screen to celebrate. Congratulations!

---

## Features

### Pickpocketing Streaks
- Tracks consecutive successful pickpockets for each NPC.
- Streak ends when:
  - You fail a pickpocket  
  - You log out or hop  
  - You are inactive for **30 seconds**
- Saves best streaks per NPC.

---

### Farming Streaks
Supports:
- **Herb patches**
- **Allotments**
- **Bushes**
- **Cacti**

Streak increases based on items gained during XP ticks for automatic harvesting actions like herbs and allotments.
For bushes and cacti, the streak will be increased each time a harvest action is made.

Streak ends when:
- The patch is depleted
- You log out or hop  
- You are inactive for **30 seconds**

Best streaks are saved per crop type.

---

### Inactivity Timeout (30 seconds)
Every valid action resets a 30-second timer.  
If no action occurs for 30 seconds, the streak ends automatically.

---

### Overlays

#### **Streak Tracker Overlay**
Shows:
- Skill + target name  
- Current streak  
- Timer until streak expires  

The width adjusts dynamically to avoid text wrapping.

#### **Celebration Overlay**
When a new personal best is achieved:
- A “NEW BEST!” popup appears
- A radial burst of confetti animates behind it
- The popup fades away over ~5 seconds

---

### Side Panel

The plugin panel includes:
- Current activity + streak
- **Collapsible Thieving section**  
- **Collapsible Farming section**  
- Per-entry delete buttons  
- **Reset All** button to clear all saved streaks

Panels update and resize dynamically.

---

## Configuration Storage

All streak data is saved using RuneLite’s config system:

- `thievingBest` – best streaks per NPC  
- `farmingBest` – best streaks per crop  

The data persists across client restarts.

---

## Contributing

Pull requests, feature ideas, and suggestions are welcome.  
Open an issue to propose changes or report bugs.

---

## License

BSD 2-Clause "Simplified" License