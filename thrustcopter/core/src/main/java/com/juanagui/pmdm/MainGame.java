package com.juanagui.pmdm;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class MainGame extends Game {


    SpriteBatch batch;
    ShapeRenderer shapeRenderer;
    Texture imgFondo;
    Texture imgFondoEnd;

    BitmapFont fuenteUno;
    BitmapFont fuenteDos;
    BitmapFont fuenteScore;


    @Override
    public void create () {
        imgFondo = new Texture("welcomeScreen.png");
        imgFondoEnd = new Texture("screenPrincipal.jpg");
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        fuenteScore = new BitmapFont(Gdx.files.internal("FuenteCreada.fnt"));
        fuenteUno =  new BitmapFont(Gdx.files.internal("FuenteCreada.fnt"));
        fuenteScore.getData().setScale(0.5f);
        fuenteDos = new BitmapFont(Gdx.files.internal("FuenteCreada.fnt"));
        fuenteDos.setColor(Color.YELLOW);
        fuenteUno.setColor(Color.MAGENTA);
        fuenteScore.setColor(Color.NAVY);
        setScreen(new PantallaMenu(this));


    }

    @Override
    public void dispose () {
        batch.dispose();
        shapeRenderer.dispose();
        fuenteDos.dispose();
        fuenteUno.dispose();
        imgFondoEnd.dispose();
        imgFondo.dispose();
    }
}
