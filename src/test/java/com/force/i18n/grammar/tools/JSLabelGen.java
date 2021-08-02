/*
 * Copyright (c) 2020, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license.
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n.grammar.tools;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.concurrent.Callable;

import com.force.i18n.grammar.VersionInfo;
import com.force.i18n.grammar.offline.JavaScriptLabelsGenerator;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

/**
 * Sample executable class to generate language-dependent files for grammaticus.js
 * <p>
 * To run this from command line, try:
 * <pre>
 *   <code>{@literal mvn exec:java -Dexec.mainClass="com.force.i18n.grammar.tools.JSLabelGen" -Dexec.classpathScope="test" -Dexec.args="<args>"}</code>
 *   {@literal specify <args> as a parameter to this main method. use just "-h" for help.}
 * </pre>
 * Note that you need <code>{@literal -Dexec.classpathScope="test"}</code> parameter.
 *
 * @see com.force.i18n.grammar.offline.JavaScriptLabelsGenerator
 * @author yoikawa
 * @since 226
 */
public class JSLabelGen extends JavaScriptLabelsGenerator {

    JSLabelGen(File rootDirectory, String labelFileName, String dictionaryFileName)
            throws MalformedURLException, URISyntaxException {
        super(rootDirectory, labelFileName, dictionaryFileName);
    }

    @Override
    protected void log(String msg) {
        System.out.println(msg);
    }

    @Override
    protected void error(String msg) {
        System.err.println(msg);
    }

    @Command(name="JSLabelGen", mixinStandardHelpOptions = true,
             headerHeading = "Usage:%n%n",
             synopsisHeading = "%n",
             descriptionHeading = "%nDescription:%n%n",
             parameterListHeading = "%nParameters:%n",
             optionListHeading = "%nOptions:%n",
             header = "Label generator for grammaticus.js",
             description = {
                     "Generates langauge dependent files for grammaticus.js.",
                     COPYRIGHT_TEXT},
             version= {
                 "CLI version: " + VersionInfo.VERSION}
            )
    protected static class CmdExecutor implements Callable<Integer> {
        @Spec protected CommandSpec spec; // injected by picocli

        @Option(names={"-n", "--name"}, defaultValue=DEFAULT_NAMES_XML, description="dictionary file name. (default: ${DEFAULT-VALUE})")
        protected String nameFile;

        @Option(names={"-l", "--label"}, defaultValue=DEFAULT_LABELS_XML, description="label file name. (default: ${DEFAULT-VALUE})")
        protected String labelFile;

        @Option(names={"-o", "--output"}, paramLabel = "DIR", description="a target directory to generate files. (default: [SRC])")
        protected void setOutputDirectory(String outDir) {
            this.outputDir = verifyFolder(outDir, true);
        }

        @Option(names={"-c", "--comment"}, arity = "0..1", defaultValue="false", fallbackValue="true",
            description="generate header comment text in each file. (default: ${DEFAULT-VALUE})")
        protected boolean shouldGenerateComment;

        @Option(names={"-a", "--all"}, arity = "0..1", defaultValue="false", fallbackValue="true",
            description="generate all names defined in dictionary file. if not specified, only names referred by label file will be generated. (default: ${DEFAULT-VALUE})")
        protected boolean shouldGenerateAllNames;

        @Parameters(paramLabel= "SRC", arity = "0..1", description = "source input directroy to read. (default: current directory)")
        protected void setSourceDirectory(String srcDir) {
            this.sourceDir = verifyFolder(srcDir, false);
        }

        protected File sourceDir;
        protected File outputDir;

        /**
         * main execution code for generating files.
         */
        @Override
        public Integer call() throws Exception {
            if (sourceDir == null) sourceDir = verifyFolder(".", false);
            if (outputDir == null) outputDir = sourceDir;
            verifySourceLabels();

            JSLabelGen gen = new JSLabelGen(sourceDir, labelFile, nameFile);
            gen.generateLabels(outputDir, shouldGenerateComment, shouldGenerateAllNames);
            return 0;
        }

        protected void verifySourceLabels() throws IOException {
            File labels = new File(sourceDir, labelFile);
            boolean isLabelsAvailable = labels.exists();

            File names = new File(sourceDir, nameFile);
            boolean isNamesAvailable = names.exists();

            if (!isNamesAvailable)
                throw new ParameterException(spec.commandLine(), "no name file in input direcotry: " + names.getCanonicalPath());
            if (!isLabelsAvailable)
                throw new ParameterException(spec.commandLine(), "no label file in input direcotry: " + labels.getCanonicalPath());
        }

        protected File verifyFolder(String path, boolean createNewDirectory) {
            File f = new File(path);
            try {
                f = f.getCanonicalFile();
                path = f.getPath();
                if (!f.exists()) {
                    if (createNewDirectory) {
                        if (!f.mkdir()) {
                            throw new ParameterException(spec.commandLine(), "cannot create directory: " + path);
                        }
                    } else {
                        throw new ParameterException(spec.commandLine(), "directory does not exist: " + path);
                    }
                } else if (!f.isDirectory()) {
                    throw new ParameterException(spec.commandLine(), path + " is not a directory.");
                }
            } catch (Exception ex) {
                throw new ParameterException(spec.commandLine(), "Unknown error while processig path: " + path);
            }
            return f;
        }
    }

    public static void main(String[] args) throws IOException, URISyntaxException {
        CommandLine cmd = new CommandLine(new CmdExecutor());
        System.out.print(cmd.getHelp().header());
        cmd.printVersionHelp(System.out);

        int status = cmd.execute(args);
        System.exit(status);
    }
}