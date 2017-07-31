Grammaticus is a localization library that allows the end users the ability to rename nouns while maintaining grammatical correctness.
----------------------------------------------------------------------------------------------------------------

This library was built to support the needs of allowing an end-user or customer to change the names of things used in an application.  Those things are referred to as "nouns". 

Salesforce has a feature called "Rename Tabs & Labels" which lets administrators change the name of standard Salesforce objects such
as Account, Contact, and Task.  However, the application often wants to display a string such as `Open an Account`, which
if you renamed `Account` to `Client`, would look strange and grammatically incorrect: `Open an Client`.  In order to support this feature, a custom label file format used.  To easy the burden of translators and the use of translation memory, the label file format is XML and split into sections and keys.  Use XML entities to represent the nouns, adjectives, and articles, such as `Open <a/> <Account/>` for the label above.  

Use of this library puts extra burden on your developers and localizers to make sure that you neither hard-code names of renameable nouns nor use string concatenation for renameable objects.  It also requires end-users to provide extra grammatical information around the nouns they are renaming, such as gender, starting with a vowel, and multiple forms for number, cases, articles, or possession.  The grammar engine prevents your application from feeling foreign, and allows expansion of your application to nouns defined by the customer.  Salesforce extensively uses this feature with Custom Objects, allowing standard screens to say `All My Puppies` through the label `<All/> <My/> <Entity entity="0"/>`

Grammaticus encodes the article, noun, and adjective declensions for 30+ languages and supports programmatic use of nouns through the `Renameable` interface.  The default label files included in testing provide a set of adjectives and articles already translated by salesforce, along with some sample nouns.  

----------------------------------------------------------------------------------------------------------------------
The files for translation are split into three different types
- `names.xml`: The dictionary of all the nouns in a given language that your customers will be allowed to change in each form for the language
- `adjectives.xml`: The dictionary of all all of the adjectives and articles you may need to conjugate for your customers
- `labels.xml` (and imports): The labels themselves.

You can load labels from a file system, from a jar file, or from a known URL.  Some helpful classes around managing IniFiles are included as well for managing censoring sensitive information from log files.

Some default behaviors (such as the list of supported languages) can be overridden by specifying an "i18n.properties" file in /com/force/i18n of your jar file.  You really want to override the LanguageProvider to return only the set of languages supported by your application.

----------------------------------------------------------------------------------------------------------------------
Known Limitations:
* Verbs are not part of the grammar engine.  Semitic languages have inflected verbs based on the gender of the subject, so labels may be grammatically incorrect for labels that change gender.
* Dual number are not supported as they are rather rare in languages used by Salesforce.  
* Partitive articles are not available.
* Many incomplete or unsupported declensions are provided for certain languages, because Salesforce doesn't translate into them.  See UnsupportedLanguageDeclension
* US English is considered to be the base language, and while specifying a different base language is supported, it hasn't been tested.
