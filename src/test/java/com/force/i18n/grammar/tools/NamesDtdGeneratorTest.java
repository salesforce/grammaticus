/**
 * 
 */
package com.force.i18n.grammar.tools;

import org.junit.Test;

/**
 * Usually ignored test that generates the names.dtd files.
 * @author stamm
 * @since 226.0
 */
//@Ignore
public class NamesDtdGeneratorTest {
	public NamesDtdGeneratorTest() {
	}
	
	@Test
	public void testGenerator() throws Exception {
		new NamesDtdGenerator().generate("", "target/dtds");
	}

}
