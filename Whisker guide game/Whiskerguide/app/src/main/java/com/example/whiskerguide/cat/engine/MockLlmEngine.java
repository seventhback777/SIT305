package com.example.whiskerguide.cat.engine;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.util.Locale;

/**
 * Deterministic keyword-matching fallback engine.
 * Reads real values from the [GAME STATE] section injected by ContextBuilder
 * (labels: "Player HP", "Mana", "Inventory", "Ready skills", "Combat", "Location").
 * Branch order: echo-defense -> meta -> anti-hallucination -> knowledge -> state -> combat advice -> fallback.
 */
public class MockLlmEngine implements LlmEngine {

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    public void initialize(Context context, InitCallback callback) {
        mainHandler.postDelayed(() -> {
            if (callback != null) callback.onSuccess();
        }, 100);
    }

    @Override
    public void generate(String prompt, LlmCallback callback) {
        if (callback == null) return;
        String reply = pickReply(prompt);
        mainHandler.postDelayed(() -> callback.onResult(reply), 600);
    }

    @Override
    public void close() {
        mainHandler.removeCallbacksAndMessages(null);
    }

    // -------- main dispatch --------

    private String pickReply(String prompt) {
        if (prompt == null) prompt = "";
        String gameState = extractSection(prompt, "[GAME STATE]");
        String rawQuestion = extractSection(prompt, "[PLAYER QUESTION]");
        String q = rawQuestion.toLowerCase(Locale.ROOT);

        boolean inCombat = gameState.contains("Combat: fighting");
        // Skills 行格式:"Skills: Basic Attack (ready), Fireball (on cooldown, 2 turn(s) left)"
        boolean fireballReady = skillIsReady(gameState, "Fireball");
        int curHp = parseStat(gameState, "Player HP");
        int maxHp = parseStatMax(gameState, "Player HP");
        int curMana = parseStat(gameState, "Mana");
        int maxMana = parseStatMax(gameState, "Mana");
        boolean lowHp = maxHp > 0 && curHp * 3 < maxHp;
        int potionCount = countInInventory(gameState, "Health Potion");

        // ===== 1. echo / pushback defense =====
        if (hasAny(q, "are you sure", "really", "you sure", "确定", "真的")) {
            if (inCombat) {
                return "Yes, I'm reading your current state. Goblin has 60 HP and is weak to fire — trust me, meow~";
            }
            return "Yes, I base this on the knowledge entries and your current state, not guesses, meow~";
        }

        // ===== 2. meta questions (demo highlights) =====
        if (hasAny(q, "who are you", "what are you", "your name", "introduce", "hello", "hi", "你是谁")) {
            return "Meow~ I'm WhiskerGuide, your on-device AI guide! Ask me about Goblins, Fireball, potions, the exit, or combat tips.";
        }
        if (hasAny(q, "how do you work", "what model", "how are you built", "what ai", "llm", "model", "原理")) {
            return "I run a small on-device model with a keyword fallback layer, fully offline, knowledge injected into the prompt, meow~";
        }
        if (hasAny(q, "what can i ask", "help", "hint", "what can you do", "capabilities")) {
            return "You can ask me about enemy weaknesses, skill usage, items, the exit, or what to do right now, meow~";
        }
        if (hasAny(q, "thanks", "thank you", "thx", "bye", "goodbye")) {
            return "You're welcome, meow~ Call me anytime!";
        }

        // ===== 3. anti-hallucination: things that do NOT exist =====
        if (hasAny(q, "shadow wolf", "wolf", "dragon", "boss", "skeleton", "orc", "slime", "demon king")) {
            return "This demo only has Goblins as enemies — no other monsters yet, meow~";
        }
        if (hasAny(q, "level up", "experience", "exp", "skill tree", "upgrade")) {
            return "No leveling system in this demo, meow~ Player stats are fixed.";
        }
        if (hasAny(q, "shop", "buy", "sell", "merchant", "gold", "npc")) {
            return "No shops or NPCs in this demo. Items are given at the start, meow~";
        }
        if (hasAny(q, "save", "load game", "checkpoint")) {
            return "Save/load isn't supported in this demo, the level is short, meow~";
        }
        if (hasAny(q, "flee", "run away", "escape", "retreat")) {
            return "You can't flee from combat, you have to fight through it, meow~";
        }
        if (hasAny(q, "multiplayer", "co-op", "online", "friend")) {
            return "Single-player offline demo only, no multiplayer, meow~";
        }

        // ===== 4. knowledge base =====
        boolean askingAboutGoblin = hasAny(q, "goblin", "哥布林", "golblin", "goblins");
        boolean askingToKill = hasAny(q, "kill", "beat", "defeat", "fight");
        if (askingAboutGoblin || (inCombat && askingToKill)) {
            if (fireballReady) {
                return "Cast Fireball — Goblins are weak to fire and it deals 40 damage, meow~";
            }
            return "Goblin has 60 HP and is weak to fire. Use Basic Attack now and save Fireball — it deals 40 damage once off cooldown, meow~";
        }
        if (hasAny(q, "weakness", "weak to", "fear", "vulnerable")) {
            return "Goblins are weak to fire — Fireball is the most effective spell on them, meow~";
        }
        if (hasAny(q, "fireball", "fire spell", "火球")) {
            if (!fireballReady && inCombat) {
                return "Fireball is on cooldown or you're low on mana — use Basic Attack to stall a turn, meow~";
            }
            return "Fireball costs 30 mana, deals 40 damage, 2-turn cooldown. It's your key skill against the Goblin, meow~";
        }
        if (hasAny(q, "basic attack", "普攻", "normal attack")) {
            return "Basic Attack costs no mana and has no cooldown. Every turn you can swing it, damage equals your Attack stat, meow~";
        }
        if (hasAny(q, "iron sword", "sword", "weapon", "铁剑")) {
            return "Iron Sword is your starter weapon, +5 Attack. It powers your Basic Attack, meow~";
        }
        if (hasAny(q, "potion", "heal", "health potion", "药水")) {
            if (potionCount >= 0) {
                return "Health Potion restores 30 HP. You currently have " + potionCount + " in your bag — drink one when HP is below half, meow~";
            }
            return "Health Potion restores 30 HP. Using it in combat takes a turn, drink when HP is below half, meow~";
        }
        if (hasAny(q, "exit", "way out", "where to go", "map", "终点")) {
            return "The exit is the glowing tile in the bottom-right corner — step on it to clear the level, meow~";
        }
        if (hasAny(q, "quest", "objective", "goal", "what to do", "任务")) {
            return "Your quest is to cross the Dark Forest and reach the exit. Goblins block the way and must be defeated, meow~";
        }
        if (hasAny(q, "combat", "battle", "turn", "战斗")) {
            return "Combat is turn-based: you act first (Basic Attack / skill / item), then the enemy counterattacks. Cooldowns drop and mana regens after each turn, meow~";
        }
        if (hasAny(q, "mana", "mp", "magic point", "法力")) {
            if (curMana >= 0 && maxMana > 0) {
                return "Mana caps at 50, regens 5 per turn. You're at " + curMana + "/" + maxMana + " now — enough for a Fireball? Check, meow~";
            }
            return "Mana caps at 50 and regens 5 per turn. Save it for Fireball, meow~";
        }
        if (hasAny(q, "move", "movement", "walk", "direction", "移动")) {
            return "Use the direction buttons to move one tile. Dark tiles are walls, the glowing tile is the exit, meow~";
        }

        // ===== 5. state queries =====
        if (hasAny(q, "hp", "my health", "how much health", "my hp", "血量")) {
            if (curHp >= 0 && maxHp > 0) {
                return "You're at " + curHp + "/" + maxHp + " HP" + (lowHp ? " — dangerously low, consider a potion, meow~" : ", looking good, meow~");
            }
        }
        if (hasAny(q, "where am i", "location", "where")) {
            return "You're in the Dark Forest, heading toward the glowing exit in the bottom-right, meow~";
        }
        if (hasAny(q, "inventory", "bag", "what items", "what do i have", "背包")) {
            if (potionCount >= 0) {
                return "You have " + potionCount + " Health Potion(s) and an Iron Sword (equipped), meow~";
            }
        }
        if (hasAny(q, "dead", "die", "game over", "killed")) {
            return "If you fall, the level restarts — so drink a potion when HP gets low, don't tank too long, meow~";
        }

        // ===== 6. combat advice =====
        if (hasAny(q, "what should i do", "what to do", "advice", "tip", "suggest", "how", "怎么办")) {
            if (inCombat) {
                if (lowHp && potionCount > 0) return "HP is critical! Drink a potion now — you still have " + potionCount + " left, meow~";
                if (lowHp) return "HP is low and no potions left — burn a Fireball to end this fast, meow~";
                if (fireballReady) return "Fireball is ready — cast it now! Goblins are weak to fire, 40 damage, meow~";
                return "Goblin is weak to fire — use Basic Attack now, then unleash Fireball for 40 damage once it's off cooldown, meow~";
            }
            return "Not in combat right now — explore the map for the exit. Watch out for Goblins, meow~";
        }

        // ===== 7. fallback =====
        return "Meow~ Ask me about Goblins, Fireball, Basic Attack, potions, the exit, your quest, or what to do right now.";
    }

