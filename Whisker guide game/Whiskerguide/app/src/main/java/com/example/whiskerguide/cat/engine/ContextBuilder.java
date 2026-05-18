package com.example.whiskerguide.cat.engine;

import com.example.whiskerguide.common.model.GameState;
import com.example.whiskerguide.common.repository.GameStateHolder;
import com.example.whiskerguide.knowledge.model.KnowledgeEntry;
import com.example.whiskerguide.knowledge.repository.KnowledgeRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ContextBuilder {

    private static final String SYSTEM_PROMPT =
            "You are WhiskerGuide, an in-game guide cat. Follow these rules strictly:\n" +
            "1. Only use information from [GAME STATE] and [KNOWLEDGE].\n" +
            "2. Never invent monsters, places, skills, items, or lore that are not in the data.\n" +
            "3. If the info is not enough to answer, just say \"Meow~ I don't know about that.\"\n" +
            "4. Answer in English, 1-2 short sentences.\n" +
            "5. You may end with 'meow~'. Do not repeat characters or phrases.\n" +
            "6. Prefer the strongest skill the player can actually use right now. If Fireball is ready and the question is about beating the Goblin, recommend Fireball — Goblins are weak to fire.\n\n" +
            "Notes on [GAME STATE]:\n" +
            "- The Skills line shows each skill's status in parentheses: \"(ready)\" means usable now, \"(on cooldown, N turn(s) left)\" means must wait, \"(not enough mana, needs N)\" means save mana first.\n" +
            "- Always check skill status before recommending it. Never recommend a skill that is on cooldown.\n\n" +
            "Examples:\n" +
            "- Player: \"How do I beat the Goblin?\"  You: \"Use Fireball — it exploits their fire weakness for 40 damage, meow~\"\n" +
            "- Player: \"How do I kill the goblin?\"  You: \"Cast Fireball, Goblins are weak to fire and it deals 40 damage, meow~\"\n" +
            "- Player: \"Are there Shadow Wolves?\"   You: \"Meow~ I don't know about that.\"\n" +
            "- Player: \"Is fireball ready?\" (Skills shows Fireball (on cooldown, 2 turn(s) left))  You: \"Not yet, Fireball is on cooldown for 2 more turns, meow~\"\n" +
            "- Player: \"Is fireball ready?\" (Skills shows Fireball (ready))  You: \"Yes, Fireball is ready — cast it now, meow~\"";

    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
            "的", "了", "吗", "呢", "啊", "我", "你", "他", "是", "在", "有",
            "and", "the", "is", "are", "what", "how", "why", "do", "i", "you"
    ));

    private final GameStateHolder stateHolder;
    private final KnowledgeRepository knowledgeRepo;

    public ContextBuilder(GameStateHolder stateHolder, KnowledgeRepository knowledgeRepo) {
        this.stateHolder = stateHolder;
        this.knowledgeRepo = knowledgeRepo;
    }

    public String build(String userQuestion) {
        StringBuilder sb = new StringBuilder();

        // Gemma 3 instruct requires this chat-format wrapper.
        // Without it, the model treats the prompt as raw text continuation
        // instead of a Q&A task and ignores the game-state context.
        sb.append("<start_of_turn>user\n");

        sb.append("[SYSTEM]\n").append(SYSTEM_PROMPT).append("\n\n");

        // Inject ALL knowledge entries. The total payload is small (~9 short entries)
        // and guarantees the LLM has full context regardless of user typos or phrasing.
        List<KnowledgeEntry> allEntries = knowledgeRepo.getAll();
        if (allEntries != null && !allEntries.isEmpty()) {
            sb.append("[KNOWLEDGE]\n");
            for (KnowledgeEntry e : allEntries) {
                sb.append("[").append(e.getId()).append("] ")
                        .append(e.getContent()).append("\n");
            }
            sb.append("\n");
        }

        GameState state = stateHolder.getCurrentSnapshot();
        sb.append("[GAME STATE]\n");
        sb.append(formatGameState(state));
        sb.append("\n");

        // Concise status line immediately before the question so a 1B model
        // does not have to search back through long context for the key facts.
        sb.append("[CURRENT STATUS] ");
        if (state != null && state.isInCombat()) {
            sb.append("In combat with ").append(state.getCurrentEnemyName())
              .append(" (HP: ").append(state.getCurrentEnemyHealth()).append("). ");
            String fireballStatus = "unknown";
            for (String skill : state.getAvailableSkills()) {
                if (skill.startsWith("Fireball")) {
                    if (skill.contains("(ready)")) {
                        fireballStatus = "READY — use it now";
                    } else if (skill.contains("on cooldown")) {
                        int turns = extractCooldownTurns(skill);
                        fireballStatus = "ON COOLDOWN (" + turns + " turn(s) left) — do NOT recommend it";
                    } else if (skill.contains("not enough mana")) {
                        fireballStatus = "NOT ENOUGH MANA — do NOT recommend it";
                    }
                    break;
                }
            }
            sb.append("Fireball: ").append(fireballStatus).append(".");
        } else {
            sb.append("Not in combat.");
        }
        sb.append("\n\n");

        sb.append("[PLAYER QUESTION]\n").append(userQuestion).append("\n");
        sb.append("<end_of_turn>\n<start_of_turn>model\n");

        return sb.toString();
    }

    private int extractCooldownTurns(String skillStatus) {
        // parses "Fireball (on cooldown, 2 turn(s) left)" → 2
        try {
            int commaIdx = skillStatus.indexOf(", ");
            int turnIdx = skillStatus.indexOf(" turn", commaIdx);
            if (commaIdx >= 0 && turnIdx > commaIdx) {
                return Integer.parseInt(skillStatus.substring(commaIdx + 2, turnIdx).trim());
            }
        } catch (Exception ignored) {}
        return 0;
    }

    private String formatGameState(GameState s) {
        if (s == null) return "(game not started yet)\n";
        StringBuilder sb = new StringBuilder();
        sb.append("Player HP: ").append(s.getPlayerHealth())
                .append("/").append(s.getPlayerMaxHealth())
                .append(", Mana: ").append(s.getPlayerMana())
                .append("/").append(s.getPlayerMaxMana()).append("\n");
        sb.append("Location: ").append(s.getCurrentLocation()).append("\n");
        if (s.isInCombat()) {
            sb.append("Combat: fighting ").append(s.getCurrentEnemyName())
                    .append(" (enemy HP: ").append(s.getCurrentEnemyHealth()).append(")\n");
        } else {
            sb.append("Combat: not in combat\n");
        }
        if (!s.getAvailableSkills().isEmpty()) {
            sb.append("Skills: ").append(join(s.getAvailableSkills(), ", ")).append("\n");
        }
        if (!s.getInventoryItems().isEmpty()) {
            sb.append("Inventory: ").append(join(s.getInventoryItems(), ", ")).append("\n");
        }
        if (s.getLastEvent() != null) {
            sb.append("Last event: ").append(s.getLastEvent()).append("\n");
        }
        return sb.toString();
    }

    private List<String> extractKeywords(String question) {
        List<String> result = new ArrayList<>();
        if (question == null) return result;

        String cleaned = question
                .replaceAll("[\\p{P}\\p{S}]+", " ")
                .toLowerCase(Locale.ROOT);

        for (String token : cleaned.split("\\s+")) {
            if (token.length() < 1) continue;
            if (STOP_WORDS.contains(token)) continue;
            result.add(token);
        }

        // Also keep the whole question as a fallback keyword
        String trimmed = question.trim();
        if (!trimmed.isEmpty()) {
            result.add(trimmed.toLowerCase(Locale.ROOT));
        }

        return result;
    }

    private String join(List<String> list, String sep) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(sep);
            sb.append(list.get(i));
        }
        return sb.toString();
    }
}
