package com.baidu.scrollstack.uitl;

/**
 * Created by baidu on 16/4/1.
 */

import java.lang.reflect.Method;

import com.baidu.scrollstack.R;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Path;
import android.util.AttributeSet;

import android.util.Log;
import android.view.InflateException;
import android.view.animation.Interpolator;

/**
 * An interpolator that can traverse a Path that extends from <code>Point</code>
 * <code>(0, 0)</code> to <code>(1, 1)</code>. The x coordinate along the <code>Path</code>
 * is the input value and the output is the y coordinate of the line at that point.
 * This means that the Path must conform to a function <code>y = f(x)</code>.
 * <p/>
 * <p>The <code>Path</code> must not have gaps in the x direction and must not
 * loop back on itself such that there can be two points sharing the same x coordinate.
 * It is alright to have a disjoint line in the vertical direction:</p>
 * <p><blockquote><pre>
 *     Path path = new Path();
 *     path.lineTo(0.25f, 0.25f);
 *     path.moveTo(0.25f, 0.5f);
 *     path.lineTo(1f, 1f);
 * </pre></blockquote></p>
 */
public class LocalPathInterpolator extends BaseInterpolator {

    // This governs how accurate the approximation of the Path is.
    private static final float PRECISION = 0.002f;
    //x1 0.0, y1 0.0, x2 0.2, y2 1.0
    private final float[] key_0P4_0P0_0P2_1P0 = new float[] {0.0f, 0.0f, 0.0f, 0.023427581f, 0.035791017f, 0.0028686523f, 0.045366786f, 0.068359375f, 0.011230469f, 0.06661378f, 0.09799805f, 0.024719238f, 0.08787831f, 0.125f, 0.04296875f, 0.109721854f, 0.1496582f, 0.06561279f, 0.13253522f, 0.17226563f, 0.092285156f, 0.18184042f, 0.2125f, 0.15625f, 0.23639746f, 0.24804688f, 0.2319336f, 0.29561847f, 0.28125f, 0.31640625f, 0.42353198f, 0.35000002f, 0.5f, 0.489804f, 0.39023438f, 0.5932617f, 0.55632406f, 0.4375f, 0.68359375f, 0.62268347f, 0.49414063f, 0.7680664f, 0.6558978f, 0.52670896f, 0.80718994f, 0.6892802f, 0.5625f, 0.84375f, 0.7230328f, 0.60180664f, 0.8773804f, 0.75742936f, 0.6449219f, 0.90771484f, 0.79281265f, 0.6921387f, 0.9343872f, 0.8295862f, 0.74375f, 0.95703125f, 0.86820143f, 0.8000488f, 0.97528076f, 0.9091417f, 0.8613281f, 0.98876953f, 0.952907f, 0.9278809f, 0.99713135f, 1.0f, 1.0f, 1.0f};
    private final float[] key_0P4_0P0_1P0_1P0 = new float[] {0.0f, 0.0f, 0.0f, 0.026292618f, 0.038061526f, 0.0028686523f, 0.053826418f, 0.07714844f, 0.011230469f, 0.08288214f, 0.11711426f, 0.024719238f, 0.11360615f, 0.1578125f, 0.04296875f, 0.14604114f, 0.19909668f, 0.06561279f, 0.18015276f, 0.24082032f, 0.092285156f, 0.25298005f, 0.325f, 0.15625f, 0.33095658f, 0.4091797f, 0.2319336f, 0.41253653f, 0.4921875f, 0.31640625f, 0.49595878f, 0.57285154f, 0.40673828f, 0.5793328f, 0.65f, 0.5f, 0.66068685f, 0.7224609f, 0.5932617f, 0.73799545f, 0.7890625f, 0.68359375f, 0.80919707f, 0.8486328f, 0.7680664f, 0.87220454f, 0.9f, 0.84375f, 0.8999783f, 0.9222412f, 0.8773804f, 0.92491275f, 0.94199216f, 0.90771484f, 0.9467427f, 0.95910645f, 0.9343872f, 0.9652022f, 0.9734375f, 0.95703125f, 0.9800248f, 0.98483884f, 0.97528076f, 0.9909436f, 0.99316406f, 0.98876953f, 0.9976912f, 0.9982666f, 0.99713135f, 1.0f, 1.0f, 1.0f};
    private final float[] key_0P0_0P0_0P2_1P0 = new float[] {0.0f, 0.0f, 0.0f, 0.0019146586f, 5.9814454E-4f, 0.0028686523f, 0.00750935f, 0.0024414063f, 0.011230469f, 0.016561627f, 0.0056030275f, 0.024719238f, 0.028851194f, 0.010156251f, 0.04296875f, 0.044160172f, 0.016174316f, 0.06561279f, 0.062273446f, 0.02373047f, 0.092285156f, 0.10606654f, 0.043750003f, 0.15625f, 0.15858112f, 0.07080078f, 0.2319336f, 0.21824203f, 0.10546875f, 0.31640625f, 0.2835738f, 0.14833984f, 0.40673828f, 0.35323417f, 0.2f, 0.5f, 0.42606008f, 0.26103514f, 0.5932617f, 0.50112975f, 0.33203125f, 0.68359375f, 0.57784355f, 0.41357422f, 0.7680664f, 0.6560235f, 0.50625f, 0.84375f, 0.6957733f, 0.5569458f, 0.8773804f, 0.73607063f, 0.6106445f, 0.90771484f, 0.77705646f, 0.66741943f, 0.9343872f, 0.81891257f, 0.72734374f, 0.95703125f, 0.86186063f, 0.79049075f, 0.97528076f, 0.9061593f, 0.8569336f, 0.98876953f, 0.9520997f, 0.9267456f, 0.99713135f, 1.0f, 1.0f, 1.0f};
    private final float[] key_0P0_0P0_0P8_1P0 = new float[] {0.0f, 0.0f, 0.0f, 0.0025773577f, 0.0023010254f, 0.0028686523f, 0.010101028f, 0.009033203f, 0.011230469f, 0.022258457f, 0.019940186f, 0.024719238f, 0.038737163f, 0.034765627f, 0.04296875f, 0.059224747f, 0.053253174f, 0.06561279f, 0.08340891f, 0.07514649f, 0.092285156f, 0.11097745f, 0.10018921f, 0.12261963f, 0.14161831f, 0.12812501f, 0.15625f, 0.21086934f, 0.19165039f, 0.2319336f, 0.28866893f, 0.26367188f, 0.31640625f, 0.37252772f, 0.34213868f, 0.40673828f, 0.4599619f, 0.425f, 0.5f, 0.63565856f, 0.5957031f, 0.68359375f, 0.71902144f, 0.67944336f, 0.7680664f, 0.7961691f, 0.759375f, 0.84375f, 0.86476f, 0.8334473f, 0.90771484f, 0.89515543f, 0.86764526f, 0.9343872f, 0.92260915f, 0.8996094f, 0.95703125f, 0.94690496f, 0.9290832f, 0.97528076f, 0.96788716f, 0.95581055f, 0.98876953f, 0.98551685f, 0.9795349f, 0.99713135f, 1.0f, 1.0f, 1.0f};
    private final float[] key_0P0_0P0_0P35_1P0 = new float[] {0.0f, 0.0f, 0.0f, 0.002035768f, 0.0010238647f, 0.0028686523f, 0.007988239f, 0.0040893555f, 0.011230469f, 0.017626053f, 0.0091873165f, 0.024719238f, 0.030719133f, 0.016308594f, 0.04296875f, 0.04703886f, 0.02544403f, 0.06561279f, 0.06635824f, 0.036584474f, 0.092285156f, 0.11309643f, 0.064843744f, 0.15625f, 0.1691604f, 0.10101318f, 0.2319336f, 0.23282084f, 0.14501953f, 0.31640625f, 0.3024079f, 0.19678955f, 0.40673828f, 0.37633184f, 0.25625f, 0.5f, 0.45311284f, 0.32332763f, 0.5932617f, 0.53142357f, 0.39794922f, 0.68359375f, 0.6101511f, 0.4800415f, 0.7680664f, 0.68848515f, 0.56953126f, 0.84375f, 0.7660398f, 0.66634524f, 0.90771484f, 0.80458397f, 0.7174759f, 0.9343872f, 0.8430646f, 0.7704102f, 0.95703125f, 0.8816234f, 0.82513887f, 0.97528076f, 0.92045635f, 0.88165283f, 0.98876953f, 0.95981425f, 0.93994296f, 0.99713135f, 1.0f, 1.0f, 1.0f};

