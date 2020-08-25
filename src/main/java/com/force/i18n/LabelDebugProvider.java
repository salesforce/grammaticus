/* 
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license. 
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.force.i18n.commons.text.TextUtil;
import com.google.common.base.Preconditions;
import com.google.common.collect.*;

/**
 * Provide access to LabelDebugging.  This class has two distinct implementations, one which does nothing
 * and one which may add some helpful hints to the end of every label on a page and keep a list of those
 * labels around for display.
 *
 * @author stamm (from rchen/150)
 */
public abstract class LabelDebugProvider {
	// TODO SLT: Switch to an enum
    protected static final String TRACE = "trace";
    protected static final String MASK = "mask";

    private volatile static LabelDebugProvider INSTANCE = new DisabledLabelDebugProvider();  // Default to disabled

    public static LabelDebugProvider get() { return INSTANCE; }

    /**
     * Set the debug provider to be enabled or disabled
     * @param enabled should the defaut debug provider be enabled
     */
    public static void setLabelDebugProviderEnabled(boolean enabled) {
    	setLabelDebugProviderEnabled(enabled ? new EnabledLabelDebugProvider() : new DisabledLabelDebugProvider());
    }

    /**
     * Set the debug provider to be used.  Generally you want to use the boolean value and
     * rely on the default behavior
     * @param provider the labeldebugprovider to use globally
     */
    public static void setLabelDebugProviderEnabled(LabelDebugProvider provider) {
    	Preconditions.checkNotNull(provider);
        INSTANCE = provider;
    }

    /**
     * Given a label reference, and it's text, possibly modify the hint to provide help
     * @param text the text value of the label
     * @param section the section for the label
     * @param key the key for the label
     * @return the text passed in, possibly with some hinting information appended.
     */
    public abstract String makeLabelHintIfRequested(String text, String section, String key);

    /**
     * @return whether or not label debugging is ever allowed
     */
    public abstract boolean isAllowed();

    /**
     * @return if the current Request should show the Label Debugging images.
     */
    public boolean isLabelHintRequest() {
        return false;
    }

    /**
     * @return a continuation of the current label debug list so that it can survive
     * re-establishing the localization context
     */
    public LabelDebugContinuation getContinuation() {
        return null;
    }

    /**
     * @param continuation the continuation returned from getContinuation to use to establish the request afterwards
     */
    public void setContinuation(LabelDebugContinuation continuation) {
    }

    /**
     * @return whether we are tracking label usage or not
     */
    public abstract boolean isTrackingLabelUsage();

    /**
     * Set whether we are tracking label usage or not
     * @param trackUsage whether usage should be tracked
     *
     * Note: This is not threadsafe, and you should synchronize on the object before setting.
     */
    public abstract void setTrackingLabelUsage(boolean trackUsage);

    /**
     * Mark the label as being tracked:
     * @param section the section of the label
     * @param key the key of the label
     */
    public abstract void trackLabel(String section, String key);

    /**
     * @return the set of labels used so far in the application, returned
     * as an unmodifiable multimap from section to key
     */
    public abstract SetMultimap<String,String> getUsedLabels();

    /**
     * Sets the value of the current request as to whether it
     * @param value the value of the
     * @return the previous value of the request.
     */
    public abstract boolean setLabelHintRequest(boolean value);

    /**
     * Set the label hint mode.
     * @param value either null, TRACE or MASK
     */
    public abstract void setLabelHintMode(String value);

    public List<LabelDebug> getLabelDebugs() {
        return ImmutableList.<LabelDebug>of();
    }

    /**
     * Marker interface for a debug continuation so that label hints can survive reestablishing requests
     */
    public static interface LabelDebugContinuation {}

    /**
     * Implementation of LabelDebugProvider that does nothing
     */
    static final class DisabledLabelDebugProvider extends LabelDebugProvider {
        @Override
        public boolean isAllowed() {
            return false;
        }

        @Override
        public String makeLabelHintIfRequested(String text, String section, String key) {
            return text;
        }

        @Override
        public boolean setLabelHintRequest(boolean value) {
            throw new UnsupportedOperationException("You shouldn't call this");
        }

        @Override
        public SetMultimap<String, String> getUsedLabels() {
            throw new UnsupportedOperationException("You shouldn't call this");
        }

        @Override
        public boolean isTrackingLabelUsage() {
            return false;
        }

        @Override
        public void trackLabel(String section, String key) {
            // Do nothing
        }

        @Override
        public void setTrackingLabelUsage(boolean trackUsage) {
            throw new UnsupportedOperationException("You shouldn't call this");
        }

        @Override
        public void setLabelHintMode(String value) {
            throw new UnsupportedOperationException("You shouldn't call this");
        }
    }

    /**
     * Implementation of LabelDebugProvider that stores the labels used in the current request.
     */
    static final class EnabledLabelDebugProvider extends LabelDebugProvider {
        private static final int STACK_LENGTH = 1300;

        // Big f'n switch for whether we're doing label debugging or not.  Gated by Debug.isDebug()
        private static final ThreadLocal<Boolean> IS_DEBUGGING = new ThreadLocal<Boolean>() {
            @Override protected Boolean initialValue() { return Boolean.FALSE; }
        };

        // If true, it means that we're tracking the total set of labels
        private boolean isTracking = false;
        // Yes unsynchronized, to prevent a big map we're going to do something a little fancier in the code itself
        private final Set<LabelReference> usedLabels = Collections.newSetFromMap(new ConcurrentHashMap<LabelReference,Boolean>(256, .75f, 16));

