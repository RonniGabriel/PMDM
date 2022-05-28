package com.juanagui.pmdm;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;

public class PantallaMenu extends ScreenAdapter {

    MainGame game;



    public PantallaMenu(MainGame game) {
        this.game = game;
    }

    @Override
    public void show(){
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keyCode) {
                if (keyCode == Input.Keys.ENTER) {
                    game.setScreen(new PantallaPrincipal(game));

                } else  if (keyCode == Input.Keys.ESCAPE){
                    System.exit(0);
                }
                return true;
            }
        });
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(100,255,0,0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        game.batch.begin();
        game.batch.draw(game.imgFondo,0,-100);
        game.fuenteUno.draw(game.batch, " To start press Enter ", Gdx.graphics.getWidth() * .19f, Gdx.graphics.getHeight() * .25f);
        game.fuenteUno.draw(game.batch, " To exit  press Escape ", Gdx.graphics.getWidth() * .19f, Gdx.graphics.getHeight() * .15f);
        game.batch.end();

    }

    @Override
    public void hide(){
        Gdx.input.setInputProcessor(null);

    }
}