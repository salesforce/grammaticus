package com.force.i18n.grammar.offline;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;

import org.junit.*;
import org.junit.rules.TemporaryFolder;

import com.force.i18n.grammar.parser.BaseGrammaticalLabelTest;



/**
 * Test converting labels into Json format for offline.
 *
 * @author yoikawa
 * @since 226
 */
public class JavaScriptLabelsGeneratorTest {
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private boolean exists(String langStr) {
        File f = new File(tempFolder.getRoot(), langStr + ".js");
        return f.exists() && f.canRead();
    }

    // negative test for missing labels in root directory
    @Test (expected = IOException.class)
    public void testEmpty() throws Exception {
        JavaScriptLabelsGenerator gen = new JavaScriptLabelsGenerator(tempFolder.getRoot());
        gen.generateLabels(tempFolder.getRoot(), false, false);
    }

    @Test
    public void generateFiles() throws Exception {
        URL baseDir = BaseGrammaticalLabelTest.getLabelDirURL();
        JavaScriptLabelsGenerator gen = new JavaScriptLabelsGenerator(Paths.get(baseDir.toURI()).toFile());

        String root = baseDir.getFile();
        root = root.substring(0, root.length() - 1);

        gen.generateLabels(tempFolder.getRoot(), false, false);

        Assert.assertTrue(exists("en"));
        Assert.assertTrue(exists("en_GB"));

        Assert.assertTrue(exists("es"));
        Assert.assertTrue(exists("es_MX"));
        Assert.assertTrue(exists("pt_BR"));
        Assert.assertTrue(exists("zh_TW"));

        // some special cases -- "nl" to "nl_NL", "zh" to "zh_CN"
        Assert.assertTrue(exists("nl_NL"));
        Assert.assertTrue(exists("zh_CN"));

        // some negative test
        Assert.assertFalse(exists("en_US")); // yeah, en_US is intentionally replaced w/ "en"
        Assert.assertFalse(exists("nl"));
        Assert.assertFalse(exists("pt"));
        Assert.assertFalse(exists("zh"));
        Assert.assertFalse(exists("zh_HK"));
    }
}