        // Keeps track of labels from this Thread (i.e. request)
        private static final ThreadLocal<List<LabelDebug>> LABEL_DEBUGS = new ThreadLocal<List<LabelDebug>>() {
            @Override
            protected List<LabelDebug> initialValue() {
                return new ArrayList<LabelDebug>(1000);  // It seems most pages have a few hundred labels
            }
        };

        private String hintMode;

        public EnabledLabelDebugProvider() {
            super();
            this.hintMode = TRACE;
        }

        @Override
        public boolean isAllowed() {
            return true;
        }

        /**
         * @param text Text of the label, to be stored until printed in dev footer
         * @param section Section
         * @param key Key (aka param)
         */
        @Override
        public String makeLabelHintIfRequested(String text, String section, String key) {
            if (text != null && isLabelHintRequest()) {
                if (this.hintMode.equals(TRACE)) {
                    List<LabelDebug> labelDebugs = LABEL_DEBUGS.get();


                    // **3 is the MAGIC NUMBER**
                    // We start at 3 because we want to skip all the frames within Thread and Debug, if the call stack changes, this will have to as well.
                    String stackTrace = formatStackTrace(new Exception().getStackTrace(), 2, Integer.MAX_VALUE);
                    if (stackTrace.length() > STACK_LENGTH) {
                        stackTrace = stackTrace.substring(0, STACK_LENGTH);
                    }

                    labelDebugs.add(new LabelDebug(text, section, key, TextUtil.escapeToHtml(stackTrace)));
                    return text + "[#" + (labelDebugs.size() - 1) + "]";
                } else if (this.hintMode.equals(MASK)) {
                    char[] mask = new char[text.length()];
                    Arrays.fill(mask, '#');
                    return new String(mask);
                }
            }
            return text;
        }

        /**
         * Determines if the current Request should show the Label Debugging images.
         */
        @Override
        public boolean isLabelHintRequest() {
            return IS_DEBUGGING.get();
        }

        @Override
        public boolean setLabelHintRequest(boolean value) {
            boolean wasDebugging = IS_DEBUGGING.get();
            IS_DEBUGGING.set(value);
            if (wasDebugging && !value) {
                LABEL_DEBUGS.remove();  // Clear out the thread if we were debugging and now are not (i.e. on release).
            }
            return wasDebugging;
        }

        @Override
        public List<LabelDebug> getLabelDebugs() {
            return LABEL_DEBUGS.get();
        }

        @Override
        public void trackLabel(String section, String key) {
            if (!isTracking) {
                return;
            }
            usedLabels.add(new LabelRef(section, key));
        }

        @Override
        public SetMultimap<String, String> getUsedLabels() {
            if (isTracking) {
                // Make a nice sorted copy
                TreeMultimap<String,String> result = TreeMultimap.create();
                for (LabelReference lr : usedLabels) {
                    result.put(lr.getSection(), lr.getKey());
                }
                return Multimaps.unmodifiableSetMultimap(result);
            } else {
                return TreeMultimap.create();
            }
        }

        @Override
        public boolean isTrackingLabelUsage() {
            return isTracking;
        }

        @Override
        public synchronized void setTrackingLabelUsage(boolean trackUsage) {
            this.isTracking = trackUsage;
        }

        private static final LabelDebugContinuation NOT_TRACKING = new LabelDebugContinuation() {};

        static final class RealLabelDebugContinuation implements LabelDebugContinuation {
            private final List<LabelDebug> existingDebugs;
            public RealLabelDebugContinuation(List<LabelDebug> existingDebugs) {
                this.existingDebugs = existingDebugs;
            }
            public List<LabelDebug> getList() {
                return this.existingDebugs;
            }
        }

        @Override
        public LabelDebugContinuation getContinuation() {
            if (isLabelHintRequest()) {
                return new RealLabelDebugContinuation(LABEL_DEBUGS.get());
            } else {
                return NOT_TRACKING;
            }
        }

        @Override
        public void setContinuation(LabelDebugContinuation continuation) {
            if (continuation == NOT_TRACKING) {
                setLabelHintRequest(false);
            } else {
                setLabelHintRequest(true);
                assert continuation instanceof RealLabelDebugContinuation;
                LABEL_DEBUGS.set(((RealLabelDebugContinuation)continuation).getList());
            }
        }

        @Override
        public void setLabelHintMode(String value) {
            this.hintMode = value;
        }
    }

    /*
     * Format the stack trace in the range and stop when meet stop class name
     * @param stack Stack trace elements
     * @param startFrame The index of elements to start with
     * @param maxFrame The max number of elements to format
     * @param stopAtText The class names to stop at. Use : as separator for multiple stop classes, e.g., com.caucho:org.eclipse.jetty
     * @return Stack trace in a formated string
     */
    // TODO: Move this to I18nJavaUtils?
    public static String formatStackTrace(StackTraceElement[] stack, int startFrame, int maxFrames) {
        if (stack == null) {
            return null;
        }

        StringBuilder result = new StringBuilder();
        // cast to long to deal with overflow if startFrame + maxFrames > Integer.MAX_VALUE
        int endAt = (int) Math.min(stack.length, (long) startFrame + (long) maxFrames);
        for (int i = startFrame; i < endAt; i++) {
            result.append('\n');
            result.append(stack[i].toString());
        }
        return result.toString();
    }
}
