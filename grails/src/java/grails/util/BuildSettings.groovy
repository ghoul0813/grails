/*
 * Copyright 2008 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package grails.util

import java.util.regex.Pattern

/**
 * <p>This class represents the project paths and other build settings
 * that the user can change when running the Grails commands. Defaults
 * are provided for all settings, but the user can override those by
 * setting the appropriate system property or specifying a value for
 * it in the BuildSettings.groovy file.</p>
 * <p><b>Warning</b> The behaviour is poorly defined if you explicitly
 * set some of the project paths (such as {@link #projectWorkDir }),
 * but not others. If you set one of them explicitly, set all of them
 * to ensure consistent behaviour.</p>
 */
class BuildSettings {
    static final Pattern DEFAULT_DEPS = ~"""\
(ant-\\d|ant-launcher-|antlr-|aopalliance-|backport-util-concurrent-|cglib-|commons-beanutils-|commons-cli-|\
commons-codec-|commons-collections-|commons-dbcp-|commons-fileupload-|commons-io-|commons-lang-|\
commons-pool-|commons-validator-|dom4j-|ehcache-|ejb3-persistence-|groovy-all-|hsqldb-|ivy-|jul-to-slf4j-|jcl-over-slf4j-|jdbc2_0-stdext|\
jsr107cache-|jta-|log4j-|ognl-|org\\.springframework|oro-|\
oscache-|sitemesh-|slf4j-api-|slf4j-log4j12-|xercesImpl-|\
xpp3_min-).*\\.jar"""
    static final Pattern JAR_PATTERN = ~/^\S+\.jar$/


    /**
     * The base directory of the application
     */
    public static final String APP_BASE_DIR = "base.dir"
    /**
     * The name of the system property for {@link #grailsWorkDir}.
     */
    public static final String WORK_DIR = "grails.work.dir"

    /**
     * The name of the system property for {@link #projectWorkDir}.
     */
    public static final String PROJECT_WORK_DIR = "grails.project.work.dir"

    /**
     * The name of the system property for {@link #projectWarExplodedDir}.
     */
    public static final String PROJECT_WAR_EXPLODED_DIR = "grails.project.war.exploded.dir"

    /**
     * The name of the system property for {@link #projectPluginsDir}.
     */
    public static final String PLUGINS_DIR = "grails.project.plugins.dir"

    /**
     * The name of the system property for {@link #globalPluginsDir}.
     */
    public static final String GLOBAL_PLUGINS_DIR = "grails.global.plugins.dir"

    /**
     * The name of the system property for {@link #resourcesDir}.
     */
    public static final String PROJECT_RESOURCES_DIR = "grails.project.resource.dir"

    /**
     * The name of the system property for {@link #classesDir}.
     */
    public static final String PROJECT_CLASSES_DIR = "grails.project.class.dir"

    /**
     * The name of the system property for {@link #testClassesDir}.
     */
    public static final String PROJECT_TEST_CLASSES_DIR = "grails.project.test.class.dir"

    /**
     * The name of the system property for {@link #testReportsDir}.
     */
    public static final String PROJECT_TEST_REPORTS_DIR = "grails.project.test.reports.dir"

    /**
     * The base directory for the build, which is normally the root
     * directory of the current project. If a command is run outside
     * of a project, then this will be the current working directory
     * that the command was launched from.
     */
    File baseDir

    /** Location of the current user's home directory - equivalen to "user.home" system property. */
    File userHome

    /**
     * Location of the Grails distribution as usually identified by
     * the GRAILS_HOME environment variable. This value may be
     * <code>null</code> if GRAILS_HOME is not set, for example if a
     * project uses the Grails JAR files directly.
     */
    File grailsHome

    /** The version of Grails being used for the current script. */
    String grailsVersion

    /** The environment for the current script. */
    String grailsEnv

    /** <code>true</code> if the default environment for a script should be used. */
    boolean defaultEnv

    /** The location of the Grails working directory where non-project-specific temporary files are stored. */
    File grailsWorkDir

    /** The location of the project working directory for project-specific temporary files. */
    File projectWorkDir

    /** The location of the Grails WAR directory where exploded WAR is built. */
    File projectWarExplodedDir

    /** The location to which Grails compiles a project's classes. */
    File classesDir

    /** The location to which Grails compiles a project's test classes. */
    File testClassesDir

    /** The location where Grails keeps temporary copies of a project's resources. */
    File resourcesDir

