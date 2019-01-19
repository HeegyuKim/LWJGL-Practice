package kr.heegyu;

import static org.lwjgl.opengl.GL11.*;

public class BaseOpenGLApp
implements OpenGLApp
{
    int frameBufferWidth;
    int frameBufferHeight;
    int windowWidth;
    int windowHeight;


    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public void initialize() throws Exception {

    }

    @Override
    public void update(float timeDelta) {

    }

    @Override
    public void render() {

    }

    @Override
    public void cleanUp() {

    }

    @Override
    public void onResize(int width, int height) {
        windowWidth = width;
        windowHeight = height;

        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(0, width, height, 0, -1, 1);
        glMatrixMode(GL_MODELVIEW);
    }

    @Override
    public void onFrameBufferSizeChanged(int width, int height) {
        glViewport(0, 0, width, height);
        frameBufferWidth = width;
        frameBufferHeight = height;
    }


    public int getFrameBufferWidth() {
        return frameBufferWidth;
    }

    public int getFrameBufferHeight() {
        return frameBufferHeight;
    }

    public int getWindowWidth() {
        return windowWidth;
    }

    public int getWindowHeight() {
        return windowHeight;
    }
}
