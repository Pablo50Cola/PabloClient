package keystrokesmod.client.ui.gfx;

import org.lwjgl.opengl.GLContext;

/**
 * Shader sources (GLSL 1.20, works on any OpenGL 2.1 GPU, which is what Minecraft 1.8.9 targets)
 * and lazily-created programs. Programs are compiled once, on first use, on the render thread.
 */
public final class Shaders {

    private static final String VERTEX =
            "#version 120\n"
          + "void main() {\n"
          + "    gl_TexCoord[0] = gl_MultiTexCoord0;\n"
          + "    gl_Position = ftransform();\n"
          + "}\n";

    private static final String SDF =
            "float sdRoundRect(vec2 p, vec2 halfSize, float r) {\n"
          + "    vec2 q = abs(p) - halfSize + vec2(r);\n"
          + "    return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - r;\n"
          + "}\n";

    /** Anti-aliased rounded rectangle: 4-corner gradient, optional inner border, optional soft shadow. */
    private static final String ROUND_FRAGMENT =
            "#version 120\n"
          + "uniform vec2 quadSize;\n"
          + "uniform vec2 rectSize;\n"
          + "uniform float radius;\n"
          + "uniform float pixel;\n"
          + "uniform float soft;\n"
          + "uniform float hole;\n"
          + "uniform float borderWidth;\n"
          + "uniform vec4 cTL;\n"
          + "uniform vec4 cTR;\n"
          + "uniform vec4 cBL;\n"
          + "uniform vec4 cBR;\n"
          + "uniform vec4 borderColor;\n"
          + SDF
          + "void main() {\n"
          + "    vec2 p = (gl_TexCoord[0].st - 0.5) * quadSize;\n"
          + "    float d = sdRoundRect(p, rectSize * 0.5, radius);\n"
          + "    float cover;\n"
          + "    if (soft > 0.0) {\n"
          + "        cover = 1.0 - smoothstep(-soft, soft, d);\n"
          + "    } else {\n"
          + "        cover = 1.0 - smoothstep(-pixel * 0.5, pixel * 0.5, d);\n"
          + "    }\n"
          + "    if (hole > 0.5) {\n"
          + "        cover *= smoothstep(-pixel * 0.5, pixel * 0.5, d);\n"
          + "    }\n"
          + "    vec2 g = clamp(p / rectSize + 0.5, 0.0, 1.0);\n"
          + "    vec4 fill = mix(mix(cTL, cBL, g.y), mix(cTR, cBR, g.y), g.x);\n"
          + "    if (borderWidth > 0.0) {\n"
          + "        float ring = 1.0 - smoothstep(-pixel * 0.5, pixel * 0.5, abs(d + borderWidth * 0.5) - borderWidth * 0.5);\n"
          + "        float ba = ring * borderColor.a;\n"
          + "        float oa = ba + fill.a * (1.0 - ba);\n"
          + "        vec3 orgb = fill.rgb;\n"
          + "        if (oa > 0.0) {\n"
          + "            orgb = (borderColor.rgb * ba + fill.rgb * fill.a * (1.0 - ba)) / oa;\n"
          + "        }\n"
          + "        fill = vec4(orgb, oa);\n"
          + "    }\n"
          + "    gl_FragColor = vec4(fill.rgb, fill.a * cover);\n"
          + "}\n";

