package kr.heegyu.textrender;

import kr.heegyu.utils.IOUtils;
import static org.lwjgl.opengl.GL11.*;

import static org.lwjgl.stb.STBImageWrite.*;
import org.lwjgl.stb.STBImageWrite;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.system.MemoryStack;

import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.stb.STBTruetype.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

public class TextRenderer
implements AutoCloseable
{
    public class FontCharacterInfo {
        int texture;
        int bearingX;
        int bearingY;
        int advance;
        int width;
        int height;

        public FontCharacterInfo(int texture, int bearingX, int bearingY, int advance, int width, int height) {
            this.texture = texture;
            this.bearingX = bearingX;
            this.bearingY = bearingY;
            this.advance = advance;
            this.width = width;
            this.height = height;
        }

        public int getTexture() {
            return texture;
        }

        public int getBearingX() {
            return bearingX;
        }

        public int getBearingY() {
            return bearingY;
        }

        public int getAdvance() {
            return advance;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }
    }

    public static TextRenderer create(File file, float fontHeight)
        throws IOException {
        try(FileInputStream inputStream = new FileInputStream(file)) {
            ByteBuffer buffer = IOUtils.ioResourceToByteBuffer(inputStream, 2 * 1024 * 1024);
            return new TextRenderer(buffer, fontHeight);
        }
    }

    Map<Integer, FontCharacterInfo> charMap = new HashMap<>();
    STBTTFontinfo info;
    float ascent;
    float descent;
    float lineGap;
    float fontHeight;
    float fontScale;

    public TextRenderer(ByteBuffer fontMemory, float fontHeight) {
        this.info = STBTTFontinfo.create();
        this.fontHeight = fontHeight;

        if(!stbtt_InitFont(info, fontMemory)) {
            throw new RuntimeException("Failed to create font file from memory");
        }
        this.fontScale = stbtt_ScaleForPixelHeight(info, fontHeight);

        try(MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer ascentPtr = stack.mallocInt(1);
            IntBuffer descentPtr = stack.mallocInt(1);
            IntBuffer lineGapPtr = stack.mallocInt(1);


            stbtt_GetFontVMetrics(info, ascentPtr, descentPtr, lineGapPtr);

            this.ascent = ascentPtr.get(0) * fontScale;
            this.descent = descentPtr.get(0) * fontScale;
            this.lineGap = lineGapPtr.get(0) * fontScale;

            System.out.printf(
                    "Load Font: fontHeight=%f ascent=%s, descent=%f, lineGap=%f, scale=%f\n",
                    fontHeight, ascent, descent, lineGap, fontScale
            );
        }

        initializeCharacters();
    }

    private void initializeCharacters() {

        int START_CODEPOINT = 1;
        int END_CODEPOINT = 63335;

        glEnable(GL_TEXTURE_2D);
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

        try(MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer x1 = stack.mallocInt(1);
            IntBuffer y1 = stack.mallocInt(1);
            IntBuffer x2 = stack.mallocInt(1);
            IntBuffer y2 = stack.mallocInt(1);

            IntBuffer width = stack.mallocInt(1);
            IntBuffer height = stack.mallocInt(1);
            IntBuffer xoff = stack.mallocInt(1);
            IntBuffer yoff = stack.mallocInt(1);

            IntBuffer advance = stack.mallocInt(1);
            IntBuffer bearingX = stack.mallocInt(1);

            for(int i = START_CODEPOINT; i <= END_CODEPOINT; ++i) {
                int codePoint = i;

                stbtt_GetCodepointBitmapBox(info, i, fontScale, fontScale, x1, y1, x2, y2);
//                System.out.printf("BitmapBox for %c(%d): %d,%d : %d,%d\n",
//                        (char)i, i, x1.get(0), y1.get(0), x2.get(0), y2.get(0)
//                        );

                ByteBuffer bitmap = stbtt_GetCodepointBitmap(info, fontScale, fontScale,
                        i, width, height, xoff, yoff);

                if(bitmap != null) {
                    stbtt_GetCodepointHMetrics(info, i, advance, bearingX);

                    int tex = createTexture(bitmap, i, width.get(0), height.get(0));

                    FontCharacterInfo ch = new FontCharacterInfo(tex,
                            Math.round(bearingX.get(0) * fontScale),
                            -yoff.get(0),
                            Math.round(advance.get(0) * fontScale),
                            width.get(0),
                            height.get(0)
                            );
                    charMap.put(i, ch);

//                    System.out.printf("Bitmap for %c(%d): %d,%d : %d,%d. Buffer Size=%d\n",
//                            (char)i, i, xoff.get(0), yoff.get(0), width.get(0), height.get(0), bitmap.capacity()
//                    );


//                    String outputFilename = String.format("test/char-%c.png", (char)codePoint);
//                    stbi_write_png(outputFilename, width.get(0), height.get(0), 1, bitmap, width.get(0));

                    stbtt_FreeBitmap(bitmap);
                }
                else {
                    System.out.printf("This Font don't has %c(%d)\n", (char)codePoint, codePoint);
                }
            }
        }
        glDisable(GL_TEXTURE_2D);
    }

    private int createTexture(ByteBuffer bitmap, int codePoint, int width, int height) {
        int tex = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, tex);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_ALPHA, width, height, 0, GL_ALPHA, GL_UNSIGNED_BYTE, bitmap);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        return tex;
    }


    @Override
    public void close() throws Exception {
        delete();
    }

    public void delete() {
        for(Map.Entry<Integer, FontCharacterInfo> pair : charMap.entrySet()) {
            FontCharacterInfo ch = pair.getValue();
            glDeleteTextures(ch.texture);
        }
        charMap.clear();
    }



    public FontCharacterInfo getInfo(int codePoint) {
        return charMap.get(codePoint);
    }


    public void beginDraw() {
        glEnable(GL_TEXTURE_2D);
        glEnable(GL_BLEND);
        glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
//        glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE);

        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
    }


    public void draw(String text, int x, int y) {

        int xoff = x;
        int yoff = y;


        for(int i = 0; i < text.length(); ++i) {
            int codePoint = text.codePointAt(i);
            TextRenderer.FontCharacterInfo info = getInfo(codePoint);

            if(codePoint == '\r') continue;
            if(codePoint == '\n') {
                xoff = x;
                yoff += lineGap + fontHeight;
                continue;
            }

            if(info == null) {
//                glPolygonMode( GL_FRONT_AND_BACK, GL_FILL );
//                glDisable(GL_TEXTURE_2D);
//                drawSquare(xoff, yoff - (int)ascent, (int)(ascent + descent), (int)(ascent + descent));
//                glEnable(GL_TEXTURE_2D);
//                xoff += fontHeight;
            }
            else {
//                glPolygonMode( GL_FRONT_AND_BACK, GL_LINE );
//                drawSquare(xoff + info.getBearingX(), yoff - info.getBearingY(), info.getWidth(), info.getHeight());
                glBindTexture(GL_TEXTURE_2D, info.getTexture());
                drawSquare(xoff + info.getBearingX(), yoff - info.getBearingY(), info.getWidth(), info.getHeight());

                xoff += info.getAdvance();
            }

        }
    }

    private void drawSquare(int x, int y, int width, int height) {
        glBegin(GL_QUADS);

        glTexCoord2f(0, 0);
        glVertex3f(x, y, 0);
        glTexCoord2f(1, 0);
        glVertex3f(x + width, y, 0);
        glTexCoord2f(1, 1);
        glVertex3f(x + width, y + height, 0);
        glTexCoord2f(0, 1);
        glVertex3f(x, y + height, 0);

        glEnd();
    }

    public void endDraw() {
        glDisable(GL_TEXTURE_2D);
        glDisable(GL_BLEND);
    }
}
