package com.bai.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for the z3 load-failure diagnostic.
 *
 * <p>See upstream issue #77: the previous message said only that z3 could not be detected,
 * which left reporters unable to tell a missing library from one installed in a directory the
 * linker never searches. One of them had copied the shared object into /usr/local/bin.
 */
public class Z3DiagnosticsTest {

    @Test
    public void describeZ3LoadFailureReportsTheUnderlyingError() {
        UnsatisfiedLinkError error = new UnsatisfiedLinkError("no libz3java in java.library.path");
        String message = Utils.describeZ3LoadFailure(error);

        Assert.assertTrue("keeps the original advice",
                message.contains("Cannot detect z3 solver library"));
        Assert.assertTrue("names the error type",
                message.contains("java.lang.UnsatisfiedLinkError"));
        Assert.assertTrue("quotes the linker message",
                message.contains("no libz3java in java.library.path"));
    }

    @Test
    public void describeZ3LoadFailureReportsWhereItLooked() {
        String message = Utils.describeZ3LoadFailure(new UnsatisfiedLinkError("boom"));

        Assert.assertTrue("names the file it wants",
                message.contains(Utils.getExpectedZ3LibraryFileName()));
        Assert.assertTrue("prints java.library.path",
                message.contains("java.library.path = "));
        Assert.assertTrue("prints the platform search variable",
                message.contains(Utils.getNativeSearchPathVariable() + " = "));
    }

    @Test
    public void describeZ3LoadFailurePointsAtALibraryDirectory() {
        String message = Utils.describeZ3LoadFailure(new UnsatisfiedLinkError("boom"));

        // The mistake in issue #77 was installing into /usr/local/bin.
        Assert.assertTrue("warns that the library belongs in a library directory",
                message.contains("/usr/local/lib") && message.contains("/usr/local/bin"));
        Assert.assertTrue("warns about version mismatch",
                message.contains("com.microsoft.z3.jar"));
    }

    @Test
    public void describeZ3LoadFailureToleratesAMessagelessError() {
        String message = Utils.describeZ3LoadFailure(new NoClassDefFoundError());
        Assert.assertTrue(message.contains("java.lang.NoClassDefFoundError"));
    }

    @Test
    public void expectedLibraryNameMatchesThePlatform() {
        String name = Utils.getExpectedZ3LibraryFileName();
        Assert.assertTrue("mentions the z3 JNI library", name.contains("libz3java"));

        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.contains("win")) {
            Assert.assertTrue(name.contains(".dll"));
            Assert.assertEquals("PATH", Utils.getNativeSearchPathVariable());
        } else if (os.contains("mac") || os.contains("darwin")) {
            Assert.assertTrue(name.contains(".dylib"));
            Assert.assertEquals("DYLD_LIBRARY_PATH", Utils.getNativeSearchPathVariable());
        } else {
            Assert.assertTrue(name.contains(".so"));
            Assert.assertEquals("LD_LIBRARY_PATH", Utils.getNativeSearchPathVariable());
        }
    }
}