    /** Frosted glass: shows the pre-blurred screen inside a rounded rectangle, tinted, with a hairline border. */
    private static final String GLASS_FRAGMENT =
            "#version 120\n"
          + "uniform sampler2D blurTex;\n"
          + "uniform vec2 rectPos;\n"
          + "uniform vec2 rectSize;\n"
          + "uniform vec2 screen;\n"
          + "uniform float radius;\n"
          + "uniform float pixel;\n"
          + "uniform float saturation;\n"
          + "uniform float opacity;\n"
          + "uniform float borderWidth;\n"
          + "uniform vec4 tint;\n"
          + "uniform vec4 borderColor;\n"
          + SDF
          + "void main() {\n"
          + "    vec2 uv = gl_TexCoord[0].st;\n"
          + "    vec2 p = (uv - 0.5) * rectSize;\n"
          + "    float d = sdRoundRect(p, rectSize * 0.5, radius);\n"
          + "    float cover = 1.0 - smoothstep(-pixel * 0.5, pixel * 0.5, d);\n"
          + "    vec2 gui = rectPos + uv * rectSize;\n"
          + "    vec2 suv = vec2(gui.x / screen.x, 1.0 - gui.y / screen.y);\n"
          + "    vec3 c = texture2D(blurTex, suv).rgb;\n"
          + "    float gray = dot(c, vec3(0.299, 0.587, 0.114));\n"
          + "    c = mix(vec3(gray), c, saturation);\n"
          + "    c = mix(c, tint.rgb, tint.a);\n"
          + "    if (borderWidth > 0.0) {\n"
          + "        float ring = 1.0 - smoothstep(-pixel * 0.5, pixel * 0.5, abs(d + borderWidth * 0.5) - borderWidth * 0.5);\n"
          + "        c = mix(c, borderColor.rgb, ring * borderColor.a);\n"
          + "    }\n"
          + "    gl_FragColor = vec4(c, cover * opacity);\n"
          + "}\n";

    /** 9-tap gaussian blur using 5 linear-filtered fetches. Run once horizontally, once vertically. */
    private static final String BLUR_FRAGMENT =
            "#version 120\n"
          + "uniform sampler2D tex;\n"
          + "uniform vec2 dir;\n"
          + "void main() {\n"
          + "    vec2 uv = gl_TexCoord[0].st;\n"
          + "    vec3 c = texture2D(tex, uv).rgb * 0.2270270270;\n"
          + "    c += texture2D(tex, uv + dir * 1.3846153846).rgb * 0.3162162162;\n"
          + "    c += texture2D(tex, uv - dir * 1.3846153846).rgb * 0.3162162162;\n"
          + "    c += texture2D(tex, uv + dir * 3.2307692308).rgb * 0.0702702703;\n"
          + "    c += texture2D(tex, uv - dir * 3.2307692308).rgb * 0.0702702703;\n"
          + "    gl_FragColor = vec4(c, 1.0);\n"
          + "}\n";

    private static ShaderProgram round;
    private static ShaderProgram glass;
    private static ShaderProgram blur;
    private static boolean roundTried;
    private static boolean glassTried;
    private static boolean blurTried;
    private static Boolean supported;

    private Shaders() {
    }

    public static boolean supported() {
        if (supported == null) {
            boolean ok;
            try {
                ok = GLContext.getCapabilities().OpenGL20;
            } catch (Throwable t) {
                ok = false;
            }
            supported = ok;
            if (!ok) System.err.println("[RavenP/ui] OpenGL 2.0 not available, using the simple renderer.");
        }
        return supported;
    }

    /** @return the rounded-rect program, or null when shaders are unavailable. */
    public static ShaderProgram round() {
        if (!roundTried) {
            roundTried = true;
            if (supported()) round = valid(new ShaderProgram("round", VERTEX, ROUND_FRAGMENT));
        }
        return round;
    }

    public static ShaderProgram glass() {
        if (!glassTried) {
            glassTried = true;
            if (supported()) glass = valid(new ShaderProgram("glass", VERTEX, GLASS_FRAGMENT));
        }
        return glass;
    }

    public static ShaderProgram blur() {
        if (!blurTried) {
            blurTried = true;
            if (supported()) blur = valid(new ShaderProgram("blur", VERTEX, BLUR_FRAGMENT));
        }
        return blur;
    }

    private static ShaderProgram valid(ShaderProgram p) {
        return p.isValid() ? p : null;
    }
}
