package tankgame;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.FontMetrics;

// hud - heads up display - ui to show score, health, etc
public class HUD {

    private int score;

    public HUD() {
        this.score = 0;
    }

    public void addScore(int points) {
        score += points;
    }

    public int getScore() {
        return score;
    }

    public void draw(Graphics2D g, Tank player) {
        int barHeight = 30;

        // ── Top HUD bar ────────────────────────────────────
        g.setColor(Constants.COLOR_HUD_BG);
        g.fillRect(0, 0, Constants.WINDOW_WIDTH, barHeight);

        g.setFont(Constants.FONT_HUD);
        FontMetrics fm = g.getFontMetrics();

        // Player health bar (left side)
        g.setColor(Constants.COLOR_TEXT);
        g.drawString("HP", 8, 20);

        int hbX = 30;
        int hbY = 10;
        int hbW = 120;
        int hbH = 12;

        g.setColor(Constants.COLOR_HEALTH_LOST);
        g.fillRect(hbX, hbY, hbW, hbH);

        double ratio = (double) player.getHealth() / player.getMaxHealth();
        Color hpColor = ratio > 0.5 ? Constants.COLOR_HEALTH_BAR
                : ratio > 0.2 ? new Color(255, 180, 0) : new Color(255, 50, 50);
        g.setColor(hpColor);
        g.fillRect(hbX, hbY, (int) (hbW * ratio), hbH);

        g.setColor(Constants.COLOR_TEXT);
        g.drawRect(hbX, hbY, hbW, hbH);

        // Score (center)
        String scoreText = "SCORE: " + score;
        int scoreW = fm.stringWidth(scoreText);
        g.setColor(Constants.COLOR_TITLE);
        g.drawString(scoreText, Constants.WINDOW_WIDTH / 2 - scoreW / 2, 20);
    }
}
