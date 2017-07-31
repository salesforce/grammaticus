/* 
 * Copyright (c) 2017, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license. 
 * For full license text, see LICENSE.txt file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */

package com.force.i18n;

import java.util.Locale;

/**
 * Basic implementation of the {@link LocaleInfo}
 * @author jared.pearson
 */
class LocaleInfoImpl implements LocaleInfo {
    private static final long serialVersionUID = 1264587819352292136L;
    private final Locale locale;
    private final boolean useEasternNameOrder;
    private final boolean formal;
    
    public LocaleInfoImpl(Locale locale, boolean useEasternNameOrder, boolean formal) {
        assert locale != null : "locale should not be null";
        this.locale = locale;
        this.useEasternNameOrder = useEasternNameOrder;
        this.formal = formal;
    }
    
    @Override
    public boolean useEasternNameOrder() {
        return this.useEasternNameOrder;
    }
    
    @Override
    public boolean isFormal() {
        return this.formal;
    }
    
    @Override
    public Locale getLocale() {
        return locale;
    }
    

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((locale == null) ? 0 : locale.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final LocaleInfoImpl other = (LocaleInfoImpl) obj;
        if (locale == null) {
            if (other.locale != null) {
                return false;
            }
        } else if (!locale.equals(other.locale)) {
            return false;
        }
        return true;
    }
    
}