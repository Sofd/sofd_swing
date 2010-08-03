/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.sofd.swing;

import javax.swing.text.AttributeSet;
import javax.swing.text.DocumentFilter.FilterBypass;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Fokko Breden
 */
public class FixedSizeFilterTest {

    public FixedSizeFilterTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of insertString method, of class FixedSizeFilter.
     */
    @Test
    public void testInsertString() throws Exception {
        System.out.println("insertString");
        FilterBypass fb = null;
        int offset = 0;
        String str = "";
        AttributeSet attr = null;
        FixedSizeFilter instance = new FixedSizeFilter(16);
        //instance.insertString(fb, offset, str, attr);
        // TODO review the generated test code and remove the default call to fail.
        // fail("The test case is a prototype.");
    }

    /**
     * Test of replace method, of class FixedSizeFilter.
     */
    @Test
    public void testReplace() throws Exception {
        System.out.println("replace");
        FilterBypass fb = null;
        int offset = 0;
        int length = 0;
        String str = "";
        AttributeSet attrs = null;
        FixedSizeFilter instance = new FixedSizeFilter(16);
        //instance.replace(fb, offset, length, str, attrs);
        // TODO review the generated test code and remove the default call to fail.
        // fail("The test case is a prototype.");
    }

}