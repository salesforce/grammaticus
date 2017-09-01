<a href="https://travis-ci.org/salesforce/grammaticus">
  <img src="https://travis-ci.org/salesforce/grammaticus.svg?branch=master">
</a>
<a href="https://scan.coverity.com/projects/salesforce-grammaticus">
  <img alt="Coverity Scan Build Status"
       src="https://scan.coverity.com/projects/13659/badge.svg"/>
</a>

Grammaticus is a grammar engine that allows users to rename nouns while keeping content grammatically correct.
----------------------------------------------------------------------------------------------------------------------

Why did we build Grammaticus? 

At Salesforce, we have a feature called "Rename Tabs & Labels" which lets administrators change the name of standard parts of our product (like "Account"). However, the application often wants to display this label as part of a phrase, like `Open an Account`. But, if you renamed `Account` to `Client`, it would look both strange and grammatically incorrect: `Open an Client`. To support making these kinds of translations integrate naturally into an application, we developed a custom label file format. To ease the burden on translators (and the use of memory for translation), the label file format is XML, split into sections and keys. We use XML entities to represent the nouns, adjectives, and articles, such as Open `<a/> <Account/>` for the label above.

Grammaticus prevents your application from feeling foreign, and allows the expansion of your application to nouns defined by your customers. Salesforce extensively uses this feature with Custom Objects, allowing standard screens to say `All My Puppies` through the label `<All/> <My/> <Entity entity="0"/>`.

Grammaticus encodes the article, noun, and adjective declensions for over 30 languages, and supports programmatic use of nouns through the `Renameable` interface. The default label files included in /src/test provide a set of adjectives and articles already translated by Salesforce, along with some sample nouns.

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
* Verbs are not part of the grammar engine. Semitic languages have inflected verbs based on the gender of the subject, so labels may be grammatically incorrect for labels that change gender.
* Dual number are not supported as they are rather rare in languages used by Salesforce.
* Partitive articles are not available.
* Many incomplete or unsupported declensions are provided for certain languages, because Salesforce doesn't translate into them. See `UnsupportedLanguageDeclension.java`.
* US English is the base language. Specifying a different base language is supported, but hasn't been tested.
