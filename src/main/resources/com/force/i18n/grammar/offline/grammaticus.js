// Grammaticus.js
/*
 * Copyright (c) 2025, Salesforce, Inc.
 * SPDX-License-Identifier: Apache-2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
var Grammaticus = function() {
};
Grammaticus.prototype = {
	NOUN_TYPE: 'n',
	ADJ_TYPE: 'a',
	ART_TYPE: 'd',
	GENDER_TYPE: 'g',
	PLURAL_TYPE: 'p',
	COUNTER_TYPE: 'c',
	MISSING: 'MISSING',
	parts: function() {
		return this._parts;
	},
	isString: function (obj) {
		return (typeof obj === 'string');
	},
	getLabel: function(label, nouns, args) {
		if (!this._labels) {
			return this.MISSING;
		}
		var str = this._labels[label];
		if (!str) {
			return this.MISSING;
		}
		return this.replaceTerms(str, nouns, args);
	},

	getString: function(label, nouns, args) {
		return this.formatArgs(this.getLabel(label, nouns, args), args);
	},


    /**
     * Format the message with the given arguments and renameable nouns.
     * @param messageObject is assumed to be an object with the parts in a predefined format
     * @param args is an array of strings or numbers to substitute for various parts (if keyed by numbers) or a map if keyed by name
     * Note: The Grammaticus object will be per-language and will not be
     *
     */
	replaceTerms: function(messageObject, nouns, args) {
		var str = messageObject;
		if (Array.isArray(messageObject)) {
			str = '';
			for (var i = 0; i < messageObject.length; i += 1) {
				var part = messageObject[i];
				if (this.isString(part)) {
					str += part;
				} else {
					// It's an object, figure out what.
					str += this.formatTerm(part, nouns, args, messageObject);
				}
			}
		} else {
			str = '' + messageObject;
		}
		return str;
	},

	getNoun: function(noun, form) {
		return noun && noun.v[form];
	},

	getModifier: function(modifier, form) {
		return modifier && modifier.v[form];
	},

	// OVERRIDE SECTION:  These allow the individual declension to override
	// Allow the child object to override
	formLowercaseNoun: function(value, form) {
		return !this.dont_capitalize ? value.toLowerCase() : value;
	},

	getModifierForm: function(termType, termForm, nounForm, noun, nextTerm) {
		// This gets modified based on the noun per declension
		return termForm;
	},

	getDefaultCounterWord: function() {
		return "";
	},

	getPluralCategory: function(val) {
		// Use the native if available.
		if (typeof Intl !== 'undefined' && typeof Intl.PluralRules !== 'undefined') {
			return new Intl.PluralRules(this.locale).select(val);
		}
		return 'other';
	},
	// END OVERRIDE SECTION

	formatNounTerm: function (term, nouns) {
		// If dynamic noun, i will be defined, but it could be 0
		var nounStr;
		if (term.i || term.i === 0) {
			nounStr = nouns[term.i];
		} else {
			nounStr = term.l;
		}
		if (!nounStr) {
			return '';
		}
		var noun = this.parts().n[nounStr.toLowerCase()];
		if (!noun) {
			return '';
		}

		var value = this.getNoun(noun, term.f);
		if (value && !term.c) {  // term.c = is capitalized
			value = this.formLowercaseNoun(value, term.f);
		}
		return value;
	},

	formatModifierTerm: function (term, nouns, termList) {
		var noun, value;
		// Get the noun ref
		var associatedNoun = termList[term.an];
		if (associatedNoun === null) {
			return '';   // If the adjective doesn't have any associated noun, return null to match java
		}

		// Get the relevant noun to see get the right value for StartsWith/Gender
		if ((associatedNoun.i || associatedNoun.i === 0) && nouns) {
			noun = this.parts().n[nouns[associatedNoun.i].toLowerCase()];
		} else {
			noun = this.parts().n[associatedNoun.l];
		}

		var nextTerm;
		if (term.nt === term.an) {
			nextTerm = noun;
		} else {
			var nextTermTag = termList[term.nt];
			if (nextTermTag) {
				nextTerm = this.parts()[nextTermTag.t][nextTermTag.l];
			} else {
				nextTerm = noun;
			}
		}

		// GetForm
		var form = this.getModifierForm(term.t, term.f, associatedNoun.f, noun, nextTerm);
		var part = this.parts()[term.t];
		if (!part) {
			return '';  // Missing part, just return '' to match Java
		}
		value = this.getModifier(part[term.l], form);

		if (value && !term.c) {
			value = value.toLowerCase();  // This isn't 100% correct
		}
		return value;
	},


	formatGenderTerm: function (term, nouns, args, termList) {
		var noun, associatedNoun = termList[term.an];
		if (associatedNoun === null) {
			return this.replaceTerms(term.def, nouns);   // If we can't find the noun, default
		}
		// Get the relevant noun to see get the right value for StartsWith/Gender
		if ((associatedNoun.i || associatedNoun.i === 0) && nouns) {
			noun = this.parts().n[nouns[associatedNoun.i].toLowerCase()];
		} else {
			noun = this.parts().n[associatedNoun.l];
		}
		if (!noun) {
			return this.replaceTerms(term.def, nouns);   // If we can't find the noun, default
		}
		// Get the "per gender", and if it's defined, use that for replacement.
		var perGender = term.v[noun.g];
		return this.replaceTerms(perGender != null ? perGender : term.def, nouns, args);
	},

	formatPluralTerm: function (term, nouns, args) {
		// TODO: Implement once we know what Intl looks like.
		var result = term.def;
		var arg = args[term.i];

		// Allow override for zero if provided
		if (arg === 0 && term.v.zero) {
			result = term.v.zero;
		} else {
			var type = this.getPluralCategory(arg);
			if (term.v && term.v[type]) {
				result = term.v[type];
			}
		}

		return this.replaceTerms(result, nouns);
	},

	formatCounterTerm: function (term, nouns, termList) {
		var noun, associatedNoun = termList[term.an];
		if (associatedNoun === null) {
			return this.getDefaultCounterWord();   // If we can't find the noun, default
		}
		// Get the relevant noun to see get the right value for StartsWith/Gender
		if ((associatedNoun.i || associatedNoun.i === 0) && nouns) {
			noun = this.parts().n[nouns[associatedNoun.i].toLowerCase()];
		} else {
			noun = this.parts().n[associatedNoun.l];
		}
		if (!noun) {
			return this.getDefaultCounterWord();   // If we can't find the noun, default
		}
		return noun.c || this.getDefaultCounterWord();
	},

	// Switch for formalTerm based on type
	formatTerm: function(term, nouns, args, termList) {
		var value = '';
		switch (term.t) {
		case this.NOUN_TYPE:  // noun
			value = this.formatNounTerm(term, nouns);
			break;
		case this.ADJ_TYPE:  // adjective
		case this.ART_TYPE:  // article
			value = this.formatModifierTerm(term, nouns, termList);
			break;
		case this.GENDER_TYPE:
			value = this.formatGenderTerm(term, nouns, args, termList);
			break;
		case this.PLURAL_TYPE:
			value = this.formatPluralTerm(term, nouns, args);
			break;
		case this.COUNTER_TYPE:
			value = this.formatCounterTerm(term, nouns, termList);
			break;
		default:
		}

		if (!value) {
			return '';  // In case anything ends up null or undefined, return empty string to prevent "Null" leakage
		}
		return value;
	},

	formatArgs: function(msg, parameters) {
		var f = function(str, match, offset, all) {
			var obj = parameters[parseInt(match, 10)];
			return Array.isArray(obj) ? obj[0] : obj;  // rhino issue
		};
		var res = msg.replace(/\{([0-9]*)\}/g, f);
		return res;
	},

	/**
	 * Dynamic components may need to add grammatical parts for their particular labels.
	 */
	addTerms: function(grammaticalParts) {
		if (!this._parts) {
			this._parts = grammaticalParts;
		}
		for (var type in grammaticalParts) {
			for (var part in grammaticalParts[type]) {
				this._parts[type][part] = grammaticalParts[type][part];
			}
		}
	},
	/**
	 * Dynamic components may need to add grammatical parts for their particular labels.
	 */
	addLabels: function(labels) {
		if (!this._labels) {
			this._labels = labels;
		}
		for (var k in labels) {
			this._labels[k] = labels[k];
		}
	}
};
if (typeof module != 'undefined' && module.exports) {
    module.exports = Grammaticus;
  // Browser.
} else {
    Grammaticus
}