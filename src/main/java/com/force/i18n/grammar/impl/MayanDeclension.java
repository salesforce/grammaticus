package com.force.i18n.grammar.impl;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

import com.force.i18n.HumanLanguage;
import com.force.i18n.grammar.*;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

public class MayanDeclension extends HungarianDeclension {

    //TODO maybe similar to HungarianDeclension, but no case, no startsWith
    
    public MayanDeclension(HumanLanguage language) {
        super(language);
    }

}