    /** The location where project-specific plugins are installed to. */
    File projectPluginsDir

    /** The location where global plugins are installed to. */
    File globalPluginsDir

    /** The location of the test reports. */
    File testReportsDir

    /** The root loader for the build. This has the required libraries on the classpath. */
    URLClassLoader rootLoader

    /** The settings stored in the project's BuildConfig.groovy file if there is one. */
    ConfigObject config

    /** Implementation of the "grailsScript()" method used in Grails scripts. */
    Closure grailsScriptClosure;

    /**
     * A Set of plugin names that represent the default set of plugins installed when creating Grails applications
     */
    Set defaultPluginSet

    /**
     * A Set of plugin names and versions that represent the default set of plugins installed when creating Grails applications
     */    
    Map defaultPluginMap

    /** List containing the compile-time dependencies of the app as File instances. */
    List compileDependencies

    /** List containing the test-time dependencies of the app as File instances. */
    List testDependencies

    /** List containing the runtime-time dependencies of the app as File instances. */
    List runtimeDependencies

    /*
     * This is an unclever solution for handling "sticky" values in the
     * project paths, but trying to be clever so far has failed. So, if
     * the values of properties such as "grailsWorkDir" are set explicitly
     * (from outside the class), then they are not overridden by system
     * properties/build config.
     *
     * TODO Sort out this mess. Must decide on what can set this properties,
     * when, and how. Also when and how values can be overridden. This
     * is critically important for the Maven and Ant support.
     */
    private boolean grailsWorkDirSet
    private boolean projectWorkDirSet
    private boolean projectWarExplodedDirSet
    private boolean classesDirSet
    private boolean testClassesDirSet
    private boolean resourcesDirSet
    private boolean projectPluginsDirSet
    private boolean globalPluginsDirSet
    private boolean testReportsDirSet

    private addJars = { File jar ->
        this.compileDependencies << jar
        this.testDependencies << jar
        this.runtimeDependencies << jar
    }

    BuildSettings() {
        this(null)
    }

    BuildSettings(File grailsHome) {
        this(grailsHome, null)
    }

    BuildSettings(File grailsHome, File baseDir) {
        this.userHome = new File(System.getProperty("user.home"))

        if (grailsHome) this.grailsHome = grailsHome

        // Load the 'build.properties' file from the classpath and
        // retrieve the Grails version from it.
        Properties buildProps = new Properties()
        try {
            loadBuildPropertiesFromClasspath(buildProps)
            grailsVersion = buildProps.'grails.version'
        }
        catch (IOException ex) {
            throw new IOException("Unable to find 'build.properties' - make " +
                    "that sure the 'grails-core-*.jar' file is on the classpath.")
        }

        // If 'grailsHome' is set, add the JAR file dependencies.
        this.defaultPluginMap = [hibernate:grailsVersion, tomcat:grailsVersion]
        this.defaultPluginSet = defaultPluginMap.keySet()
        this.compileDependencies = []
        this.testDependencies = []
        this.runtimeDependencies = []

        if (grailsHome) {
            // Currently all JARs are added to each of the dependency
            // lists.
            new File(this.grailsHome, "lib").eachFileMatch(DEFAULT_DEPS, addJars)
            new File(this.grailsHome, "dist").eachFileMatch(JAR_PATTERN) { File jar ->
                // don't include test or scripts jar in runtime dependencies
                if(jar.name.startsWith("grails-test") || jar.name.startsWith("grails-scripts")) {
                    testDependencies <<  jar
                    compileDependencies << jar
                }
                else {
                    addJars(jar)
                }

            }
        }

        // Update the base directory. This triggers some extra config.
        setBaseDir(baseDir)

        // The "grailsScript" closure definition. Returns the location
        // of the corresponding script file if GRAILS_HOME is set,
        // otherwise it loads the script class using the Gant classloader.
        grailsScriptClosure = {String name ->
            def potentialScript = new File("${grailsHome}/scripts/${name}.groovy")
            potentialScript = potentialScript.exists() ? potentialScript : new File("${grailsHome}/scripts/${name}_.groovy")
            if(potentialScript.exists()) {
                return potentialScript
            }
            else {
                try {
                    return classLoader.loadClass("${name}_")
                }
                catch (e) {
                    return classLoader.loadClass(name)
                }
            }

        }
    }

    private def loadBuildPropertiesFromClasspath(Properties buildProps) {
        InputStream stream = getClass().classLoader.getResourceAsStream("build.properties")
        if(stream) {            
            buildProps.load(stream)
        }
    }

