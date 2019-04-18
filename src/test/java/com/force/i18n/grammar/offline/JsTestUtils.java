/* 
 * Copyright (c) 2019, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license. 
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */
package com.force.i18n.grammar.offline;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.junit.Assert;

import com.force.i18n.HumanLanguage;
import com.force.i18n.LabelReference;
import com.force.i18n.LanguageLabelSetDescriptor.GrammaticalLabelSetDescriptor;
import com.force.i18n.Renameable;
import com.force.i18n.grammar.GrammaticalLabelSet;
import com.force.i18n.grammar.GrammaticalTerm;
import com.force.i18n.grammar.parser.GrammaticalLabelSetLoader;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;

/**
 * Tools for testing differences between Java and JS evaluation of Labels
 * @author stamm
 * @since 1.1
 */
public class JsTestUtils {


    private static AtomicReference<Bindings> BINDINGS = new AtomicReference<>();
    /**
     * @return a script engine with an appropriate global context for $Api so that we don't have to revaluate the functions constantly
     * 
     * If you want to add stuff to bindings while running tests, just comment out the if and the closing brace
     */
    public static ScriptEngine getScriptEngine() {
        Bindings bindings = BINDINGS.get();
        if (bindings == null) {
            // Create an engine just to compile the $AP
            ScriptEngine compEngine = new ScriptEngineManager().getEngineByName("JavaScript");
            compEngine.put("out", System.out);

            try {
                bindings = compEngine.getBindings(ScriptContext.GLOBAL_SCOPE);
                bindings.put("Grammaticus",  compEngine.eval(Resources.toString(JsTestUtils.class.getResource("grammaticus.js"), Charsets.UTF_8)));
                BINDINGS.set(bindings);
            } catch (ScriptException | IOException x) {
                x.printStackTrace();
            }
        }
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("JavaScript");
        engine.setBindings(bindings, ScriptContext.GLOBAL_SCOPE);
        return engine;
    }

    public static void compareResults(HumanLanguage language, GrammaticalLabelSetDescriptor descriptor, Collection<LabelReference> labels, Renameable[] nouns) throws IOException, ScriptException {
    	ScriptEngine engine = getScriptEngine();
        GrammaticalLabelSetLoader loader = new GrammaticalLabelSetLoader(descriptor);
        GrammaticalLabelSet set = loader.getSet(language);

        StringBuilder sb = new StringBuilder();
        engine.put("out", System.out);
        // Instantiate Grammaticus
        sb.append("var $G = new Grammaticus();\n");
        // Apply overrides to the instance
        set.getDictionary().getDeclension().writeJsonOverrides(sb, "$G");
        
        Set<GrammaticalTerm> termsInUse = new HashSet<>();
        // Add in labels
        sb.append("$G.addLabels(");
        set.writeJson(sb, labels.stream().map(a->a.getSection()+"."+a.getKey()).collect(Collectors.toList()), termsInUse);
        // Add in any default terms
        sb.append(");\n$G.addTerms(");
        set.getDictionary().writeJsonTerms(sb, true, termsInUse);
        sb.append(");");
        //System.out.println(sb.toString());
        // Evaluate it.
        engine.eval(sb.toString());
        
        if (nouns != null && nouns.length >= 0) {
        	sb = new StringBuilder();
        	sb.append("$G.addTerms(");
        	set.getDictionary().writeJsonTerms(sb, true, Arrays.asList(nouns).stream().map(a->a.getStandardNoun(language)).collect(Collectors.toList()));
        	sb.append(");");
            engine.eval(sb.toString());
        	
        }
        
        List<String> errors = new ArrayList<>();
        for (LabelReference label : labels) {
        	String javaStr = set.getString(label.getSection(), label.getKey(), nouns, label.getArguments());
        	String nounNames = nouns != null && nouns.length > 0 ? Arrays.asList(nouns).stream().map(a->a.getName()).collect(Collectors.joining("\",\"", "[\"", "\"]")) : "null";
        	String args = label.getArguments() != null ? Arrays.asList(label.getArguments()).stream().map(a->a instanceof String ? "'" + a + "'" : String.valueOf(a)).collect(Collectors.joining(",", "[", "]")) : "null";
        	String jsStr = String.valueOf(engine.eval("$G.getLabel('"+label.getSection()+"."+label.getKey()+"', "+ nounNames + ", " + args + ")"));
        	if (!javaStr.equals(jsStr)) {
        		errors.add("Mismatch for " + label + " should be \t" + javaStr + "\t but was \t" + jsStr);
        	}
        	if (label.getArguments().length > 0) {
                javaStr = MessageFormat.format(set.getString(label.getSection(), label.getKey(), nouns, label.getArguments()), label.getArguments());
                jsStr = String.valueOf(engine.eval("$G.getString('"+label.getSection()+"."+label.getKey()+"', "+ nounNames + ", " + args + ")"));
                if (!javaStr.equals(jsStr)) {
                    errors.add("Mismatch for " + label + " when formatting should be \t" + javaStr + "\t but was \t" + jsStr);
                }
        	    
        	}
        }
        Assert.assertTrue(errors.stream().collect(Collectors.joining("\n")), errors.size() == 0);

    }
    
}
