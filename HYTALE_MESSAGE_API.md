# Hytale Message API Documentation

Based on decompilation of `com.hypixel.hytale.server.core.Message` from HytaleServer.jar

## Key Findings

### 1. ❌ Minecraft § Color Codes DON'T WORK
Hytale uses a completely different system from Minecraft. The `§` color codes you're using (like `§e`, `§a`, `§c`) are NOT supported.

### 2. ✅ Proper Color System
Hytale uses the `color()` method with either:
- **Hex color strings**: `"#FF5733"`, `"#00FF00"`, etc.
- **java.awt.Color objects**: `Color.RED`, `new Color(255, 87, 51)`

### 3. ✅ Clickable Links
Use the `link(String url)` method to make text clickable!

---

## Available Message Methods

### Factory Methods (Static)
```java
Message.raw(String text)           // Plain text message
Message.translation(String key)     // Localized message from translation key
Message.empty()                     // Empty message
Message.parse(String text)          // Parse text with markup
Message.join(Message... messages)   // Join multiple messages
```

### Styling Methods (Chainable)
```java
.color(String hexColor)             // Set color using hex: "#FF5733"
.color(java.awt.Color color)        // Set color using Color object
.bold(boolean)                      // Make text bold
.italic(boolean)                    // Make text italic
.monospace(boolean)                 // Use monospace font
.link(String url)                   // ⭐ Make text clickable with URL!
```

### Content Methods
```java
.insert(Message child)              // Add child message
.insert(String text)                // Add text as child
.insertAll(Message... children)     // Add multiple children
.insertAll(List<Message> children)  // Add list of children
```

### Parameter Methods (for translations)
```java
.param(String key, String value)
.param(String key, boolean value)
.param(String key, double value)
.param(String key, int value)
.param(String key, long value)
.param(String key, float value)
.param(String key, Message value)
```

### Getter Methods
```java
.getRawText()                       // Get raw text
.getMessageId()                     // Get translation key
.getColor()                         // Get hex color
.getChildren()                      // Get child messages
.getAnsiMessage()                   // Get ANSI formatted version (for console)
.getFormattedMessage()              // Get underlying protocol object
```

---

## Corrected Examples

### ❌ Old Way (DOESN'T WORK)
```java
playerRef.sendMessage(Message.raw("§b§lJoin our Discord community!"));
playerRef.sendMessage(Message.raw("§e§n" + DISCORD_LINK));
```

### ✅ New Way (PROPER)
```java
// Bold blue text
Message header = Message.raw("Join our Discord community!")
    .color("#00FFFF")  // Cyan/aqua color
    .bold(true);
playerRef.sendMessage(header);

// Clickable link in yellow/gold
Message discordLink = Message.raw(DISCORD_LINK)
    .color("#FFD700")  // Gold color
    .link(DISCORD_LINK);  // ⭐ Makes it clickable!
playerRef.sendMessage(discordLink);
```

---

## Complete Discord Command Example

```java
package com.veilcore.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;

import javax.annotation.Nonnull;
import java.awt.Color;

public class DiscordCommand extends AbstractPlayerCommand {
    
    private static final String DISCORD_LINK = "https://discord.gg/6SDWFb7ZTD";
    
    public DiscordCommand() {
        super("discord", "Get the Discord server invite link");
    }
    
    @Override
    protected void execute(@Nonnull CommandContext context, ...) {
        // Header line
        Message headerLine = Message.raw("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            .color("#00FFFF")
            .bold(true);
        
        // Title
        Message title = Message.raw("Join our Discord community!")
            .color("#00FFFF")
            .bold(true);
        
        // Instruction text
        Message instruction = Message.raw("Click the link below to join:")
            .color("#AAAAAA");
        
        // Clickable link
        Message link = Message.raw(DISCORD_LINK)
            .color("#FFD700")
            .link(DISCORD_LINK);  // ⭐ THIS MAKES IT CLICKABLE!
        
        // Send messages
        playerRef.sendMessage(headerLine);
        playerRef.sendMessage(Message.empty());
        playerRef.sendMessage(title);
        playerRef.sendMessage(Message.empty());
        playerRef.sendMessage(instruction);
        playerRef.sendMessage(link);
        playerRef.sendMessage(Message.empty());
        playerRef.sendMessage(headerLine);
    }
}
```

---

## Color Reference

Common hex colors to replace Minecraft codes:

| Minecraft | Hex Color | Name |
|-----------|-----------|------|
| §0 | `#000000` | Black |
| §1 | `#0000AA` | Dark Blue |
| §2 | `#00AA00` | Dark Green |
| §3 | `#00AAAA` | Dark Aqua |
| §4 | `#AA0000` | Dark Red |
| §5 | `#AA00AA` | Dark Purple |
| §6 | `#FFAA00` | Gold |
| §7 | `#AAAAAA` | Gray |
| §8 | `#555555` | Dark Gray |
| §9 | `#5555FF` | Blue |
| §a | `#55FF55` | Green |
| §b | `#55FFFF` | Aqua/Cyan |
| §c | `#FF5555` | Red |
| §d | `#FF55FF` | Light Purple |
| §e | `#FFFF55` | Yellow |
| §f | `#FFFFFF` | White |

---

## Advanced Examples

### Example 1: Multi-colored Message
```java
Message msg = Message.raw("Welcome ")
    .color("#55FF55")
    .insert(Message.raw("to the server!")
        .color("#FFD700")
        .bold(true));
playerRef.sendMessage(msg);
```

### Example 2: Clickable Button Style
```java
Message button = Message.raw("[CLICK HERE]")
    .color("#00FF00")
    .bold(true)
    .link("https://example.com");
playerRef.sendMessage(button);
```

### Example 3: Formatted List
```java
Message header = Message.raw("Available Profiles:")
    .color("#00FFFF")
    .bold(true);

Message item1 = Message.raw("• Profile 1")
    .color("#AAAAAA");

Message item2 = Message.raw("• Profile 2")
    .color("#AAAAAA");

playerRef.sendMessage(header);
playerRef.sendMessage(item1);
playerRef.sendMessage(item2);
```

### Example 4: Using java.awt.Color
```java
import java.awt.Color;

Message msg = Message.raw("Custom RGB Color!")
    .color(new Color(255, 87, 51))  // RGB: (255, 87, 51)
    .bold(true);
playerRef.sendMessage(msg);
```

---

## Important Notes

1. **No Underlining**: There's no public `underline()` method. The `underlined` field exists in FormattedMessage but isn't exposed.

2. **Chainable**: All styling methods return `Message`, so you can chain them:
   ```java
   Message.raw("text").color("#FF0000").bold(true).italic(true)
   ```

3. **Parse Method**: `Message.parse()` may support markup syntax, but documentation is not available from decompilation.

4. **Translation Keys**: Use `Message.translation("key")` for localized text that supports multiple languages.

5. **No § Codes**: Remove all `§` formatting codes from your existing messages and replace with proper `color()` calls.

---

## Migration Checklist

- [ ] Replace all `Message.raw("§x...")` with proper `.color()` calls
- [ ] Add `.link(url)` to all URLs to make them clickable
- [ ] Convert Minecraft color codes to hex colors
- [ ] Test clickable links in-game
- [ ] Update all existing commands with new formatting
