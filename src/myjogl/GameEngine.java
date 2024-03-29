package myjogl;

import myjogl.gameview.*;

import java.awt.event.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import javax.media.opengl.GLAutoDrawable;
import javax.swing.text.View;

import myjogl.gameview.IntroView;
import myjogl.tank3d.Tank3D;
import myjogl.utils.ResourceManager;
import myjogl.utils.Sound;
import myjogl.utils.Writer;

/**
 * GameEngine class provides a basic tank3d engine.
 */
public class GameEngine implements KeyListener, MouseListener, MouseMotionListener, MouseWheelListener {

    public boolean paused;
    public int volume = 3; // 1, 2, 3, 4, 5
    public Tank3D tank3d;
    public ArrayList<GameView> views;
    public long localTime = System.currentTimeMillis();
    private ArrayList<GameView> listOldView = null; //detaching view
    boolean hasOldView = false;
    GameView newView = null; //attaching view
    boolean hasNewView = false;
    private static GameEngine instance = null;
    public static Writer writer;
    public static Sound sClick;
    public static Sound sMouseMove;
    public static Sound sFire;
    public static Sound sExplode;
    public static Sound sGameOver;
    
    
    private GameEngine() {
    }

    public static GameEngine getInstance() {
        if (instance == null) {
            instance = new GameEngine();
        }

        return instance;
    }

    public void init(Tank3D tank3d) {
        this.tank3d = tank3d;
        this.paused = false;
        this.views = new ArrayList<GameView>();
        this.listOldView = new ArrayList<GameView>();

        tank3d.canvas.addKeyListener(KeyboardState.getState());
        tank3d.frame.setTitle("Battle City");
        //--------
        this.attach(new IntroView());
    }

    public void loadResource(GLAutoDrawable drawable) {
        writer = new Writer("data/font/Motorwerk_80.fnt");
        //
        this.loadHighscore();
        //
        //sound
        sClick = ResourceManager.getInst().getSound("sound/click.wav", false);
        sMouseMove = ResourceManager.getInst().getSound("sound/mouse_move.wav", false);
        sFire = ResourceManager.getInst().getSound("sound/fire.wav", false);
        sExplode = ResourceManager.getInst().getSound("sound/explode.wav", false);
        sGameOver = ResourceManager.getInst().getSound("sound/game_over.wav", false);
    }

    public void loadHighscore() {
        String path = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
        int index = path.lastIndexOf("/");
        path = path.substring(0, index + 1) + Global.highscorefile;

        File file = new File(path);
        if (file.exists()) {
            try {
                Scanner scn = new Scanner(file);
                //
                Global.level = scn.nextInt();
                Global.playerScore = scn.nextInt();
                //
                scn.close();
            } catch (Exception ex) {
                //System.out.println("Can not load highscore!\n" + ex.getMessage());
                Global.level = 1;
                Global.playerScore = 0;
            }
        } else {
            Global.level = 1;
            Global.playerScore = 0;
        }
    }

    public void saveHighscore() {
        String path = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
        int index = path.lastIndexOf("/");
        path = path.substring(0, index + 1) + Global.highscorefile;

        File file = new File(path);
        if (file.exists() == false) {
            try {
                file.createNewFile();
                FileWriter fw = new FileWriter(file);
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write(Global.level); //level
                bw.write(Global.playerScore); //level
                bw.flush();
                bw.close();
            } catch (IOException ex) {
            }
        }
    }

    public void resume() {
        if (paused) {
            this.paused = false;
        }
    }

    public void pause() {
        if (!paused) {
            this.paused = true;
        }
    }

    public void attach(GameView view) {
        if (!this.views.contains(view)) {
            newView = view;
            hasNewView = true;
        }
    }

    public void detach(GameView view) {
        if (this.views.contains(view)) {
            listOldView.add(view);
            hasOldView = true;
        }
    }

    public void detachTopDialog() {
    	if (!this.views.isEmpty()) {
    		GameView view = (GameView) this.views.get(this.views.size() - 1);
    		
    		if(view.getClass().getName() == "myjogl.gameview.PauseView" ||
    				view.getClass().getName() == "myjogl.gameview.LostGameView" || 
    				view.getClass().getName() == "myjogl.gameview.GameOverView") {
	            this.detach(view);
    		}
        }
    }
    
