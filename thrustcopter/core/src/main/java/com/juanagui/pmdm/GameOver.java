package com.juanagui.pmdm;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;

public class GameOver extends ScreenAdapter {

    MainGame game;

    public GameOver(MainGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(new InputAdapter() {

            @Override
            public boolean keyDown(int keyCode) {

                if (keyCode == Input.Keys.ENTER) {
                    game.setScreen(new PantallaPrincipal(game));

            }else if  (keyCode == Input.Keys.ESCAPE){
                    System.exit(0);
                }

                return true;
            }

        });
    }
    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(255,0,0,0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.batch.begin();
        game.batch.draw(game.imgFondoEnd,0,0);
        game.fuenteDos.draw(game.batch, "OH! HAS PERDIDO - GAMEOVER!", Gdx.graphics.getWidth() * .19f, Gdx.graphics.getHeight() * .75f);
        game.fuenteDos.draw(game.batch, " To Retry Press ENTER ", Gdx.graphics.getWidth() * .25f, Gdx.graphics.getHeight() * .25f);
        game.fuenteDos.draw(game.batch, " To Exit Press ESCAPE ", Gdx.graphics.getWidth() * .25f, Gdx.graphics.getHeight() * 0.15f);
        game.batch.end();
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }
}