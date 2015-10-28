/*
 * Copyright 2015 Victor Nazarov <asviraspossible@gmail.com>.
 */
package com.github.sviperll.maven.plugin.mustache;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Goal which writes properties to file.
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
@Mojo(name = "render", defaultPhase = LifecyclePhase.GENERATE_RESOURCES)
public class RenderMustacheMojo extends ConfiguredMustacheMojo {

    @Parameter(defaultValue="src/main/resources/templates/", required=true, property="templateDirectory")
   	private File templateDirectory;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            Charset charset = Charset.forName(encoding);
            MustacheLoader mustacheReader = new MustacheLoader(new DefaultMustacheFactory(templateDirectory), getLog());
            if (contexts != null) {
                for (Context contextConfig: contexts) {
                    Object contextObject = loadContext(contextConfig);
                    if (contextConfig.templates != null) {
                        for (Template template: contextConfig.templates) {
                            Mustache mustache = mustacheReader.load(new File(templateDirectory, template.inputTemplate), charset);
                            renderMustache(mustache, contextObject, template.outputFile, charset);
                        }
                    }
                }
            }
        } catch (IOException ex) {
            throw new MojoExecutionException("Error writing mustache", ex);
        }
    }

    private Object loadContext(Context context) throws MojoFailureException, IOException {
        Charset charset = Charset.forName(encoding);
        if (context.type.equals("properties"))
            return loadPropertiesContext(context.file, charset);
        else if (context.type.equals("json"))
            return loadJsonContext(context.file, charset);
        else
            throw new MojoFailureException(MessageFormat.format("Unsupported context type: {0}, use one of json, properties",
                                                                context.type));
    }
}