    public void detachAll() {
    	paused = true;
    	
    	for (Object o : this.views) {
            GameView view = (GameView) o;
            view.unload();
        }
    	
    	this.views.clear();
    }
    
    public void display() {
        for (Object o : this.views) {
            GameView view = (GameView) o;
            view.display();
        }
    }

    public void update(long time) {
        for (Object o : this.views) {
            GameView view = (GameView) o;
            view.update(time);
        }
        
        //System.out.println("FPS: " + 1000.0f/time);
    }

    public void run(GLAutoDrawable drawable) {

        //always detach first
        if (hasOldView) {
            for (Object o : listOldView) {
                this.views.remove(o);
                ((GameView) o).unload();
            }

            hasOldView = false;
            this.listOldView.clear();

            System.gc();
            Runtime.getRuntime().gc();
        }

        ResourceManager.getInst().PreUnload();

        if (hasNewView) {
            newView.load();
            this.views.add(newView);
            hasNewView = false;
        }

        ResourceManager.getInst().PreLoad();

        long currentTime = System.currentTimeMillis();
        {
            this.update(currentTime - localTime);
            this.display();

            ////System.out.println("FPS: " + (float)1000 / (currentTime - localTime));
            //tank3d.frame.setTitle("FPS: " + (float) 1000 / (currentTime - localTime));

            localTime = currentTime;
        }
    }

    public void exit() {
        this.tank3d.exit();
    }

    /////////////////////// ENVENT HANDLER /////////////////////////////////////
    public void keyTyped(KeyEvent e) {
    }

    //
    public void keyPressed(KeyEvent e) {
        if (!this.views.isEmpty()) {
            GameView view = (GameView) this.views.get(this.views.size() - 1);
            view.keyPressed(e);
        }
    }

    //
    public void keyReleased(KeyEvent e) {
        if (!this.views.isEmpty()) {
            GameView view = (GameView) this.views.get(this.views.size() - 1);
            view.keyReleased(e);
        }
    }

    //
    public void mouseClicked(MouseEvent e) {
    }

    /**
     * Translated to OpenGL coordinate
     *
     * @param e
     */
    public void mousePressed(MouseEvent e) {
        if (!this.views.isEmpty()) {
            GameView view = (GameView) this.views.get(this.views.size() - 1);
//            int dy = Global.wndHeight - 2 * e.getY();
//            e.translatePoint(0, dy);
//            view.pointerPressed(e);
            
            MouseEvent newEvent = new MouseEvent(
                tank3d.canvas,
                e.getID(), 
                e.getWhen(), 
                e.getModifiers(), 
                e.getX(),
                Global.wndHeight - e.getY(), 
                e.getClickCount(), 
                e.isPopupTrigger(),
                e.getButton());
            
            view.pointerPressed(newEvent);
        }
    }

    /**
     * Translated to OpenGL coordinate
     *
     * @param e
     */
    public void mouseReleased(MouseEvent e) {
        if (!this.views.isEmpty()) {
            GameView view = (GameView) this.views.get(this.views.size() - 1);
//            int dy = Global.wndHeight - 2 * e.getY();
//            e.translatePoint(0, dy);
//            view.pointerReleased(e);
            
            MouseEvent newEvent = new MouseEvent(
                tank3d.canvas,
                e.getID(), 
                e.getWhen(), 
                e.getModifiers(), 
                e.getX(),
                Global.wndHeight - e.getY(), 
                e.getClickCount(), 
                e.isPopupTrigger(),
                e.getButton());
            
            view.pointerReleased(newEvent);            
        }
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseDragged(MouseEvent e) {
    }

    /**
     * Translated to OpenGL coordinate
     *
     * @param e
     */
    public void mouseMoved(MouseEvent e) {
        if (!this.views.isEmpty()) {
            GameView view = (GameView) this.views.get(this.views.size() - 1);
//            int dy = Global.wndHeight - 2 * e.getY();
//            e.translatePoint(0, dy);
//            view.pointerMoved(e);
            
            MouseEvent newEvent = new MouseEvent(
                    tank3d.canvas,
                    e.getID(), 
                    e.getWhen(), 
                    e.getModifiers(), 
                    e.getX(),
                    Global.wndHeight - e.getY(), 
                    e.getClickCount(), 
                    e.isPopupTrigger(),
                    e.getButton());
            view.pointerMoved(newEvent);
        }
    }

    public void mouseWheelMoved(MouseWheelEvent e) {
    }
}
