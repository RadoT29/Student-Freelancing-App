package envLoader;

import envLoader.exceptions.PackageNameNotDefined;
import java.io.FileNotFoundException;
import java.util.HashMap;

/**
 * The <code>envLoader</code> builder class.
 *
 * Example usage:
 *
 * Say you have a `.env` file in your <code>resources</code> folder with the following contents;<br>
 * <br>
 * <code>key=value</code>
 * <br><br>
 * This is how you would read the <code>key</code> variable.
 *
 * <pre>
 *     {@code
 *var tmp = new envLoaderBuilder()
 *    .packageName("experimenting")
 *    .load();
 *
 *tmp.get("key") // "text"
 *tmp.get("test") // null
 *tmp.get("test", "default") // "default"
 *     }
 * </pre>
 */
public class envLoaderBuilder {
    private transient String _filename = ".env";
    private transient String _path = "src/main/resources";
    private transient String _packageName = null;
    private transient boolean _throwFileNotFoundException = false;

    public envLoaderBuilder filename(String filename) {
        this._filename = filename;

        return this;
    }

    /**
     * Sets the path to the file. The default is <code>src/main/resources</code>.
     *
     * @param path The path
     * @return envLoaderBuilder
     */
    public envLoaderBuilder path(String path) {
        this._path = path;

        return this;
    }

    /**
     * Sets the package name. This is required before <code>load()</code> is called.
     *
     * @param packageName The package name of your current project.
     * @return envLoaderBuilder
     */
    public envLoaderBuilder packageName(String packageName) {
        this._packageName = packageName;

        return this;
    }

    /**
     * Makes it so the envLoader will throw an exception if the file is not found instead of
     * returning nulls.
     *
     * @return envLoaderBuilder
     */
    public envLoaderBuilder throwFileNotFoundException() {
        this._throwFileNotFoundException = true;

        return this;
    }

    /**
     * Loads and parses the file.
     *
     * @return envLoader
     * @throws FileNotFoundException If the file cannot be found.
     */
    public envLoader load() throws FileNotFoundException {
        if (_packageName == null) {
            throw new PackageNameNotDefined();
        }

        var parser = new envLoaderParser(_packageName + "/" + _path, _filename);

        try {
            var res = parser.parse();

            return new envLoaderImpl(res);
        } catch (FileNotFoundException e) {
            if (_throwFileNotFoundException)
                throw e;
            else
                return new envLoaderImpl(new HashMap<>());
        }
    }
}
