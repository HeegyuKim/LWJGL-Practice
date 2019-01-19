package kr.heegyu.image;

import kr.heegyu.BaseOpenGLApp;
import kr.heegyu.LWJGLApp;
import kr.heegyu.OpenGLApp;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;

public class ImageRenderTest
extends BaseOpenGLApp
{

    Texture texture;

    @Override
    public String getTitle() {
        return "ImageRenderTest";
    }

    @Override
    public void initialize() throws Exception {
        glEnable(GL_TEXTURE_2D);

        texture = Texture.fromFile("res/the_death.png");

        glClearColor(1, 0, 0, 1);
//        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable( GL_BLEND );
    }

    @Override
    public void update(float timeDelta) {

    }

    @Override
    public void render() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        glBindTexture(GL_TEXTURE_2D, texture.getId());

        glBegin(GL_QUADS);

        glTexCoord2f(0.0f, 0.0f);
        glVertex2f(0.0f, 0.0f);

        glTexCoord2f(1.0f, 0.0f);
        glVertex2f(getFrameBufferWidth() / 2, 0.0f);

        glTexCoord2f(1.0f, 1.0f);
        glVertex2f(getFrameBufferWidth() / 2, getFrameBufferHeight());

        glTexCoord2f(0.0f, 1.0f);
        glVertex2f(0.0f, getFrameBufferHeight());

        glEnd();
    }

    @Override
    public void cleanUp() {
        texture.delete();
    }

    public static void main(String []args) {
        new LWJGLApp(new ImageRenderTest()).run();
    }
}
