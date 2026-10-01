# Universal Dictionary

A lightweight, beautiful Android dictionary and vocabulary-learning toolbox built for everyday lookup and low-resource devices.

## Included now

### Dictionary
- Fast online lookup through the Free Dictionary API.
- Local offline core vocabulary for useful first-launch/offline lookups.
- Definitions, parts of speech and examples.
- Phonetics and supplied pronunciation audio.
- Android Text-to-Speech fallback when audio is unavailable.
- Synonyms and antonyms when supplied.
- Smart suggestions through Datamuse with local fallback.
- Spelling recovery and typo suggestions.
- Session caching plus persistent history.

### Personal vocabulary
- Saved words.
- Search history.
- Word of the Day.
- Daily learning streak.
- Lookup statistics.
- Vocabulary export/import.
- Copy and share complete entries.
- Android Process Text integration.
- Daily optional Word of the Day notification.

### Learning
- Vocabulary quiz.
- Personal learning dashboard.
- Possible word-form hints.
- Specialist vocabulary categories:
  - Legal
  - Medical
  - Science
  - Technology
  - Business
  - Academic
  - Nigerian English
  - Nigerian Pidgin

### Privacy/licensing strategy
Universal Dictionary deliberately avoids proprietary dictionary databases and provider credentials. Its built-in specialist/Nigerian material is original project content. Future open-data additions must be reviewed for license compatibility and attribution requirements.

For example, Wiktionary states that its original entry text is available under CC BY-SA and GFDL, with attribution/share-alike obligations; its individual media can have separate licenses. Any future Wiktionary-derived offline pack therefore needs proper attribution and license handling.

See [PRIVACY.md](PRIVACY.md).

## Android
- Kotlin + Jetpack Compose.
- Minimum Android API 26.
- System light/dark theme.
- U/open-book launcher branding.
- Low-dependency architecture suitable for budget devices.
- GitHub Actions build pipeline.
- Unit tests for bundled dictionary resources.

## Build
Use JDK 17+ and run:

    ./gradlew assembleDebug

The GitHub Actions workflow builds the debug APK and can be extended for release APK/AAB signing once release credentials are supplied.

## Data sources
- Free Dictionary API: primary network definitions.
- Datamuse: typing/suggestion assistance.
- Original Universal Dictionary offline and specialist cores.

No Oxford, Cambridge, Collins, Merriam-Webster or WordReference credentials are required.
