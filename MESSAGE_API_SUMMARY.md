# Summary: Hytale Message Formatting API

## 🔍 Research Results

I examined the `HytaleServer.jar` file and decompiled the Message class. Here's what I found:

---

## ❌ What DOESN'T Work

**Minecraft § Color Codes** - These do NOT work in Hytale:
- `§a`, `§b`, `§c`, `§e`, `§f`, etc.
- `§l` for bold
- `§n` for underline
- Any `§` formatting codes

Your current code uses these extensively, which is why colors aren't working!

---

## ✅ What DOES Work - The Proper API

### 1. Creating Clickable URLs
```java
Message.raw("Click here!")
    .link("https://discord.gg/6SDWFb7ZTD")
```

### 2. Color System (Hex Colors)
```java
Message.raw("Colored text")
    .color("#FF5555")  // Red
    .color("#55FF55")  // Green
    .color("#55FFFF")  // Aqua/Cyan
    .color("#FFD700")  // Gold
```

### 3. Text Formatting
```java
Message.raw("Bold text").bold(true)
Message.raw("Italic text").italic(true)
Message.raw("Monospace").monospace(true)
```

### 4. Available Methods on Message Class
```java
// Factory methods
Message.raw(String text)
Message.translation(String key)
Message.empty()
Message.parse(String text)
Message.join(Message... messages)

// Styling methods (all chainable)
.color(String hexColor)           // "#FF5555"
.color(java.awt.Color color)      // Color.RED
.bold(boolean)
.italic(boolean)
.monospace(boolean)
.link(String url)                 // Makes text clickable!

// Insertion methods
.insert(Message child)
.insert(String text)
.insertAll(Message... children)

// Parameter methods (for translations)
.param(String key, String/int/float/etc value)

// Getter methods
.getRawText()
.getMessageId()
.getColor()
.getChildren()
```

---

## 📝 Fixed Your DiscordCommand

**Before (BROKEN):**
```java
playerRef.sendMessage(Message.raw("§b§lJoin our Discord community!"));
playerRef.sendMessage(Message.raw("§e§n" + DISCORD_LINK));
```

**After (WORKING):**
```java
Message title = Message.raw("Join our Discord community!")
    .color("#55FFFF")  // Aqua
    .bold(true);

Message link = Message.raw(DISCORD_LINK)
    .color("#FFD700")  // Gold
    .link(DISCORD_LINK);  // Makes it clickable!

playerRef.sendMessage(title);
playerRef.sendMessage(link);
```

---

## 🎨 Color Conversion Chart

| Minecraft Code | Hex Color | Description |
|----------------|-----------|-------------|
| §0 | #000000 | Black |
| §1 | #0000AA | Dark Blue |
| §2 | #00AA00 | Dark Green |
| §3 | #00AAAA | Dark Aqua |
| §4 | #AA0000 | Dark Red |
| §5 | #AA00AA | Dark Purple |
| §6 | #FFAA00 | Gold |
| §7 | #AAAAAA | Gray |
| §8 | #555555 | Dark Gray |
| §9 | #5555FF | Blue |
| §a | #55FF55 | Green |
| §b | #55FFFF | Aqua |
| §c | #FF5555 | Red |
| §d | #FF55FF | Light Purple |
| §e | #FFFF55 | Yellow |
| §f | #FFFFFF | White |

---

## 📦 Files Modified

1. **DiscordCommand.java** - Updated to use proper hex colors and `.link()` method
2. **HYTALE_MESSAGE_API.md** - Complete documentation with examples

---

## 🔧 Files That Still Need Updates

The following files still use `§` codes and need to be updated:

1. **PlayerEventListener.java**
   - Line 58: `§eWelcome! Please create...`
   - Line 70: `§aWelcome back! Loaded profile...`
   - Line 71: `§7Use /profile to switch profiles`
   - Line 77: `§eSelect a profile to continue:`

2. **ProfileCreationPage.java**
   - Lines with `§c` (red), `§a` (green) error messages

3. **ProfileSelectionPage.java**
   - Lines with `§a`, `§c`, `§e` messages

Would you like me to update these files as well?

---

## 🎯 Key Takeaways

1. **Use `.color("#HEX")` instead of `§` codes**
2. **Use `.link(url)` to make URLs clickable**
3. **Chain methods**: `.color("#FF0000").bold(true).link("url")`
4. **No underlining** - not exposed in public API
5. **Use `Message.empty()`** for blank lines instead of `Message.raw("")`
