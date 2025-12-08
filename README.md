# Streaks Plugin

A RuneLite plugin that tracks streak-based activities in Old School RuneScape, including:

- **Pickpocketing**
- **Farming harvest**
  - Herb patches
  - Hops patches
  - Giant Seaweed
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

### Farming Streaks
Supports:
- **Herb patches**
- **Hops patches**
- **Giant Seaweed**
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

### Best Streak Screenshots
By default, when a new best streak is achieved the plugin will take a screenshot of the game area and save it in the folder .runelite/streaks/

This can be disabled in the configuration if desired.

---

### Inactivity Timeout (30 seconds)
Every valid action resets a 30-second timer.  
If no action occurs for 30 seconds, the streak ends automatically.

---

### Overlays

#### **Streak Tracker Overlay**
<img width="195" height="71" alt="521955072-00b18bcf-95d8-4986-8530-b66ffac38f3a" src="https://github.com/user-attachments/assets/77305061-36f6-4068-a875-01df9652e4da" />

Shows:
- Skill + target name  
- Current streak  
- Timer until streak expires  

The width adjusts dynamically to avoid text wrapping.

#### **Celebration Overlay**
<img width="1087" height="584" alt="520521042-c44abb23-292b-41d0-baf3-440b3b9c4b7a" src="https://github.com/user-attachments/assets/e12684ad-5820-45ae-8afb-f4d7434da61d" />

When a new personal best is achieved:
- A “NEW BEST!” popup appears
- A radial burst of confetti animates behind it
- The popup fades away over ~5 seconds

---

### Side Panel

<img width="243" height="408" alt="521953767-d12295f6-dbd4-41c0-9017-3fd8117395df" src="https://github.com/user-attachments/assets/3de82ef6-50b3-4f7c-b680-d83c00d2aaf6" />

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
