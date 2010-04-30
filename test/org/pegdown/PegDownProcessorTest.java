/*
 * Copyright (C) 2010 Mathias Doenitz
 *
 * Based on peg-markdown (C) 2008-2010 John MacFarlane
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.pegdown;

import org.parboiled.support.ParsingResult;
import org.parboiled.support.ToStringFormatter;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.w3c.tidy.Tidy;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import static org.parboiled.trees.GraphUtils.printTree;
import static org.pegdown.PegDownProcessor.prepare;
import static org.pegdown.TestUtils.assertEqualsMultiline;

public class PegDownProcessorTest {

    private final PegDownProcessor processor = new PegDownProcessor();
    private final PegDownProcessor extProcessor = new PegDownProcessor(Extensions.ALL);
    private final Tidy tidy = new Tidy();

    @BeforeClass
    public void setup() {
        tidy.setPrintBodyOnly(true);
        tidy.setShowWarnings(false);
        tidy.setQuiet(true);
    }

    @Test
    public void test() throws Exception {
        // MarkdownTest suite
        test("Amps and angle encoding");
        test("Auto links");
        test("Backslash escapes");
        test("Blockquotes with code blocks");
        test("Code Blocks");
        test("Code Spans");
        test("Hard-wrapped paragraphs with list-like lines");
        test("Horizontal rules");
        test("Inline HTML (Advanced)");
        test("Inline HTML (Simple)");
        test("Inline HTML comments");
        test("Links, inline style");
        test("Links, reference style");
        test("Links, shortcut references");
        test("Literal quotes in titles");
        test("Nested blockquotes");
        test("Ordered and unordered lists");
        test("Strong and em together");
        test("Tabs");
        test("Tidyness");

        test("Markdown Documentation - Basics");
        test("Markdown Documentation - Syntax");
        
        // custom tests
        test("Linebreaks");
        test("Quoted Blockquote");

        testExt("Extensions");
    }

    private void test(String testName) {
        test(processor, testName);
    }

    private void testExt(String testName) {
        test(extProcessor, testName);
    }

    private void test(PegDownProcessor processor, String testName) {
        String markdown = FileUtils.readAllTextFromResource(testName + ".text");

        ParsingResult<AstNode> result = processor.getParser().parseRawBlock(prepare(markdown));
        // assertEqualsMultiline(printNodeTree(result), "");  // for advanced debugging: check the parse tree
        AstNode astRoot = result.parseTreeRoot.getValue();
        String expectedAst = FileUtils.readAllTextFromResource(testName + ".ast.text");
        String actualAst = printTree(astRoot, new ToStringFormatter<AstNode>());
        assertEqualsMultiline(actualAst, expectedAst);

        String expectedHtml = FileUtils.readAllTextFromResource(testName + ".compact.html");
        String actualHtml = processor.markdownToHtml(markdown);
        assertEqualsMultiline(actualHtml, expectedHtml);

        // tidy up html for fair equality test
        String expectedUntidy = FileUtils.readAllTextFromResource(testName + ".html");
        if (expectedUntidy != null) {
            actualHtml = tidy(actualHtml);
            expectedHtml = tidy(expectedUntidy);
            assertEqualsMultiline(actualHtml, expectedHtml);
        }
    }

    private String tidy(String html) {
        Reader in = new StringReader(html);
        Writer out = new StringWriter();
        tidy.parse(in, out);
        return out.toString();
    }


}
