/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package myjogl.gameview;

import com.sun.opengl.util.texture.Texture;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import myjogl.GameEngine;
import myjogl.Global;
import myjogl.utils.Renderer;
import myjogl.utils.ResourceManager;

/**
 *
 * @author Jundat
 */
public class NextLevelView implements GameView {

    Point pBg = new Point(230, 132);
    Point pGame = new Point(230 + 291, 132 + 252);
    Point pOver = new Point(230 + 300, 132 + 165);
    Rectangle rectMenu = new Rectangle(230 + 30, 130, 202, 54);
    Rectangle rectRetry = new Rectangle(230 + 305, 130, 202, 54);
    //
    MenuItem itMenu;
    MenuItem itRetry;
    //
    MainGameView mainGameView;
    Texture ttBg;
    //
    public static long TIME_ANIMATION = 500;
    long time = 0;

    private int menuItemCounter = 0;
    private int MAX_MENU_ITEM_COUNTER = 1;

    public NextLevelView(MainGameView mainGameView) {
        this.mainGameView = mainGameView;
        mainGameView.isPause = true;
    }

    public void keyPressed(KeyEvent e) {
    }

    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_SHIFT) {
            GameEngine.sClick.play();
            switch(menuItemCounter)
            {
                case 0:
                    itMenu.setIsClick(true);
                    GameEngine.sClick.play();
                    //
                    GameEngine.getInst().attach(new MenuView());
                    GameEngine.getInst().detach(mainGameView);
                    GameEngine.getInst().detach(this);
                    break;

                case 1:
                    if (itRetry.isClicked == false) {
                        itRetry.setIsClick(true);
                        GameEngine.sClick.play();
                        //
                        mainGameView.isPause = false;
                        mainGameView.loadLevel(Global.level + 1);
                        GameEngine.getInst().detach(this);
                    }
                    break;
            }
        }       
        
        if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_A) {
            GameEngine.sMouseMove.play(false);
            menuItemCounter--;
            menuItemCounter = (menuItemCounter < 0) ? 0 : menuItemCounter;
        }
        
        if (e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_D) {
            GameEngine.sMouseMove.play(false);
            menuItemCounter++;
            menuItemCounter = (menuItemCounter > MAX_MENU_ITEM_COUNTER) ? MAX_MENU_ITEM_COUNTER : menuItemCounter;
        }
        
        itMenu.setIsOver(false);
        itRetry.setIsOver(false);
        
        switch(menuItemCounter)
        {
            case 0:
                itMenu.setIsOver(true);
                break;
                
            case 1:
                itRetry.setIsOver(true);
                break;
        }
    }

    public void pointerPressed(MouseEvent e) {
        if (itMenu.contains(e.getX(), e.getY())) {
            itMenu.setIsClick(true);
        }

        if (itRetry.contains(e.getX(), e.getY())) {
            itRetry.setIsClick(true);
        }
    }

    public void pointerMoved(MouseEvent e) {
        if (itMenu.contains(e.getX(), e.getY())) {
            if (itMenu.isOver == false) {
                itMenu.setIsOver(true);
                GameEngine.sMouseMove.play(false);
            }
        } else {
            itMenu.setIsOver(false);
        }

        if (itRetry.contains(e.getX(), e.getY())) {
            if (itRetry.isOver == false) {
                itRetry.setIsOver(true);
                GameEngine.sMouseMove.play(false);
            }
        } else {
            itRetry.setIsOver(false);
        }
    }

    public void pointerReleased(MouseEvent e) {
        if (itMenu.contains(e.getX(), e.getY())) { //menu
            itMenu.setIsClick(true);
            GameEngine.sClick.play();
            //
            GameEngine.getInst().attach(new MenuView());
            GameEngine.getInst().detach(mainGameView);
            GameEngine.getInst().detach(this);
        } else if (rectRetry.contains(e.getX(), e.getY())) {
            itRetry.setIsClick(true);
            GameEngine.sClick.play();
            //
            mainGameView.isPause = false;
            mainGameView.loadLevel(Global.level + 1);
            GameEngine.getInst().detach(this);
        }
    }

    public void load() {
        ttBg = ResourceManager.getInst().getTexture("data/common/bg_dialog.png");
        //
        itMenu = new MenuItem(ResourceManager.getInst().getTexture("data/menu/btn.png"),
                ResourceManager.getInst().getTexture("data/menu/btn_press.png"));
        itRetry = new MenuItem(ResourceManager.getInst().getTexture("data/menu/btn.png"),
                ResourceManager.getInst().getTexture("data/menu/btn_press.png"));

        itMenu.SetPosition(rectMenu.x, rectMenu.y);
        itRetry.SetPosition(rectRetry.x, rectRetry.y);
        
        itMenu.setIsOver(true);

        //
        GameEngine.getInst().saveHighscore();
    }

    public void unload() {
        ResourceManager.getInst().deleteTexture("data/common/bg_dialog.png");
    }

    public void update(long elapsedTime) {
        time += elapsedTime;
    }

    public void display() {
        float delta = 1.0f;
        if (time <= TIME_ANIMATION) {
            delta = (float) time / (float) TIME_ANIMATION;
        }

        Renderer.Render(ttBg, pBg.x, pBg.y * delta);
        //
        itMenu.SetPosition(rectMenu.x, (int) (rectMenu.y * delta));
        itRetry.SetPosition(rectRetry.x, (int) (rectRetry.y * delta));
        itMenu.Render();
        itRetry.Render();
        //
        GameEngine.writer.Render("LEVEL", pGame.x + 30, pGame.y * delta, 0.9f, 0.9f);
        GameEngine.writer.Render("COMPLETE", pOver.x - 80 + 30, pOver.y * delta, 0.9f, 0.9f);
        GameEngine.writer.Render("MENU", rectMenu.x + 56, (rectMenu.y + 16) * delta, 0.85f, 0.85f);
        GameEngine.writer.Render("NEXT", rectRetry.x + 57, (rectRetry.y + 16) * delta, 0.85f, 0.85f);
    }
}
