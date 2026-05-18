package com.example.whiskerguide.game.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.whiskerguide.common.model.GameState;
import com.example.whiskerguide.common.repository.GameStateHolder;
import com.example.whiskerguide.game.model.Enemy;
import com.example.whiskerguide.game.model.Item;
import com.example.whiskerguide.game.model.ItemType;
import com.example.whiskerguide.game.model.MapData;
import com.example.whiskerguide.game.model.Player;
import com.example.whiskerguide.game.model.Skill;
import com.example.whiskerguide.game.model.SkillType;
import com.example.whiskerguide.game.model.TileType;

import java.util.ArrayList;
import java.util.List;

public class GameViewModel extends ViewModel {

    private static final String LOCATION = "Dark Forest";
    private static final int ENEMY_X = 4;
    private static final int ENEMY_Y = 3;
    private static final int MANA_REGEN_PER_TURN = 5;

    private final MapData mapData;
    private final Player player;
    private boolean enemyDefeated = false;
    private boolean gameOver = false;
    private boolean victory = false;

    private final MutableLiveData<Player> playerLive = new MutableLiveData<>();
    private final MutableLiveData<Enemy> enemyLive = new MutableLiveData<>();
    private final MutableLiveData<String> messageLive = new MutableLiveData<>();

    private final GameStateHolder stateHolder = GameStateHolder.getInstance();

    public GameViewModel() {
        this.mapData = buildDefaultMap();
        this.player = buildDefaultPlayer();
        playerLive.setValue(player);
        enemyLive.setValue(null);
        pushState("Game started");
    }

    public MapData getMapData() { return mapData; }
    public LiveData<Player> getPlayer() { return playerLive; }
    public LiveData<Enemy> getEnemy() { return enemyLive; }
    public LiveData<String> getMessage() { return messageLive; }

    public void moveUp() { tryMove(0, -1); }
    public void moveDown() { tryMove(0, 1); }
    public void moveLeft() { tryMove(-1, 0); }
    public void moveRight() { tryMove(1, 0); }

    private void tryMove(int dx, int dy) {
        if (gameOver || victory) return;
        if (enemyLive.getValue() != null) {
            messageLive.setValue("Cannot move while in combat");
            return;
        }
        int nx = player.getX() + dx;
        int ny = player.getY() + dy;
        if (!mapData.isWalkable(nx, ny)) return;

        if (!enemyDefeated && nx == ENEMY_X && ny == ENEMY_Y) {
            Enemy goblin = new Enemy("goblin", "Goblin", 60, 10);
            enemyLive.setValue(goblin);
            pushState("Encountered a Goblin!");
            messageLive.setValue("⚔ A Goblin blocks your path!");
            return;
        }

        player.setX(nx);
        player.setY(ny);
        playerLive.setValue(player);

        if (mapData.getTile(nx, ny) == TileType.EXIT) {
            victory = true;
            pushState("Reached the exit, level cleared!");
            messageLive.setValue("🏆 You made it through the Dark Forest!");
            return;
        }
        pushState("Moved to (" + nx + ", " + ny + ")");
    }

    public void basicAttack() {
        Enemy e = enemyLive.getValue();
        if (e == null || gameOver) return;
        Skill skill = findSkill("basic_attack");
        if (skill == null) return;

        int dmg = player.getAttackDamage();
        e.setHealth(Math.max(0, e.getHealth() - dmg));
        String event = "Basic Attack dealt " + dmg + " damage to " + e.getName();
        messageLive.setValue(event);

        if (resolveEnemyDeath(event)) return;
        endPlayerTurn(event);
    }

    public void castFireball() {
        Enemy e = enemyLive.getValue();
        if (e == null || gameOver) return;
        Skill fireball = findSkill("fireball");
        if (fireball == null) return;

        if (!fireball.isReady()) {
            messageLive.setValue("Fireball on cooldown (" + fireball.getCurrentCooldown() + " turn(s) left)");
            return;
        }
        if (player.getMana() < fireball.getManaCost()) {
            messageLive.setValue("Not enough mana, need " + fireball.getManaCost());
            return;
        }

        player.setMana(player.getMana() - fireball.getManaCost());
        int dmg = fireball.getValue();
        e.setHealth(Math.max(0, e.getHealth() - dmg));
        fireball.triggerCooldown();
        playerLive.setValue(player);

        String event = "Fireball dealt " + dmg + " damage to " + e.getName();
        messageLive.setValue(event);

        if (resolveEnemyDeath(event)) return;
        endPlayerTurn(event);
    }