    /**
     * Returns the current base directory of this project.
     */
    public File getBaseDir() {
        return this.baseDir
    }
    
    /**
     * <p>Changes the base directory, making sure that everything that
     * depends on it gets refreshed too. If you have have previously
     * loaded a configuration file, you should load it again after
     * calling this method.</p>
     * <p><b>Warning</b> This method resets the project paths, so if
     * they have been set manually by the caller, then that information
     * will be lost!</p>
     */
    public void setBaseDir(File newBaseDir) {
        this.baseDir = newBaseDir ?: establishBaseDir()

        // Set up the project paths, using an empty config for now. The
        // paths will be updated if and when a BuildConfig configuration
        // file is loaded.
        config = new ConfigObject()
        establishProjectStructure()

        if (grailsHome) {
            // Now add the "standard-*.jar" and "jstl-*.jar" for the
            // configured servlet version. Note: we don't use
            // Metadata.getCurrent() because it caches the loaded props,
            // and some properties may be loaded after the metadata is
            // cached.
            //
            // Also, "baseDir" may not be the root of the project, in
            // which case "servletVersion" won't be known and its value
            // below will be 'null'.
            def metadata = Metadata.getInstance(new File(this.baseDir, "application.properties"))
            def servletVersion = metadata.getServletVersion()
            if (servletVersion) {
                addJars(new File(this.grailsHome, "lib/standard-${servletVersion}.jar"))
                addJars(new File(this.grailsHome, "lib/jstl-${servletVersion}.jar"))
            }
        }

        // Add the application's libraries.
        def appLibDir = new File(this.baseDir, "lib")
        if (appLibDir.exists()) {
            appLibDir.eachFileMatch(JAR_PATTERN, addJars)
        }
    }

    public File getGrailsWorkDir() {
        return this.grailsWorkDir
    }

    public void setGrailsWorkDir(File dir) {
        this.grailsWorkDir = dir
        this.grailsWorkDirSet = true
    }

    public File getProjectWorkDir() {
        return this.projectWorkDir
    }

    public void setProjectWorkDir(File dir) {
        this.projectWorkDir = dir
        this.projectWorkDirSet = true
    }

    public File getProjectWarExplodedDir() {
        return this.projectWarExplodedDir
    }

    public void setProjectWarExplodedDir(File dir) {
        this.projectWarExplodedDir = dir
        this.projectWarExplodedDirSet = true
    }

    public File getClassesDir() {
        return this.classesDir
    }

    public void setClassesDir(File dir) {
        this.classesDir = dir
        this.classesDirSet = true
    }

    public File getTestClassesDir() {
        return this.testClassesDir
    }

    public void setTestClassesDir(File dir) {
        this.testClassesDir = dir
        this.testClassesDirSet = true
    }

    public File getResourcesDir() {
        return this.resourcesDir
    }

    public void setResourcesDir(File dir) {
        this.resourcesDir = dir
        this.resourcesDirSet = true
    }

    public File getProjectPluginsDir() {
        return this.projectPluginsDir
    }

    public void setProjectPluginsDir(File dir) {
        this.projectPluginsDir = dir
        this.projectPluginsDirSet = true
    }
    
    public File getGlobalPluginsDir() {
        return this.globalPluginsDir
    }

    public void setGlobalPluginsDir(File dir) {
        this.globalPluginsDir = dir
        this.globalPluginsDirSet = true
    }

    public File getTestReportsDir() {
        return this.testReportsDir
    }

    public void setTestReportsDir(File dir) {
        this.testReportsDir = dir
        this.testReportsDirSet = true
    }

    /**
     * Loads the application's BuildSettings.groovy file if it exists
     * and returns the corresponding config object. If the file does
     * not exist, this returns an empty config.
     */
    public ConfigObject loadConfig() {
        loadConfig(new File(baseDir, "grails-app/conf/BuildConfig.groovy"))
    }

