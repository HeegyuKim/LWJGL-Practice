package kr.heegyu.textrender;

import kr.heegyu.BaseOpenGLApp;
import kr.heegyu.LWJGLApp;
import kr.heegyu.OpenGLApp;

import java.io.File;

import static org.lwjgl.opengl.GL11.*;

public class TextRenderApp
extends BaseOpenGLApp
{
    TextRenderer renderer;

    @Override
    public String getTitle() {
        return "TextRenderApp";
    }

    @Override
    public void initialize() throws Exception {
        glEnable(GL_BLEND);
        glEnable(GL_TEXTURE_2D);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glColor3f(0, 0, 0);

        renderer = TextRenderer.create(
                new File("res/malgun.ttf"),
                64
        );
    }

    @Override
    public void cleanUp() {
        renderer.delete();


    }

    @Override
    public void render() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        String text = "Hello World!!\n왈도??";
        renderer.beginDraw();
        renderer.draw(text, 100, 100);
        renderer.endDraw();
    }

    public static void main(String []args) {
        new LWJGLApp(new TextRenderApp()).run();
    }
}
