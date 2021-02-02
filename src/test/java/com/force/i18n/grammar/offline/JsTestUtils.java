/* 
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license. 
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */
package com.force.i18n.grammar.offline;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import javax.script.*;

import org.junit.Assert;

import com.force.i18n.*;
import com.force.i18n.LanguageLabelSetDescriptor.GrammaticalLabelSetDescriptor;
import com.force.i18n.grammar.GrammaticalLabelSet;
import com.force.i18n.grammar.GrammaticalTerm;
import com.force.i18n.grammar.parser.GrammaticalLabelSetLoader;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.ibm.icu.text.MessageFormat;

/**
 * Tests for executing JS code inside the JVM. 
 * Not optimized for Graal. 
 * @author stamm
 *
 */
public class JsTestUtils {
    private static String GRAMMATICUS_JS;

    private static String getGrammaticusJs() throws IOException {
    	if (GRAMMATICUS_JS == null) {	
    		GRAMMATICUS_JS = Resources.toString(JsTestUtils.class.getResource("grammaticus.js"), Charsets.UTF_8); 		
    	}
    	return GRAMMATICUS_JS;
    }
    
    /**
     * @return a script engine with an appropriate global context for $Api so that we don't have to revaluate the functions constantly
     * 
     * If you want to add stuff to bindings while running tests, just comment out the if and the closing brace
     */
    public static ScriptEngine getScriptEngine() {
        System.setProperty("polyglot.js.nashorn-compat","true");
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("JavaScript");
        
        Bindings engineBindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
        engineBindings.put("polyglot.js.allowAllAccess", true);

        // Assume graal does the caching, and not us.
        Bindings bindings = engine.getBindings(ScriptContext.GLOBAL_SCOPE);
        try {
            bindings.put("Grammaticus", engine.eval(getGrammaticusJs()));
        } catch (ScriptException | IOException e) {
            e.printStackTrace();
        }

        engine.put("out", System.out);
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