    /**
     * Loads the given configuration file if it exists and returns the
     * corresponding config object. If the file does not exist, this
     * returns an empty config.
     */
    public ConfigObject loadConfig(File configFile) {
        // To avoid class loader issues, we make sure that the
        // Groovy class loader used to parse the config file has
        // the root loader as its parent. Otherwise we get something
        // like NoClassDefFoundError for Script.
        GroovyClassLoader gcl = this.rootLoader != null ? new GroovyClassLoader(this.rootLoader) : new GroovyClassLoader(ClassLoader.getSystemClassLoader());
        def slurper = new ConfigSlurper()
        slurper.setBinding(
                    basedir: baseDir.path,
                    baseFile: baseDir,
                    baseName: baseDir.name,
                    grailsHome: grailsHome?.path,
                    grailsVersion: grailsVersion,
                    userHome: userHome,
                    grailsSettings: this)
      
        // Find out whether the file exists, and if so parse it.
        def settingsFile = new File("$userHome/.grails/settings.groovy")
        if (settingsFile.exists()) {
            Script script = gcl.parseClass(settingsFile)?.newInstance();
            if(script)
                config = slurper.parse(script)
        }

        if (configFile.exists()) {
            URL configUrl = configFile.toURI().toURL()
            Script script = gcl.parseClass(configFile)?.newInstance();

            if (!config && script)
               config = slurper.parse(script)
            else if(script)
               config.merge(slurper.parse(script))

            config.setConfigFile(configUrl)

        }

        establishProjectStructure()

        if(config.grails.default.plugin.set instanceof List) {
            defaultPluginSet = config.grails.default.plugin.set
        }

        return config
    }

    private void establishProjectStructure() {
        // The third argument to "getPropertyValue()" is either the
        // existing value of the corresponding field, or if that's
        // null, a default value. This ensures that we don't override
        // settings provided by, for example, the Maven plugin.
        def props = config.toProperties()
        if (!grailsWorkDirSet) {
            grailsWorkDir = new File(getPropertyValue(WORK_DIR, props, "${userHome}/.grails/${grailsVersion}"))
        }

        if (!projectWorkDirSet) {
            projectWorkDir = new File(getPropertyValue(PROJECT_WORK_DIR, props, "$grailsWorkDir/projects/${baseDir.name}"))
        }

        if (!projectWarExplodedDirSet) {
            projectWarExplodedDir = new File(getPropertyValue(PROJECT_WAR_EXPLODED_DIR, props,  "${projectWorkDir}/stage"))
        }

        if (!classesDirSet) {
            classesDir = new File(getPropertyValue(PROJECT_CLASSES_DIR, props, "$projectWorkDir/classes"))
        }

        if (!testClassesDirSet) {
            testClassesDir = new File(getPropertyValue(PROJECT_TEST_CLASSES_DIR, props, "$projectWorkDir/test-classes"))
        }

        if (!resourcesDirSet) {
            resourcesDir = new File(getPropertyValue(PROJECT_RESOURCES_DIR, props, "$projectWorkDir/resources"))
        }

        if (!projectPluginsDirSet) {
            projectPluginsDir = new File(getPropertyValue(PLUGINS_DIR, props, "$projectWorkDir/plugins"))
        }

        if (!globalPluginsDirSet) {
            globalPluginsDir = new File(getPropertyValue(GLOBAL_PLUGINS_DIR, props, "$grailsWorkDir/global-plugins"))
        }

        if (!testReportsDirSet) {
            testReportsDir = new File(getPropertyValue(PROJECT_TEST_REPORTS_DIR, props, "${baseDir}/test/reports"))
        }
    }

    private getPropertyValue(String propertyName, Properties props, String defaultValue) {
        // First check whether we have a system property with the given name.
        def value = System.getProperty(propertyName)
        if (value != null) return value

        // Now try the BuildSettings config.
        value = props[propertyName]

        // Return the BuildSettings value if there is one, otherwise
        // use the default.
        return value != null ? value : defaultValue
    }

    private File establishBaseDir() {
        def sysProp = System.getProperty(APP_BASE_DIR)
        def baseDir
        if (sysProp) {
            baseDir = sysProp == '.' ? new File("") : new File(sysProp)
        }
        else {
            baseDir = new File("")
            if(!new File(baseDir, "grails-app").exists()) {
                // be careful with this next step...
                // baseDir.parentFile will return null since baseDir is new File("")
                // baseDir.absoluteFile needs to happen before retrieving the parentFile
                def parentDir = baseDir.absoluteFile.parentFile

                // keep moving up one directory until we find
                // one that contains the grails-app dir or get
                // to the top of the filesystem...
                while (parentDir != null && !new File(parentDir, "grails-app").exists()) {
                    parentDir = parentDir.parentFile
                }

                if (parentDir != null) {
                    // if we found the project root, use it
                    baseDir = parentDir
                }
            }

        }
        return baseDir.canonicalFile
    }
}
