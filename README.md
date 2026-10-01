# Universal Dictionary

Universal Dictionary is a lightweight Android language toolbox for fast lookup, vocabulary building and source comparison.

## Current capabilities

- Definitions, parts of speech and examples.
- Phonetics and pronunciation audio when supplied.
- Synonyms and antonyms.
- Typing suggestions and spelling assistance.
- Persistent saved words and search history.
- Personal vocabulary learning screen.
- Android Process Text integration for selected text.
- Light/dark system theme.
- In-app browsing of Oxford Learner's Dictionaries, Cambridge Dictionary, Collins, Merriam-Webster, Wiktionary and WordReference.
- Search caching during a session.
- Separated repository/data layer ready for licensed dictionary providers.
- Branded Universal Dictionary launcher icon and SVG master artwork.
- GitHub Actions debug APK build pipeline.

## API strategy

The baseline lookup uses the Free Dictionary API and Datamuse for suggestions. Premium/licensed providers are intentionally kept behind a separate data boundary so credentials and licensing terms are not embedded in the application.

Oxford, Cambridge, Collins and Merriam-Webster integrations can be added when the required credentials and permissions are available. API keys must never be shipped as plaintext secrets inside the APK.

## Product roadmap

The architecture is prepared for:
- richer offline dictionary datasets with compatible licensing;
- provider comparison;
- word-of-the-day and notifications;
- vocabulary quizzes and streaks;
- specialist legal, medical, scientific, business and technology terminology;
- Nigerian English and Nigerian Pidgin resources;
- richer pronunciation and inflection data;
- production-grade privacy, accessibility and release hardening.

## Build

The project uses Android Gradle Plugin 8.9.1, Kotlin 2.1.21 and Compose. Build with JDK 17+:

`gradle assembleDebug`

GitHub Actions builds and publishes the debug APK as a workflow artifact.