    // -------- helpers --------

    private boolean hasAny(String q, String... needles) {
        for (String n : needles) if (q.contains(n.toLowerCase(Locale.ROOT))) return true;
        return false;
    }

    private String extractSection(String prompt, String tag) {
        int start = prompt.indexOf(tag);
        if (start < 0) return "";
        start += tag.length();
        int end = prompt.indexOf('[', start);
        return (end < 0 ? prompt.substring(start) : prompt.substring(start, end)).trim();
    }

    /**
     * Looks for "Skills: ..." line and checks if the given skill is marked "(ready)".
     * Returns false if the skill is on cooldown, lacks mana, or is missing entirely.
     */
    private boolean skillIsReady(String section, String skillName) {
        for (String line : section.split("\\n")) {
            String trimmed = line.trim();
            if (!trimmed.startsWith("Skills")) continue;
            int idx = trimmed.indexOf(skillName);
            if (idx < 0) return false;
            // Look at the parenthesized status right after the skill name.
            int open = trimmed.indexOf('(', idx);
            int close = trimmed.indexOf(')', open);
            if (open < 0 || close < 0) return false;
            String status = trimmed.substring(open + 1, close).trim();
            return status.equalsIgnoreCase("ready");
        }
        return false;
    }

    /**
     * Parses the current value after the given label from a line like
     * "Player HP: 75/100, Mana: 50/50". Returns -1 on failure.
     * label may appear anywhere on the line.
     */
    private int parseStat(String gameState, String label) {
        int labelIdx = gameState.indexOf(label);
        if (labelIdx < 0) return -1;
        try {
            int colon = gameState.indexOf(':', labelIdx);
            int slash = gameState.indexOf('/', labelIdx);
            if (colon < 0 || slash < 0 || slash < colon) return -1;
            return Integer.parseInt(gameState.substring(colon + 1, slash).trim());
        } catch (Exception ignored) { return -1; }
    }

    /** Parses the max value after label "X/Y", terminator is comma, space or newline. */
    private int parseStatMax(String gameState, String label) {
        int labelIdx = gameState.indexOf(label);
        if (labelIdx < 0) return -1;
        try {
            int slash = gameState.indexOf('/', labelIdx);
            if (slash < 0) return -1;
            int end = slash + 1;
            while (end < gameState.length()) {
                char c = gameState.charAt(end);
                if (c == ',' || c == ',' || c == '\n' || c == ' ') break;
                end++;
            }
            return Integer.parseInt(gameState.substring(slash + 1, end).trim());
        } catch (Exception ignored) { return -1; }
    }

    /** Counts how many times an item name appears in the "Inventory: ..." line. */
    private int countInInventory(String gameState, String itemName) {
        for (String line : gameState.split("\\n")) {
            String t = line.trim();
            if (t.startsWith("Inventory")) {
                int count = 0, idx = 0;
                while ((idx = t.indexOf(itemName, idx)) != -1) {
                    count++;
                    idx += itemName.length();
                }
                return count;
            }
        }
        return -1;
    }
}