    private float[] mX; // x coordinates in the line

    private float[] mY; // y coordinates in the line

    /**
     * Create an interpolator for a cubic Bezier curve.  The end points
     * <code>(0, 0)</code> and <code>(1, 1)</code> are assumed.
     *
     * @param controlX1 The x coordinate of the first control point of the cubic Bezier.
     * @param controlY1 The y coordinate of the first control point of the cubic Bezier.
     * @param controlX2 The x coordinate of the second control point of the cubic Bezier.
     * @param controlY2 The y coordinate of the second control point of the cubic Bezier.
     */
    public LocalPathInterpolator(float controlX1, float controlY1, float controlX2, float controlY2) {
        initCubic(controlX1, controlY1, controlX2, controlY2);
    }

    private void initCubic(float x1, float y1, float x2, float y2) {
        Path path = new Path();
        path.moveTo(0, 0);
        path.cubicTo(x1, y1, x2, y2, 1f, 1f);
        String key = ("key" + "_" + x1 + "_" + y1 + "_" + x2 + "_" + y2).replace(".", "P");
        initPath(path, key);
    }

    private void initPath(Path path, String key) {
        float[] pointComponents = new float[] {0, 0, 0, 1, 1, 1};
        switch (key) {
            case "key_0P4_0P0_0P2_1P0":
                pointComponents = key_0P4_0P0_0P2_1P0;
                break;
            case "key_0P4_0P0_1P0_1P0":
                pointComponents = key_0P4_0P0_1P0_1P0;
                break;
            case "key_0P0_0P0_0P2_1P0":
                pointComponents = key_0P0_0P0_0P2_1P0;
                break;
            case "key_0P0_0P0_0P8_1P0":
                pointComponents = key_0P0_0P0_0P8_1P0;
                break;
            case "key_0P0_0P0_0P35_1P0":
                pointComponents = key_0P0_0P0_0P35_1P0;
                break;
            default:
                break;
        }
//        if (key.equals("key_0P0_0P0_0P35_1P0")) {
//            try {
//                Class pathClass = path.getClass();
//                Method method = pathClass.getDeclaredMethod("approximate", new Class[] {float.class});
//                pointComponents = (float[]) method.invoke(path, new Object[] {PRECISION});
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        Log.i("AndroidRuntime", "initPath key " + key);
//        StringBuffer stringBuffer = new StringBuffer();
//        for (int i = 0; i < pointComponents.length; i++) {
//            stringBuffer.append(pointComponents[i] + "f, ");
//        }
//        Log.i("AndroidRuntime", "pointComponents " + stringBuffer.toString());
//        Log.i("AndroidRuntime", "----------------------------------------------------------");
//        }
        int numPoints = pointComponents.length / 3;
        if (pointComponents[1] != 0 || pointComponents[2] != 0
                || pointComponents[pointComponents.length - 2] != 1
                || pointComponents[pointComponents.length - 1] != 1) {
            throw new IllegalArgumentException("!!!!!!!!!!!!!!The Path must start at (0,0) and end at (1,1)");
        }

        mX = new float[numPoints];
        mY = new float[numPoints];
        float prevX = 0;
        float prevFraction = 0;
        int componentIndex = 0;
        for (int i = 0; i < numPoints; i++) {
            float fraction = pointComponents[componentIndex++];
            float x = pointComponents[componentIndex++];
            float y = pointComponents[componentIndex++];
            if (fraction == prevFraction && x != prevX) {
                throw new IllegalArgumentException(
                        "The Path cannot have discontinuity in the X axis.");
            }
            if (x < prevX) {
                throw new IllegalArgumentException("The Path cannot loop back on itself.");
            }
            mX[i] = x;
            mY[i] = y;
            prevX = x;
            prevFraction = fraction;
        }
    }

