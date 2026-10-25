package keystrokesmod.client.ui.gfx;

import java.util.HashMap;
import java.util.Map;

import org.lwjgl.opengl.GL20;

/**
 * A compiled GLSL program. Compiles once; uniform locations are cached. If anything fails the
 * program is marked invalid and callers fall back to the simple renderer (never crashes).
 */
public final class ShaderProgram {
    private final String name;
    private final Map<String, Integer> uniforms = new HashMap<String, Integer>();
    private int program;
    private boolean valid;

    public ShaderProgram(String name, String vertexSource, String fragmentSource) {
        this.name = name;
        int vs = 0;
        int fs = 0;
        try {
            vs = compile(GL20.GL_VERTEX_SHADER, vertexSource);
            fs = compile(GL20.GL_FRAGMENT_SHADER, fragmentSource);
            int p = GL20.glCreateProgram();
            GL20.glAttachShader(p, vs);
            GL20.glAttachShader(p, fs);
            GL20.glLinkProgram(p);
            if (GL20.glGetProgrami(p, GL20.GL_LINK_STATUS) == 0) {
                System.err.println("[PabloClient/ui] shader '" + name + "' failed to link: "
                        + GL20.glGetProgramInfoLog(p, 4096));
                GL20.glDeleteProgram(p);
            } else {
                program = p;
                valid = true;
            }
        } catch (Throwable t) {
            System.err.println("[PabloClient/ui] shader '" + name + "' unavailable: " + t);
            valid = false;
        } finally {
            if (vs != 0) GL20.glDeleteShader(vs);
            if (fs != 0) GL20.glDeleteShader(fs);
        }
    }

    private int compile(int type, String source) {
        int shader = GL20.glCreateShader(type);
        GL20.glShaderSource(shader, source);
        GL20.glCompileShader(shader);
        if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == 0) {
            String log = GL20.glGetShaderInfoLog(shader, 4096);
            GL20.glDeleteShader(shader);
            throw new RuntimeException("compile error in '" + name + "': " + log);
        }
        return shader;
    }

    public boolean isValid() {
        return valid;
    }

    public void use() {
        GL20.glUseProgram(program);
    }

    public static void stop() {
        GL20.glUseProgram(0);
    }

    private int loc(String uniform) {
        Integer cached = uniforms.get(uniform);
        if (cached == null) {
            cached = GL20.glGetUniformLocation(program, uniform);
            uniforms.put(uniform, cached);
        }
        return cached;
    }

    public void set1i(String uniform, int v) {
        GL20.glUniform1i(loc(uniform), v);
    }

    public void set1f(String uniform, float v) {
        GL20.glUniform1f(loc(uniform), v);
    }

    public void set2f(String uniform, float a, float b) {
        GL20.glUniform2f(loc(uniform), a, b);
    }

    public void set4f(String uniform, float a, float b, float c, float d) {
        GL20.glUniform4f(loc(uniform), a, b, c, d);
    }

    /** Sets a vec4 from an ARGB int (0xAARRGGBB). */
    public void setColor(String uniform, int argb) {
        GL20.glUniform4f(loc(uniform),
                ((argb >> 16) & 255) / 255f,
                ((argb >> 8) & 255) / 255f,
                (argb & 255) / 255f,
                ((argb >>> 24) & 255) / 255f);
    }
}