    public void usePotion() {
        if (gameOver) return;
        Item potion = null;
        for (Item it : player.getInventory()) {
            if (it.getType() == ItemType.POTION) { potion = it; break; }
        }
        if (potion == null) {
            messageLive.setValue("No potions left in inventory");
            return;
        }
        int healed = Math.min(potion.getValue(), player.getMaxHealth() - player.getHealth());
        player.setHealth(player.getHealth() + healed);
        player.getInventory().remove(potion);
        playerLive.setValue(player);

        String event = "Used " + potion.getName() + ", restored " + healed + " HP";
        messageLive.setValue(event);

        if (enemyLive.getValue() != null) {
            endPlayerTurn(event);
        } else {
            pushState(event);
        }
    }

    private boolean resolveEnemyDeath(String lastEvent) {
        Enemy e = enemyLive.getValue();
        if (e != null && e.isDead()) {
            enemyDefeated = true;
            enemyLive.setValue(null);
            tickEndOfTurn();
            messageLive.setValue("Defeated the " + e.getName() + "!");
            pushState("Defeated the " + e.getName());
            return true;
        }
        return false;
    }

    private void endPlayerTurn(String lastPlayerEvent) {
        Enemy e = enemyLive.getValue();
        if (e == null) return;

        int enemyDmg = e.getAttackDamage();
        player.setHealth(Math.max(0, player.getHealth() - enemyDmg));
        playerLive.setValue(player);

        String combinedEvent = lastPlayerEvent + "; " + e.getName() + " counterattacked for " + enemyDmg + " damage";

        if (player.getHealth() <= 0) {
            gameOver = true;
            enemyLive.setValue(null);
            messageLive.setValue("💀 You were defeated by the " + e.getName());
            pushState("Player died");
            return;
        }

        tickEndOfTurn();
        pushState(combinedEvent);
    }

    private void tickEndOfTurn() {
        for (Skill s : player.getSkills()) s.tickCooldown();
        int newMana = Math.min(player.getMaxMana(), player.getMana() + MANA_REGEN_PER_TURN);
        player.setMana(newMana);
        playerLive.setValue(player);
    }

    private Skill findSkill(String id) {
        for (Skill s : player.getSkills()) {
            if (id.equals(s.getId())) return s;
        }
        return null;
    }

    private void pushState(String lastEvent) {
        Enemy e = enemyLive.getValue();

        List<String> items = new ArrayList<>();
        for (Item it : player.getInventory()) items.add(it.getName());

        // 输出每个技能的真实状态,让 LLM 能直接看到冷却信息。
        List<String> skillsWithStatus = new ArrayList<>();
        for (Skill s : player.getSkills()) {
            String status;
            if (!s.isReady()) {
                status = s.getName() + " (on cooldown, "
                        + s.getCurrentCooldown() + " turn(s) left)";
            } else if (player.getMana() < s.getManaCost()) {
                status = s.getName() + " (not enough mana, needs "
                        + s.getManaCost() + ")";
            } else {
                status = s.getName() + " (ready)";
            }
            skillsWithStatus.add(status);
        }

        GameState state = new GameState(
                player.getHealth(), player.getMaxHealth(),
                player.getMana(), player.getMaxMana(),
                player.getAttackDamage(),
                items,
                skillsWithStatus,
                LOCATION,
                e != null,
                e == null ? null : e.getName(),
                e == null ? 0 : e.getHealth(),
                lastEvent
        );
        stateHolder.update(state);
    }

    private MapData buildDefaultMap() {
        int W = 1, F = 0, E = 2;
        int[][] grid = {
                {W, W, W, W, W, W, W, W},
                {W, F, F, F, F, F, F, W},
                {W, F, W, W, F, W, F, W},
                {W, F, F, F, F, W, F, W},
                {W, W, W, F, W, W, F, W},
                {W, F, F, F, F, F, F, W},
                {W, F, W, W, W, F, E, W},
                {W, W, W, W, W, W, W, W}
        };
        return new MapData(grid);
    }

    private Player buildDefaultPlayer() {
        Player p = new Player(100, 50, 15, 1, 1);

        List<Skill> skills = new ArrayList<>();
        skills.add(new Skill("basic_attack", "Basic Attack", 0, 15, SkillType.ATTACK, 0, "Basic physical attack"));
        skills.add(new Skill("fireball", "Fireball", 30, 40, SkillType.ATTACK, 2, "Fire magic"));
        p.setSkills(skills);

        List<Item> inventory = new ArrayList<>();
        inventory.add(new Item("hp_potion_1", "Health Potion", ItemType.POTION, 30));
        inventory.add(new Item("hp_potion_2", "Health Potion", ItemType.POTION, 30));
        inventory.add(new Item("iron_sword", "Iron Sword", ItemType.WEAPON, 5));
        p.setInventory(inventory);

        return p;
    }
}