    /**
     * Using the line in the Path in this interpolator that can be described as
     * <code>y = f(x)</code>, finds the y coordinate of the line given <code>t</code>
     * as the x coordinate. Values less than 0 will always return 0 and values greater
     * than 1 will always return 1.
     *
     * @param t Treated as the x coordinate along the line.
     *
     * @return The y coordinate of the Path along the line where x = <code>t</code>.
     *
     * @see Interpolator#getInterpolation(float)
     */
    @Override
    public float getInterpolation(float t) {
        if (t <= 0) {
            return 0;
        } else if (t >= 1) {
            return 1;
        }
        // Do a binary search for the correct x to interpolate between.
        int startIndex = 0;
        int endIndex = mX.length - 1;

        while (endIndex - startIndex > 1) {
            int midIndex = (startIndex + endIndex) / 2;
            if (t < mX[midIndex]) {
                endIndex = midIndex;
            } else {
                startIndex = midIndex;
            }
        }

        float xRange = mX[endIndex] - mX[startIndex];
        if (xRange == 0) {
            return mY[startIndex];
        }

        float tInRange = t - mX[startIndex];
        float fraction = tInRange / xRange;

        float startY = mY[startIndex];
        float endY = mY[endIndex];
        return startY + (fraction * (endY - startY));
    }
}
