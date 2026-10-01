# Universal Dictionary

Universal Dictionary is a lightweight Android language toolbox focused on fast lookup, learning, and source comparison.

## Included in the first build
- Definitions, parts of speech and examples.
- Phonetics and pronunciation audio when supplied by the data source.
- Synonyms and antonyms.
- Typing suggestions and spelling assistance through Datamuse.
- Saved-word state during the current app session.
- Android Process Text integration: select text in another app and choose Universal Dictionary.
- In-app dictionary browsing for Oxford Learner's Dictionaries, Cambridge Dictionary, Collins, Merriam-Webster, Wiktionary and WordReference.
- A deliberately separated data layer so licensed dictionary APIs can be added without rewriting the UI.

## API strategy
The primary lookup uses the Free Dictionary API, whose documented response includes definitions, phonetics, audio, examples, synonyms and antonyms. Datamuse provides suggestions and word-finding capabilities. citeturn1search1turn1search0

Premium/licensed adapters are kept separate from the UI:
- Oxford Dictionaries API provides dictionary, translation and lexical data; its current authentication uses an App ID and App Key. citeturn0search1turn0search9
- Cambridge offers a Dictionary API and licensed datasets. citeturn0search2turn0search17
- Collins offers dictionary APIs covering definitions, translations, examples, phrases and audio; its current English-only pricing lists up to 5,000 calls/month free. citeturn0search0
- Merriam-Webster provides dictionary/thesaurus APIs, but its published terms distinguish non-commercial use from advertising-supported/commercial apps. citeturn1search3turn1search12

Keys are not embedded in this repository. Licensed content will only be integrated after the required credentials and permissions are supplied.

## Build
The project uses Android Gradle Plugin 8.9.1, Kotlin 2.1.21 and Compose. Build locally with JDK 17+:

`gradle assembleDebug`

GitHub Actions builds the debug APK on pushes and pull requests.
