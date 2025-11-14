package fish.crafting.logviewer.util;

public class ColorUtil {

    public static int alphaCutoff(){
        return 26;
    }
    public static int alphaCutoff(int alpha){
        return alpha < alphaCutoff() ? 0 : alpha;
    }

    public static int alpha(int color, int alpha) {
        if(alpha <= 4) alpha = 4;

        return (color & 0x00FFFFFF) | (alpha << 24);
    }

    public static int red(int color, int red) {
        return (color & 0xFF00FFFF) | (red << 16);
    }

    public static int green(int color, int green) {
        return (color & 0xFFFF00FF) | (green << 8);
    }

    public static int blue(int color, int blue) {
        return (color & 0xFFFFFF00) | (blue);
    }

    public static int getRed(int color) {
        return (color >> 16) & 0xFF;
    }

    public static int getGreen(int color) {
        return (color >> 8) & 0xFF;
    }

    public static int getBlue(int color) {
        return (color >> 0) & 0xFF;
    }

    public static int getAlpha(int color) {
        return (color >> 24) & 0xff;
    }

    public static int mix(int colorA, int colorB, float ratio){
        float iRatio = 1.0f - ratio;

        int aA = (colorA >> 24 & 0xff);
        int aR = ((colorA & 0xff0000) >> 16);
        int aG = ((colorA & 0xff00) >> 8);
        int aB = (colorA & 0xff);

        int bA = (colorB >> 24 & 0xff);
        int bR = ((colorB & 0xff0000) >> 16);
        int bG = ((colorB & 0xff00) >> 8);
        int bB = (colorB & 0xff);

        int a = (int) ((aA * iRatio) + (bA * ratio));
        int r = (int) ((aR * iRatio) + (bR * ratio));
        int g = (int) ((aG * iRatio) + (bG * ratio));
        int b = (int) ((aB * iRatio) + (bB * ratio));

        return a << 24 | r << 16 | g << 8 | b;
    }
}
