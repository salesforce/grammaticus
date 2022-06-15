![Build Status](https://github.com/salesforce/formula-engine/actions/workflows/ci.yml/badge.svg)

Grammaticus is a grammar engine that allows users to rename nouns while keeping content grammatically correct.
----------------------------------------------------------------------------------------------------------------------

Why did we build Grammaticus? 

At Salesforce, we have a feature called "Rename Tabs & Labels" which lets administrators change the name of standard parts of our product (like "Account"). However, the application often wants to display this label as part of a phrase, like `Open an Account`. But, if you renamed `Account` to `Client`, it would look both strange and grammatically incorrect: `Open an Client`. To support making these kinds of translations integrate naturally into an application, we developed a custom label file format. To ease the burden on translators (and the use of memory for translation), the label file format is XML, split into sections and keys. We use XML entities to represent the nouns, adjectives, and articles, such as Open `<a/> <Account/>` for the label above.

Grammaticus prevents your application from feeling foreign, and allows the expansion of your application to nouns defined by your customers. Salesforce extensively uses this feature with Custom Objects, allowing standard screens to say `All My Puppies` through the label `<All/> <My/> <Entity entity="0"/>`.  This also includes use of plural rules to correctly handle `Create 1 house` vs `Create 2 houses`.  Such as this example:

```
<param name="num_records_entity"><plural num="0"><when val="one">There is {0} <entity entity="0"/></when>There are {0} <entities entity="0"/></plural></param>
```

Grammaticus encodes the article, noun, and adjective declensions for over 30 languages, and supports programmatic use of nouns through the `Renameable` interface. The default label files included in /src/test provide a set of adjectives and articles already translated by Salesforce, along with some sample nouns.

For use in a browser or node.js, there is a beta offline engine that runs in javascript.  The `grammaticus.js` file contains a module that has base grammar rules that each declension then overrides.  The label files can be transferred to the client as json and cached there.  A future version will have automatic integration with webcomponents. See `OfflineProcessingTest` for examples

Disclaimer: This library requires developers and localizers to ensure that names of “renameable nouns” aren’t hard-coded, and that string concatenation for renameable objects isn’t used. It also requires your users to provide information around the nouns they are renaming. This includes gender and various language-specific grammar rules. 

----------------------------------------------------------------------------------------------------------------------
The files for translation are split into three different types:
- `names.xml`: The dictionary of all the nouns in a given language that your customers are allowed to change in each form for the language.
- `adjectives.xml`: The dictionary of all of the adjectives and articles you may need to conjugate for your users.
- `labels.xml (and imports)`: The labels themselves.

You can load labels from a file system, a jar file, or from a known URL. Some helpful classes around managing IniFiles are included as well for managing and censoring sensitive information from log files.

Some default behaviors, such as the list of supported languages, can be overridden by specifying an `i18n.properties` file in `/com/force/i18n` of your jar file. Specifically, you should override the LanguageProvider to return only the set of languages supported by your application.

----------------------------------------------------------------------------------------------------------------------
How to build:

Grammaticus uses Maven for its build lifecycle. Run the following commands to pull the source code and build package/jar in your development environment:

```shell
git clone https://github.com/salesforce/grammaticus
cd grammaticus
mvn package
```
----------------------------------------------------------------------------------------------------------------------
Known Limitations:
* Verbs are not part of the grammar engine. Semitic languages have inflected verbs based on the gender of the subject, so labels may be grammatically incorrect for labels that change gender.  You can fix these issues by using a `gender` tag, such as  `<gender><when val="m">MaleVerb</when>FemaleVerb</gender>` .
* Bantu language support (Swahili, Xhosa & Zulu) is in beta.
* Offline javascript support is in beta
* Partitive articles are not available.
* Many incomplete or unsupported declensions are provided for certain languages, because Salesforce doesn't translate into them. See `UnsupportedLanguageDeclension.java`.
* US English is the base language. Specifying a different base language is supported, but hasn't been tested.

1.1 Improvements
* New Languages: Afrikaans, Burmese, Gujarati, Kannada, Malayalam, Maori, Marathi, Swahili, Telugu, Xhosa, Zulu.
* Beta Offline Javascript label rendering support using grammaticus.js.
* PluralRules support (Now depends on icu4j) through the `<plural/>` tag
* Semitic verb support through the `<gender/>` tag
* Classifier/counting word support on nouns with the `<counter/`> tag.
* Support of Korean postpositions through endsWith tag, along with some defaults.
* Dual number support for Arabic and Slovenian.

1.2 Improvements
* New Languages: Amhartic, Khmer, Samoan, Hawaiian, Kazakh, Haitian Creole
* Support for ICU in BaseLocalizer for date and number formatting as an optional dependency.  
  Include icu4j-localespi as a dependency and call `BaseLocalizer.setLocaleFormatFixer(loc->BaseLocalizer.getICUFormatFixer())`
* Support graal.js for javascript testing
* Reduce logging for invalid labels
